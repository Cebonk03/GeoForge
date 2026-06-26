package com.geoforge.engine.density;

import com.geoforge.engine.noise.NoiseSource;
import com.geoforge.engine.noise.SimplexNoise;

/**
 * River carver that carves steep-walled, flat-bottomed canyons using 2D
 * simplex noise to determine river positions.
 *
 * <p>The canyon profile produces vertical walls with a flat bottom, in contrast
 * to the v-shaped valley of {@link SimplexRiverCarver}. The full
 * {@code canyonDepth} is subtracted uniformly across the canyon floor, creating
 * a step function at the canyon walls rather than a gradual slope.
 *
 * <p>Carving applies where the noise value falls below a threshold derived from
 * the configured canyon width: {@code threshold = 1 / (1 + width × 0.15)}. Within
 * the canyon, density is reduced by the full {@code canyonDepth} regardless of
 * horizontal position within the river channel, producing the characteristic
 * flat bottom.
 */
public final class CanyonRiverCarver implements RiverCarver {
    private final NoiseSource noise;
    private final double frequency;
    private final int canyonDepth;
    private final int canyonWidth;

    /**
     * Creates a canyon river carver with the given parameters.
     *
     * @param seed        the world generation seed (will be decorrelated for noise)
     * @param frequency   frequency of the 2D river noise
     * @param canyonDepth carving depth — higher values produce deeper canyons.
     *                    The full depth is subtracted uniformly across the canyon
     *                    floor. A value of 0 disables carving.
     * @param canyonWidth canyon width parameter (higher = wider canyons)
     */
    public CanyonRiverCarver(long seed, double frequency, int canyonDepth, int canyonWidth) {
        if (canyonWidth <= 0) throw new IllegalArgumentException(
                "canyonWidth must be > 0, got " + canyonWidth);
        this.noise = new SimplexNoise(seed ^ 0xFEEDBEEFL);
        this.frequency = frequency;
        this.canyonDepth = canyonDepth;
        this.canyonWidth = canyonWidth;
    }

    @Override
    public double carve(double density, int blockX, int blockY, int blockZ) {
        // Skip carving when disabled or in air (negative density means above surface)
        if (canyonDepth <= 0 || density <= 0) {
            return density;
        }

        // Sample 2D noise at (x, z) to get river value in [-1, 1]
        double riverValue = noise.sample2D(blockX * frequency, blockZ * frequency);

        // Logistic-style bounded threshold: always in (0, 1] regardless of width
        double threshold = 1.0 / (1.0 + (double) canyonWidth * 0.15);

        if (riverValue < -threshold) {
            // Canyon: subtract full depth uniformly across the canyon floor.
            // This creates vertical walls at the threshold boundary (step function)
            // and a flat bottom (constant subtraction regardless of riverValue).
            density -= canyonDepth;
        }

        return density;
    }
}
