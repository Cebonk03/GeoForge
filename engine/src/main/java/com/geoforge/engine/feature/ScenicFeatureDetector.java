package com.geoforge.engine.feature;

import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.noise.NoiseSource;
import com.geoforge.engine.noise.GradientNoise;

/**
 * Detects scenic terrain features (wow moments) from local height analysis.
 *
 * <p>Three feature types are detected from purely local height data at each surface column:
 *
 * <ul>
 *   <li><b>EMERGENCE</b> — Potential cave exit near a cliff face. Detected when cave noise
 *       is active near the surface and the local slope exceeds a threshold.
 *   <li><b>HIDDEN_VALLEY</b> — A ridge crest where the terrain drops sharply on one side
 *       by at least 20 blocks within 10 blocks horizontal distance.
 *   <li><b>EDGE_VISTA</b> — A cliff edge with a clear drop on one side and relatively
 *       flat terrain on the other, suggesting a viewpoint.
 * </ul>
 *
 * <p>All detection is derived purely from (x, z, seed) — deterministic and Folia-safe.
 * No cross-chunk state is required.
 */
public final class ScenicFeatureDetector {

    /** Minimum height drop in blocks to qualify as HIDDEN_VALLEY. */
    private static final int VALLEY_MIN_DROP = 20;

    /** Horizontal search radius in blocks for height drop detection. */
    private static final int VALLEY_SEARCH_RADIUS = 10;

    /** Minimum gradient magnitude for EDGE_VISTA detection. */
    private static final double EDGE_MIN_GRADIENT = 4.0;

    /** Cave noise threshold at surface for EMERGENCE detection. */
    private static final double EMERGENCE_CAVE_THRESHOLD = 0.6;

    /** Noise source for feature placement variation (decorrelated seed). */
    private final NoiseSource featureNoise;

    /**
     * Creates a scenic feature detector.
     *
     * @param seed the world seed (decorrelated internally)
     */
    public ScenicFeatureDetector(long seed) {
        this.featureNoise = new GradientNoise(seed ^ 0xABCDEF01L);
    }

    /**
     * The type of scenic feature detected at a surface column.
     */
    public enum FeatureType {
        /** No scenic feature at this location. */
        NONE,
        /** Cave exit toward a cliff face or open area. */
        EMERGENCE,
        /** Ridge crest with a sharp drop into a valley on one side. */
        HIDDEN_VALLEY,
        /** Cliff edge with layered depth — a natural viewpoint. */
        EDGE_VISTA
    }

    /**
     * Result of scenic feature detection for a surface column.
     *
     * @param type       the detected feature type
     * @param intensity  signal strength in [0, 1]; higher = more pronounced
     * @param direction  horizontal angle in radians toward the feature's focal direction,
     *                   or 0 if not applicable
     */
    public record FeatureResult(FeatureType type, double intensity, double direction) {}

    /**
     * Detects scenic features at the given surface column.
     *
     * <p>Uses the engine's density function for cave noise and height queries.
     * This method is safe to call from concurrent chunk generation threads
     * (no mutable shared state).
     *
     * @param engine      the generation engine (for height and cave queries)
     * @param blockX      the block X coordinate
     * @param blockZ      the block Z coordinate
     * @param surfaceY    the surface height at this column
     * @return the detected feature result (never null)
     */
    public FeatureResult detect(GeoForgeEngine engine, int blockX, int blockZ, int surfaceY) {
        // Sample gradient components at this column using domain-warped height (matches actual terrain)
        double hCenter = engine.getWarpedHeightAt(blockX, blockZ);
        double hDx = engine.getWarpedHeightAt(blockX + 5, blockZ);
        double hDz = engine.getWarpedHeightAt(blockX, blockZ + 5);
        double gradX = (hDx - hCenter) / 5.0;
        double gradZ = (hDz - hCenter) / 5.0;
        double gradientMag = Math.sqrt(gradX * gradX + gradZ * gradZ);

        // Early-out: flat terrain (low gradient) has no scenic features
        if (gradientMag < 0.5) {
            return new FeatureResult(FeatureType.NONE, 0.0, 0.0);
        }

        // EDGE_VISTA: steep gradient on one side suggests a cliff edge
        if (gradientMag >= EDGE_MIN_GRADIENT) {
            return new FeatureResult(FeatureType.EDGE_VISTA,
                    Math.min(1.0, gradientMag / 10.0),
                    Math.atan2(gradZ, gradX));
        }

        // HIDDEN_VALLEY: check 4 cardinal directions at full search radius
        // Uses getWarpedHeightAt() (domain-warped) for fast height screening, then verifies with getSurfaceHeight()
        int[][] dirs = {{10, 0}, {-10, 0}, {0, 10}, {0, -10}};
        for (int[] d : dirs) {
            int nx = blockX + d[0];
            int nz = blockZ + d[1];
            double neighborHeight = engine.getWarpedHeightAt(nx, nz);
            double drop = hCenter - neighborHeight;
            if (drop >= VALLEY_MIN_DROP) {
                int neighborSurfaceY = engine.getSurfaceHeight(nx, nz);
                int actualDrop = surfaceY - neighborSurfaceY;
                if (actualDrop >= VALLEY_MIN_DROP) {
                    double intensity = Math.min(1.0, (double) actualDrop / 40.0);
                    double angle = Math.atan2(d[1], d[0]);
                    double noiseVal = featureNoise.sample2D(blockX * 0.001, blockZ * 0.001);
                    if (noiseVal > 0.0) {
                        return new FeatureResult(FeatureType.HIDDEN_VALLEY, intensity, angle);
                    }
                }
            }
        }

        // EMERGENCE: check if cave noise is active near the surface
        var ctx = com.geoforge.engine.ColumnContext.compute(engine, blockX, blockZ);
        double caveNearSurface = engine.getDensity(blockX, surfaceY - 3, blockZ, ctx)
                - engine.getDensity(blockX, surfaceY, blockZ, ctx);
        if (caveNearSurface < -EMERGENCE_CAVE_THRESHOLD && gradientMag > 2.0) {
            return new FeatureResult(FeatureType.EMERGENCE,
                    Math.min(1.0, -caveNearSurface / 2.0),
                    Math.atan2(gradZ, gradX));
        }

        return new FeatureResult(FeatureType.NONE, 0.0, 0.0);
    }
}
