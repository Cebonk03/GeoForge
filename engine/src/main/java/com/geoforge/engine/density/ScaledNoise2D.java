package com.geoforge.engine.density;

import com.geoforge.engine.noise.SimplexNoise;

/**
 * A horizontal-only 2D noise sampler that ignores the Y coordinate.
 *
 * <p>This is an optimization for horizontal features such as continent shape,
 * biome temperature/humidity, and large-scale variation where Y-axis variation
 * is unnecessary. The Y parameter in {@link #sample(double, double, double)}
 * is accepted (to satisfy {@link DensityFunctionTree}) but has no effect on
 * the output.
 *
 * <p>For situations requiring full 3D noise (e.g., caves, overhangs, noise
 * pillars), use {@link ScaledNoise} instead.
 *
 * @param noise  the noise source
 * @param xScale scaling factor applied to the x-coordinate before sampling
 * @param zScale scaling factor applied to the z-coordinate before sampling
 */
public record ScaledNoise2D(SimplexNoise noise, double xScale, double zScale)
        implements DensityFunctionTree {

    @Override
    public double sample(double x, double y, double z) {
        return noise.sample(x * xScale, z * zScale);
    }
}
