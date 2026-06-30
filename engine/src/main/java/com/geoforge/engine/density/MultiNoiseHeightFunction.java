package com.geoforge.engine.density;

import com.geoforge.engine.geology.TectonicPlateMapper;
import com.geoforge.engine.noise.NoiseSource;

/**
 * A density function that blends three terrain types — ridge (mountains), FBM (hills),
 * and flat (plains/oceans) — based on tectonic plate continentalness with noise-warped
 * boundary transitions.
 *
 * <p>Blend weights are computed from continentalness as follows:
 *
 * <ul>
 *   <li>{@code c ∈ [0.0, 0.35]}: ocean — flat noise (seabed)
 *   <li>{@code c ∈ [0.25, 0.55]}: coast — blend flat → FBM
 *   <li>{@code c ∈ [0.45, 0.75]}: plains/hills — FBM dominant
 *   <li>{@code c ∈ [0.65, 1.0]}: highlands — blend FBM → ridge
 * </ul>
 *
 * <p>Continentalness is modulated by noise-warping for organic, invisible biome boundaries.
 * Zones overlap by 0.1 for smooth transitions.
 *
 * <p><b>Column weight caching:</b> Weights are computed ONCE per (x, z) column and
 * reused for all y samples via a {@link ThreadLocal} cache. This avoids redundant
 * continentalness queries when evaluating density across many Y levels in the same column.
 */
public final class MultiNoiseHeightFunction implements DensityFunctionTree {

    private final NoiseSource ridgeNoise;
    private final NoiseSource fbmNoise;
    private final NoiseSource flatNoise;
    private final TectonicPlateMapper plateMapper;
    private final NoiseSource boundaryWarpNoise;
    private final double ridgeFrequency;
    private final double fbmFrequency;
    private final double flatFrequency;
    private final double ridgeAmplitude;
    private final double continentalnessBlendSharpness;
    private final double boundaryWarpFrequency;
    private final double boundaryWarpAmplitude;

    // Column weight cache: ThreadLocal so multiple chunk workers don't collide
    private final ThreadLocal<ColumnCache> columnCache =
            ThreadLocal.withInitial(ColumnCache::new);

    private static final class ColumnCache {
        int lastX = Integer.MIN_VALUE;
        int lastZ = Integer.MIN_VALUE;
        double ridgeWeight;
        double fbmWeight;
        double flatWeight;
    }

    /**
     * Constructs a multi-noise height function.
     *
     * @param ridgeNoise                   noise source for mountain terrain
     * @param fbmNoise                     noise source for hill terrain
     * @param flatNoise                    noise source for flat/plains terrain
     * @param plateMapper                  tectonic plate mapper (provides continentalness)
     * @param seed                         seed (reserved for future use)
     * @param ridgeFrequency               frequency for ridge noise sampling
     * @param fbmFrequency                 frequency for FBM noise sampling
     * @param flatFrequency                frequency for flat noise sampling
     * @param ridgeAmplitude               amplitude multiplier for ridge noise
     * @param continentalnessBlendSharpness sharpness of transition between continentalness zones
     * @param boundaryWarpNoise            noise source for warping continentalness at biome boundaries
     * @param boundaryWarpFrequency        frequency for boundary warp noise
     * @param boundaryWarpAmplitude        amplitude for boundary warp noise
     */
    public MultiNoiseHeightFunction(
            NoiseSource ridgeNoise,
            NoiseSource fbmNoise,
            NoiseSource flatNoise,
            TectonicPlateMapper plateMapper,
            long seed,
            double ridgeFrequency,
            double fbmFrequency,
            double flatFrequency,
            double ridgeAmplitude,
            double continentalnessBlendSharpness,
            NoiseSource boundaryWarpNoise,
            double boundaryWarpFrequency,
            double boundaryWarpAmplitude) {
        this.ridgeNoise = ridgeNoise;
        this.fbmNoise = fbmNoise;
        this.flatNoise = flatNoise;
        this.plateMapper = plateMapper;
        this.boundaryWarpNoise = boundaryWarpNoise;
        this.ridgeFrequency = ridgeFrequency;
        this.fbmFrequency = fbmFrequency;
        this.flatFrequency = flatFrequency;
        this.ridgeAmplitude = ridgeAmplitude;
        this.continentalnessBlendSharpness = continentalnessBlendSharpness;
        this.boundaryWarpFrequency = boundaryWarpFrequency;
        this.boundaryWarpAmplitude = boundaryWarpAmplitude;
    }

    /**
     * Samples the blended terrain height at the given coordinates.
     *
     * <p>The Y coordinate is ignored — terrain height is a 2D property.
     *
     * @param x the x-coordinate (block space)
     * @param y the y-coordinate (ignored)
     * @param z the z-coordinate (block space)
     * @return the blended height detail offset in blocks
     */
    @Override
    public double sample(double x, double y, double z) {
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);

