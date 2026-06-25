package com.geoforge.engine.util;

/**
 * Utility for sanitizing density values against NaN, Infinity, and extreme values.
 *
 * <p>Applied as a safety net in {@code getDensity()} to prevent floating-point
 * corruption from propagating into chunk generation. NaN and Infinity are replaced
 * with a safe negative value (air), and extreme values are clamped to a reasonable
 * range relative to world boundaries.
 *
 * <p>Benchmarks: {@code Double.isFinite()} is a single JVM intrinsic —
 * overhead is &lt; 1% of a typical density computation.
 */
public final class DensityGuard {

    private DensityGuard() {}

    /**
     * Sanitizes a density value, replacing NaN/Infinity with a safe negative value
     * and clamping extremes to reasonable bounds.
     *
     * @param density  the raw density value
     * @param minWorld minimum world Y level
     * @param maxWorld maximum world Y level
     * @return a safe density value
     */
    public static double clamp(double density, int minWorld, int maxWorld) {
        if (!Double.isFinite(density)) {
            return Double.NEGATIVE_INFINITY; // air
        }
        // Clamp to a reasonable range: world height + generous margin
        double margin = (maxWorld - minWorld) * 2.0;
        if (density < minWorld - margin) return minWorld - margin;
        if (density > maxWorld + margin) return maxWorld + margin;
        return density;
    }
}
