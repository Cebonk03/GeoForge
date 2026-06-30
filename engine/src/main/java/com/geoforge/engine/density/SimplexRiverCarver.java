package com.geoforge.engine.density;

import com.geoforge.engine.noise.NoiseSource;
import com.geoforge.engine.noise.GradientNoise;

/**
 * River carver that uses 2D simplex noise to carve v-shaped river valleys
 * along moisture convergence zones in the terrain.
 *
 * <p>This is the "vshaped" profile implementation. The carving tapers linearly
 * from maximum at the valley center to zero at the valley edges, and also
 * diminishes with depth below the terrain surface. The result is a classic
 * V-shaped valley cross-section.
 *
 * <p>The carving applies only where the noise value falls below a threshold
 * determined by the configured river width. The threshold formula
 * {@code 1 / (1 + width * 0.15)} is self-bounding to the range (0, 1], ensuring
 * that extreme width values never produce degenerate carving behavior.
 * Below threshold, the density is reduced vertically with a depth factor
 * that diminishes below the surface.
 */
public final class SimplexRiverCarver implements RiverCarver {
    private final NoiseSource noise;
    private final double frequency;
    private final int depth;
    private final int width;

    /**
     * Creates a river carver with the given parameters.
     *
     * @param seed      the world generation seed (will be decorrelated for noise)
     * @param frequency frequency of the 2D river noise
     * @param depth     carving depth scaling factor — higher values produce deeper river
     *                  valleys. Used as a divisor in the depth factor calculation:
     *                  {@code depthFactor = max(0, 1 - density / depth)}. The effective
     *                  carving depth in blocks is approximately
     *                  {@code (threshold - riverValue) * depth * depthFactor}.
     * @param width     river width parameter (higher = wider rivers)
     */
    public SimplexRiverCarver(long seed, double frequency, int depth, int width) {
        if (width <= 0) throw new IllegalArgumentException(
                "width must be > 0, got " + width);
        this.noise = new GradientNoise(seed ^ 0xFEEDBEEFL);
        this.frequency = frequency;
        this.depth = depth;
        this.width = width;
    }

    @Override
    public double carve(double density, int blockX, int blockY, int blockZ) {
        // Skip carving in air (negative density means already above surface)
        if (density <= 0) return density;

        // Sample 2D noise at (x, z) to get river value in [-1, 1]
        double riverValue = noise.sample2D(blockX * frequency, blockZ * frequency);

        // Logistic-style bounded threshold: always in (0, 1] regardless of width.
        // This is anti-fragile — extreme width values degrade gracefully
        // instead of producing degenerate (<=0) thresholds.
        double threshold = 1.0 / (1.0 + (double) width * 0.15);
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
