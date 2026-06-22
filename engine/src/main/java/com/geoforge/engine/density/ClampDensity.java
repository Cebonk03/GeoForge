package com.geoforge.engine.density;

/**
 * A density function that clamps the output of a child function to a specified range.
 *
 * @param inner the child function whose output is clamped
 * @param min   the minimum allowed value
 * @param max   the maximum allowed value
 */
public record ClampDensity(DensityFunctionTree inner, double min, double max)
        implements DensityFunctionTree {

    @Override
    public double sample(double x, double y, double z) {
        double v = inner.sample(x, y, z);
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
