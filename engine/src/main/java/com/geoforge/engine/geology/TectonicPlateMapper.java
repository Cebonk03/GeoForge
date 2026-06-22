package com.geoforge.engine.geology;

import com.geoforge.engine.noise.SimplexNoise;
import java.util.SplittableRandom;

/**
 * Maps block coordinates to a continentalness value using Voronoi plate simulation.
 *
 * <p>At construction, {@code plateCount} seed points are randomly placed. The continentalness
 * at any coordinate is the distance to the nearest plate centre, modulated by simplex noise
 * for organic coastlines. Values above 0.5 represent continental interior (land), below 0.2
 * represent deep ocean.
 */
public final class TectonicPlateMapper {

    private static final int PLATE_COUNT = 12;

    private final float[] plateX;
    private final float[] plateZ;
    private final SimplexNoise coastlineNoise;
    private final SimplexNoise modulateNoise;

    /** Creates a mapper with a default plate count of 12. */
    public TectonicPlateMapper(long seed) {
        this(seed, PLATE_COUNT);
    }

    /** Creates a mapper with a custom plate count. */
    public TectonicPlateMapper(long seed, int plateCount) {
        var rng = new SplittableRandom(seed);
        this.plateX = new float[plateCount];
        this.plateZ = new float[plateCount];

        // Spread plate centres across a wide area
        float spread = 5000.0f;
        for (int i = 0; i < plateCount; i++) {
            plateX[i] = (rng.nextFloat() - 0.5f) * 2.0f * spread;
            plateZ[i] = (rng.nextFloat() - 0.5f) * 2.0f * spread;
        }

        this.coastlineNoise = new SimplexNoise(seed ^ 0xDEADBEEFL);
        this.modulateNoise = new SimplexNoise(seed ^ 0xCAFEBABEL);
    }

    /**
     * Returns the continentalness at the given block coordinates.
     *
     * @param blockX the x-coordinate
     * @param blockZ the z-coordinate
     * @return value in [0.0, 1.0]; > 0.5 indicates land
     */
    public float getContinentalness(int blockX, int blockZ) {
        // Find distance to nearest plate centre
        float minDistSq = Float.MAX_VALUE;
        float secondMinDistSq = Float.MAX_VALUE;

        for (int i = 0; i < plateX.length; i++) {
            float dx = blockX - plateX[i];
            float dz = blockZ - plateZ[i];
            float distSq = dx * dx + dz * dz;
            if (distSq < minDistSq) {
                secondMinDistSq = minDistSq;
                minDistSq = distSq;
            } else if (distSq < secondMinDistSq) {
                secondMinDistSq = distSq;
            }
        }

        // Voronoi: closest / second_closest gives a value near 0 at plate centre
        // and approaching 1 near cell boundaries (edge factor)
        float edgeFactor =
                (float) Math.sqrt(minDistSq)
                        / ((float) Math.sqrt(secondMinDistSq) + 0.001f);

        // Modulate with noise for organic coastlines
        double noiseMod = coastlineNoise.sample(blockX * 0.002, blockZ * 0.002) * 0.3;
        double largeMod = modulateNoise.sample(blockX * 0.0005, blockZ * 0.0005) * 0.2;

        float raw = edgeFactor + (float) noiseMod + (float) largeMod;
        return clamp(raw, 0.0f, 1.0f);
    }

    private static float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
