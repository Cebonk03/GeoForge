package com.geoforge.engine.density;

import com.geoforge.engine.noise.NoiseSource;

/**
 * A density function that applies domain warping to a wrapped function.
 *
 * <p>Warping shifts the input {@code (x, z)} coordinates by noise offsets
 * before sampling the wrapped function, creating organic-looking distortion
 * of terrain features. The warp X and Z noise sources should use different
 * seeds or coordinate offsets for decorrelated warping in each axis.
 *
 * <p>When {@code amplitude} is zero, this function delegates directly to
 * the wrapped function with zero overhead.
 *
 * @param warpX     noise source for X-axis warping
 * @param warpZ     noise source for Z-axis warping
 * @param wrapped   the wrapped density function
 * @param amplitude the warp amplitude in blocks (0 = no warping)
 */
public record DomainWarpDensity(
        NoiseSource warpX, NoiseSource warpZ,
        DensityFunctionTree wrapped,
        double amplitude) implements DensityFunctionTree {

    @Override
    public double sample(double x, double y, double z) {
        if (amplitude == 0.0) {
            return wrapped.sample(x, y, z);
        }
        double wx = x + warpX.sample2D(x, z) * amplitude;
        double wz = z + warpZ.sample2D(x, z) * amplitude;
        return wrapped.sample(wx, y, wz);
    }
}
