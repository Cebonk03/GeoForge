package com.geoforge.engine.density;

/**
 * A density function that returns a constant value regardless of input coordinates.
 *
 * @param value the constant density value
 */
public record ConstantDensity(double value) implements DensityFunctionTree {

    @Override
    public double sample(double x, double y, double z) {
        return value;
    }
}
