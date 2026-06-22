package com.geoforge.engine.density;

/**
 * A density function that returns the product of two child functions.
 *
 * @param a first operand
 * @param b second operand
 */
public record MultiplyDensity(DensityFunctionTree a, DensityFunctionTree b)
        implements DensityFunctionTree {

    @Override
    public double sample(double x, double y, double z) {
        return a.sample(x, y, z) * b.sample(x, y, z);
    }
}
