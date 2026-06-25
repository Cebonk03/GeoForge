package com.geoforge.engine.density;

import com.geoforge.engine.noise.SimplexNoise;

/**
 * River carver that uses 2D simplex noise to carve river valleys
 * along moisture convergence zones in the terrain.
 *
 * <p>The carving applies only where the noise value falls below a threshold
 * determined by the configured river width. Below threshold, the density is
 * reduced vertically with a depth factor that diminishes below the surface.
 */
public final class SimplexRiverCarver implements RiverCarver {
    private final SimplexNoise noise;
    private final double frequency;
    private final int depth;
    private final int width;

    /**
     * Creates a river carver with the given parameters.
     *
     * @param seed      the world generation seed (will be decorrelated for noise)
     * @param frequency frequency of the 2D river noise
     * @param depth     maximum river carving depth in blocks
     * @param width     river width parameter (higher = wider rivers)
     */
    public SimplexRiverCarver(long seed, double frequency, int depth, int width) {
        this.noise = new SimplexNoise(seed ^ 0xFEEDBEEFL);
        this.frequency = frequency;
        this.depth = depth;
        this.width = width;
    }

    @Override
    public double carve(double density, int blockX, int blockY, int blockZ) {
        // Skip carving in air (negative density means already above surface)
        if (density <= 0) return density;

        // Sample 2D noise at (x, z) to get river value in [-1, 1]
        double riverValue = noise.sample(blockX * frequency, blockZ * frequency);

        // Only carve where noise indicates river (below threshold)
        // The threshold controls river width
        double threshold = 1.0 - width * 0.1; // wider rivers = lower threshold
        if (riverValue < -threshold) {
            // Carve vertically - deeper near surface, shallower below
            // Use density as a proxy for distance below terrain surface:
            //   density ≈ 0 at surface (max carving)
            //   density >= depth at river bottom (zero carving)
            //   density < 0 above surface (already air, handled above)
            double depthFactor = Math.max(0, 1.0 - density / (double) depth);
            density -= (threshold - riverValue) * depth * depthFactor;
        }
        return density;
    }
}
