package com.geoforge.engine.density;

/**
 * A utility for computing a Gaussian-like vertical envelope that reduces
 * cave density near the surface and at extreme depths.
 *
 * <p>The envelope combines a Gaussian centered at {@code caveCenterY} with
 * a surface cutoff factor that suppresses caves near the surface.
 */
public final class CaveYEnvelope {

    private static final double MAX_EXP_ARG = -700.0;

    private CaveYEnvelope() {
        // Utility class — no instances
    }

    /**
     * Computes the cave envelope value at the given Y coordinate.
     *
     * <p>The return value is in {@code [0, 1]} and represents the fraction of
     * cave density to retain. Higher values near the cave center Y produce
     * more caves; the surface cutoff suppresses caves as Y approaches the
     * surface from below.
     *
     * @param y                 the current Y coordinate (block space)
     * @param surfaceY          the surface height at this column (block space)
     * @param caveCenterY       the center Y of the cave distribution
     * @param caveSpread        the vertical spread of the Gaussian envelope
     * @param caveSurfaceCutoff distance from surface over which caves are
     *                          suppressed (blocks)
     * @return the envelope value in {@code [0, 1]}
     */
    public static double envelope(
            double y, double surfaceY, double caveCenterY,
            double caveSpread, double caveSurfaceCutoff) {
        if (caveSpread <= 0.0) {
            return 0.0;
        }

        // Vertical Gaussian factor centered at caveCenterY
        double dx = y - caveCenterY;
        double exponent = -(dx * dx) / (2.0 * caveSpread * caveSpread);
        if (exponent < MAX_EXP_ARG) {
            exponent = MAX_EXP_ARG;
        }
        double verticalFactor = Math.exp(exponent);

        // Surface cutoff factor: 1.0 at depth, 0.0 at/above surface
        double surfaceFactor;
        if (caveSurfaceCutoff <= 0.0) {
            surfaceFactor = 1.0;
        } else if (y < surfaceY) {
            surfaceFactor = Math.min(1.0, (surfaceY - y) / caveSurfaceCutoff);
        } else {
            surfaceFactor = 0.0;
        }

        return verticalFactor * surfaceFactor;
    }
}
