package com.geoforge.engine.density;

import com.geoforge.engine.noise.NoiseSource;
import com.geoforge.engine.noise.SimplexNoise;

/**
 * River carver that carves wide, shallow floodplains with gentle banks using
 * 2D simplex noise to determine river positions.
 *
 * <p>The floodplain profile produces broad, shallow valleys with smooth,
 * gradual banks, in contrast to the steep v-shaped valleys of
 * {@link SimplexRiverCarver} and the vertical-walled canyons of
 * {@link CanyonRiverCarver}.
 *
 * <p>Carving applies where the noise value falls below a threshold derived from
 * the configured floodplain width. The intensity ramps smoothly from zero at the
 * valley edges to maximum at the river center using a {@code sqrt} curve, which
 * concentrates carving in the center while keeping the banks gentle. The
 * effective carving depth is reduced compared to the v-shaped profile, producing
 * a characteristically shallow floodplain.
 */
public final class FloodplainRiverCarver implements RiverCarver {
    private final NoiseSource noise;
    private final double frequency;
    private final int depth;
    private final int floodplainWidth;

    /**
     * Creates a floodplain river carver with the given parameters.
     *
     * @param seed           the world generation seed (will be decorrelated for noise)
     * @param frequency      frequency of the 2D river noise
     * @param depth          maximum carving depth — floodplain carving is shallower
     *                       by design (effective depth is {@code depth × 0.5}).
     *                       A value of 0 disables carving.
     * @param floodplainWidth floodplain width parameter (higher = wider floodplains)
     */
    public FloodplainRiverCarver(long seed, double frequency, int depth, int floodplainWidth) {
        if (floodplainWidth <= 0) throw new IllegalArgumentException(
                "floodplainWidth must be > 0, got " + floodplainWidth);
        this.noise = new SimplexNoise(seed ^ 0xFEEDBEEFL);
        this.frequency = frequency;
        this.depth = depth;
        this.floodplainWidth = floodplainWidth;
    }

    @Override
    public double carve(double density, int blockX, int blockY, int blockZ) {
        // Skip carving when disabled or in air (negative density means above surface)
        if (depth <= 0 || density <= 0) {
            return density;
        }

        // Sample 2D noise at (x, z) to get river value in [-1, 1]
        double riverValue = noise.sample2D(blockX * frequency, blockZ * frequency);

        // Floodplain uses a wider threshold for broad river zones
        double threshold = 1.0 / (1.0 + (double) floodplainWidth * 0.15);

        if (riverValue < -threshold) {
            // Depth factor diminishes with depth below surface:
            //   density ≈ 0 at surface (max carving)
            //   density >= depth at valley bottom (zero carving)
            //   density < 0 above surface (already air, handled above)
            double depthFactor = Math.max(0, 1.0 - density / (double) depth);

            // Normalized intensity: 0 at valley edge, 1 at river center
            double rawIntensity = (-threshold - riverValue) / (1.0 - threshold);

            // Gentle banks: sqrt curve gives a smooth ramp near the edges
            // instead of the linear ramp of the v-shaped profile
            double smoothIntensity = Math.sqrt(rawIntensity);

            // Shallower: multiply effective depth by 0.5
            density -= smoothIntensity * depth * 0.5 * depthFactor;
        }

        return density;
    }
}
