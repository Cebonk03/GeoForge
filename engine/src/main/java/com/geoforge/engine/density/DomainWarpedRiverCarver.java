package com.geoforge.engine.density;

import com.geoforge.engine.noise.NoiseSource;

/**
 * A river carver that warps input coordinates before delegating to a wrapped carver.
 *
 * <p>Domain warping distorts the river noise input space, creating natural meanders
 * and eliminating the "canal river" anti-pattern. This follows the same composable
 * decorator pattern as {@link DomainWarpDensity}.
 *
 * <p>When {@code amplitude} is zero, this carver delegates directly to the wrapped
 * carver with zero overhead.
 *
 * @param wrapped   the underlying river carver being warped
 * @param warpX     noise source for X-axis coordinate warping
 * @param warpZ     noise source for Z-axis coordinate warping
 * @param amplitude the warp amplitude in block units (0 = no warping)
 */
public record DomainWarpedRiverCarver(
        RiverCarver wrapped,
        NoiseSource warpX,
        NoiseSource warpZ,
        double amplitude) implements RiverCarver {

    @Override
    public double carve(double density, int blockX, int blockY, int blockZ) {
        if (amplitude == 0.0) {
            return wrapped.carve(density, blockX, blockY, blockZ);
        }
        double wx = blockX + warpX.sample2D(blockX, blockZ) * amplitude;
        double wz = blockZ + warpZ.sample2D(blockX, blockZ) * amplitude;
        return wrapped.carve(density, (int) Math.round(wx), blockY, (int) Math.round(wz));
    }
}
