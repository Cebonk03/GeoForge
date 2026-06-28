package com.geoforge.engine.density;

import com.geoforge.engine.config.GeoForgeConfig;

/**
 * A three-type cave carving utility that extends the basic 3D cave noise with
 * distinct cave morphologies: spaghetti, cheese, and noodle.
 *
 * <p>Each cave type uses a different noise threshold test to determine where
 * to carve additional air blocks:
 *
 * <ul>
 *   <li><b>Spaghetti:</b> {@code |noise3D| < spaghettiThreshold} &mdash; narrow winding tunnels.
 *   <li><b>Cheese:</b>  {@code noise3D > cheeseThreshold} &mdash; large open chambers.
 *   <li><b>Noodle:</b>  {@code |noiseA| + |noiseB| < noodleThreshold} &mdash; thin branching tunnels.
 * </ul>
 *
 * <p>Each type is dispatched via the {@link CaveType} enum. All carving is modulated
 * by {@link CaveYEnvelope#envelope} so that caves are suppressed near the surface and
 * at extreme depths. When all three thresholds are at their extreme (disabled) values
 * the method returns the original density with near-zero overhead.
 */
public final class EnhancedCaveSystem {

    private static final double EPSILON = 1e-12;

    private EnhancedCaveSystem() {
        // Utility class — no instances
    }

    /**
     * Carves additional cave volume into the existing density field.
     *
     * @param originalDensity the density value before enhanced cave carving
     * @param noise3D         the 3D cave noise value (used for spaghetti and cheese)
     * @param noiseA          first noise value for noodle cave detection
     * @param noiseB          second noise value for noodle cave detection
     * @param y               the current block Y coordinate
     * @param surfaceY        the surface height at this column (block space)
     * @param config          the engine configuration with cave thresholds
     * @return the modified density value (more negative = more air carved)
     */
    public static double carve(
            double originalDensity,
            double noise3D,
            double noiseA,
            double noiseB,
            double y,
            double surfaceY,
            GeoForgeConfig config) {

        double envelope = CaveYEnvelope.envelope(y, surfaceY, config.caveCenterY(),
                config.caveSpread(), config.caveSurfaceCutoff());
        if (envelope <= EPSILON) {
            return originalDensity;
        }
        double result = originalDensity;
        double carveTarget = -envelope;
        for (CaveType type : CaveType.VALUES) {
            double thresh = clampToUnit(type.threshold(config));
            if (!type.isDisabled(thresh) && type.test(noise3D, noiseA, noiseB, thresh)) {
                result = Math.min(result, carveTarget);
            }
        }
        return result;
    }

    /**
     * Clamps a value to the closed interval [0, 1].
     */
    private static double clampToUnit(double value) {
        if (value < 0.0) return 0.0;
        if (value > 1.0) return 1.0;
        return value;
    }
}
