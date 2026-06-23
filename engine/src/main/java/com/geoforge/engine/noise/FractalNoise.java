package com.geoforge.engine.noise;

import com.geoforge.engine.density.DensityFunctionTree;

/**
 * Multi-octave fractal noise that combines several octaves of {@link SimplexNoise} at
 * increasing frequencies for more natural-looking terrain.
 *
 * <p>Each octave adds detail at a smaller scale. The {@code lacunarity} controls the
 * frequency multiplier between octaves, and {@code persistence} controls the amplitude
 * decay. The final value is normalized to [-1, 1] by dividing by the sum of amplitudes.
 *
 * @param noise       the base {@link SimplexNoise} source
 * @param octaves     number of octaves to sum (must be &gt; 0)
 * @param lacunarity  frequency multiplier between octaves (must be &gt; 0)
 * @param persistence amplitude multiplier between octaves (must be &gt; 0)
 */
public record FractalNoise(SimplexNoise noise, int octaves, double lacunarity, double persistence)
        implements DensityFunctionTree {

    /**
     * Validates that all parameters are positive.
     *
     * @throws IllegalArgumentException if {@code octaves <= 0}, {@code lacunarity <= 0},
     *                                  or {@code persistence <= 0}
     */
    public FractalNoise {
        if (octaves <= 0) {
            throw new IllegalArgumentException("octaves must be positive: " + octaves);
        }
        if (lacunarity <= 0) {
            throw new IllegalArgumentException("lacunarity must be positive: " + lacunarity);
        }
        if (persistence <= 0) {
            throw new IllegalArgumentException("persistence must be positive: " + persistence);
        }
    }

    /**
     * Samples the fractal noise at the given 3D coordinates.
     *
     * <p>The y-coordinate is ignored; noise is sampled in 2D (x, z) space. Each octave
     * samples {@code noise.sample(x * frequency, z * frequency)} and accumulates the
     * result weighted by the octave amplitude. The final sum is normalized to [-1, 1]
     * by dividing by the total amplitude sum.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate (ignored)
     * @param z the z-coordinate
     * @return the normalized fractal noise value in [-1, 1]
     */
    @Override
    public double sample(double x, double y, double z) {
        double value = 0.0;
        double amplitude = 1.0;
        double frequency = 1.0;
        double maxAmp = 0.0;

        for (int i = 0; i < octaves; i++) {
            value += noise.sample(x * frequency, z * frequency) * amplitude;
            maxAmp += amplitude;
            frequency *= lacunarity;
            amplitude *= persistence;
        }

        return value / maxAmp;
    }

    /**
     * Samples the fractal noise at the given 2D (x, z) coordinates.
     *
     * <p>Convenience method equivalent to {@code sample(x, 0, z)}.
     *
     * @param x the x-coordinate
     * @param z the z-coordinate
     * @return the normalized fractal noise value in [-1, 1]
     */
    public double sample2D(double x, double z) {
        return sample(x, 0, z);
    }
}
