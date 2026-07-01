package com.geoforge.engine.noise;

/**
 * A noise source that warps input coordinates before delegating to a wrapped source.
 *
 * <p>Domain warping distorts the coordinate space, creating more organic, less
 * "noise-like" patterns. This follows the same composable decorator pattern as
 * {@link com.geoforge.engine.density.DomainWarpDensity}, but operates at the
 * {@link NoiseSource} level.
 *
 * <p>When {@code amplitude} is zero, this source delegates directly to the wrapped
 * source with zero overhead.
 *
 * @param wrapped  the underlying noise source being warped
 * @param warpX    noise source for X-axis coordinate warping
 * @param warpZ    noise source for Z-axis coordinate warping
 * @param warpY    noise source for Y-axis coordinate warping
 * @param amplitude the warp amplitude in coordinate units (0 = no warping)
 */
public record DomainWarpedNoiseSource(
        NoiseSource wrapped,
        NoiseSource warpX,
        NoiseSource warpZ,
        NoiseSource warpY,
        double amplitude) implements NoiseSource {

    @Override
    public double sample2D(double x, double z) {
        if (amplitude == 0.0) {
            return wrapped.sample2D(x, z);
        }
        double wx = x + warpX.sample2D(x, z) * amplitude;
        double wz = z + warpZ.sample2D(x, z) * amplitude;
        return wrapped.sample2D(wx, wz);
    }

    @Override
    public double sample3D(double x, double y, double z) {
        if (amplitude == 0.0) {
            return wrapped.sample3D(x, y, z);
        }
        // Use 2D warp noise for (x,z) and 2D for y, keeping warp computation cheap
        double wx = x + warpX.sample2D(x, z) * amplitude;
        double wy = y + warpY.sample2D(x, z) * amplitude;
        double wz = z + warpZ.sample2D(x, z) * amplitude;
        return wrapped.sample3D(wx, wy, wz);
    }
}
