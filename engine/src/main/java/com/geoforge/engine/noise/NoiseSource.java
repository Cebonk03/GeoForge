package com.geoforge.engine.noise;

/**
 * A source of deterministic noise values at 2D and 3D coordinates.
 *
 * <p>Implementations produce values in approximately [-1, 1] and are consistent
 * for the same {@code long} seed. This is the primary abstraction for all noise
 * generation in the engine. Both {@link GradientNoise} and {@link FastNoiseLiteSource}
 * implement this interface, allowing them to be swapped without changing consumers.
 *
 * <p>All implementations must be thread-safe and deterministic.
 *
 * @see GradientNoise
 * @see FastNoiseLiteSource
 */
public interface NoiseSource {

    /**
     * Samples noise at the given 2D (x, z) coordinates.
     *
     * @param x the x-coordinate
     * @param z the z-coordinate
     * @return noise value in approximately [-1, 1]
     */
    double sample2D(double x, double z);

    /**
     * Samples noise at the given 3D (x, y, z) coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return noise value in approximately [-1, 1]
     */
    double sample3D(double x, double y, double z);
}