        ColumnCache cache = columnCache.get();
        if (cache.lastX != bx || cache.lastZ != bz) {
            computeWeights(bx, bz, cache);
            cache.lastX = bx;
            cache.lastZ = bz;
        }

        // Sample each noise type at the scaled position
        double ridgeVal = ridgeNoise.sample2D(x * ridgeFrequency, z * ridgeFrequency);
        double fbmVal = fbmNoise.sample2D(x * fbmFrequency, z * fbmFrequency);
        double flatVal = flatNoise.sample2D(x * flatFrequency, z * flatFrequency);

        // Weighted blend: ridge gets amplified, fbm/flat are used directly
        return cache.ridgeWeight * ridgeVal * ridgeAmplitude
                + cache.fbmWeight * fbmVal
                + cache.flatWeight * flatVal;
    }

    /**
     * Returns the 3 cached blend weights for the given block column, or computes
     * them if not yet cached. This is package-private for testing purposes.
     *
     * @param blockX the x-coordinate
     * @param blockZ the z-coordinate
     * @return a 3-element array [ridgeWeight, fbmWeight, flatWeight] summing to ~1
     */
    double[] getCachedWeights(int blockX, int blockZ) {
        ColumnCache cache = columnCache.get();
        if (cache.lastX != blockX || cache.lastZ != blockZ) {
            computeWeights(blockX, blockZ, cache);
            cache.lastX = blockX;
            cache.lastZ = blockZ;
        }
        return new double[] { cache.ridgeWeight, cache.fbmWeight, cache.flatWeight };
    }

    /**
     * Computes blend weights from continentalness with noise-warped boundary transitions.
     */
    private void computeWeights(int blockX, int blockZ, ColumnCache cache) {
        float continentalnessF = plateMapper.getContinentalness(blockX, blockZ);
        double c = Math.min(1.0, Math.max(0.0, (double) continentalnessF));

        // Apply noise warp to continentalness for organic boundary transitions
        double warp = boundaryWarpNoise.sample2D(
                (double) blockX * boundaryWarpFrequency,
                (double) blockZ * boundaryWarpFrequency) * boundaryWarpAmplitude;
        c = Math.min(1.0, Math.max(0.0, c + warp));

        double shp = Math.max(0.1, continentalnessBlendSharpness);

        double ridgeW;
        double fbmW;
        double flatW;

        // Zone 1: Ocean (c ∈ [0, 0.35]) — flat dominates with transition to FBM
        if (c <= 0.35) {
            double t = c / 0.35; // 0..1 in this zone
            double blend = smoothstep(t, shp);
            flatW = 1.0 - blend;
            fbmW = blend;
            ridgeW = 0.0;
        }
        // Zone 2: Coast (c ∈ [0.25, 0.55]) — flat → FBM transition, overlaps with ocean by 0.1
        else if (c <= 0.55) {
            double t = (c - 0.25) / 0.3; // 0..1 in this zone
            double blend = smoothstep(t, shp);
            flatW = (1.0 - blend);
            fbmW = blend;
            ridgeW = 0.0;
        }
        // Zone 3: Plains/hills (c ∈ [0.45, 0.75]) — FBM dominant, overlaps with coast by 0.1
        else if (c <= 0.75) {
            flatW = 0.0;
            fbmW = 1.0;
            ridgeW = 0.0;
        }
        // Zone 4: Highlands (c ∈ [0.65, 1.0]) — FBM → ridge transition, overlaps with plains by 0.1
        else {
            double t = (c - 0.65) / 0.35; // 0..1 in this zone
            double blend = smoothstep(t, shp);
            fbmW = 1.0 - blend;
            ridgeW = blend;
            flatW = 0.0;
        }

        // Normalize so weights sum to 1
        double sum = ridgeW + fbmW + flatW;
        if (sum > 0.0) {
            cache.ridgeWeight = ridgeW / sum;
            cache.fbmWeight = fbmW / sum;
            cache.flatWeight = flatW / sum;
        } else {
            // Fallback (should not happen): uniform weights
            cache.ridgeWeight = 0.0;
            cache.fbmWeight = 1.0;
            cache.flatWeight = 0.0;
        }
    }

    /**
     * Smoothstep function with configurable sharpness.
     * Higher sharpness = more abrupt transition.
     */
    private static double smoothstep(double t, double sharpness) {
        if (t <= 0.0) return 0.0;
        if (t >= 1.0) return 1.0;
        // Apply sharpness as a power curve, then smoothstep
        double st = Math.pow(t, sharpness);
        return st * st * (3.0 - 2.0 * st);
    }

    /**
     * Resets the column weight cache for this thread.
     * Useful in testing to force cache recomputation.
     */
    void resetCache() {
        columnCache.remove();
    }
}
