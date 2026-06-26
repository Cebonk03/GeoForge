package com.geoforge.engine.density;

import com.geoforge.engine.noise.NoiseSource;

/**
 * A density function that samples a SimplexNoise instance with configurable scale factors.
 *
 * @param noise   the noise source
 * @param xScale  scaling factor for the x-coordinate
 * @param yScale  scaling factor for the y-coordinate
 * @param zScale  scaling factor for the z-coordinate
 */
public record ScaledNoise(NoiseSource noise, double xScale, double yScale, double zScale)
        implements DensityFunctionTree {

    @Override
    public double sample(double x, double y, double z) {
        return noise.sample3D(x * xScale, y * yScale, z * zScale);
    }
}
