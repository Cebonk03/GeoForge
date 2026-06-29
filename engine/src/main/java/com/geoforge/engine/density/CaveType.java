package com.geoforge.engine.density;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.geoforge.engine.config.GeoForgeConfig;

/**
 * Enumeration of enhanced cave carving types used by {@link EnhancedCaveSystem}.
 *
 * <p>Each variant implements a strategy-pattern dispatch with abstract methods
 * for extracting its threshold from a configuration, checking whether the
 * threshold is at its disabled extreme, and testing whether carving
 * conditions are met at a given point.
 *
 * <ul>
 *   <li><b>SPAGHETTI:</b> {@code |noise3D| < threshold} — narrow winding tunnels.
 *   <li><b>CHEESE:</b>  {@code noise3D > threshold} — large open chambers.
 *   <li><b>NOODLE:</b>  {@code |noiseA| + |noiseB| < threshold} — thin branching tunnels.
 * </ul>
 */
public enum CaveType {

    SPAGHETTI {
        @Override
        public double threshold(final GeoForgeConfig config) {
            return config.caveSpaghettiThreshold();
        }

        @Override
        public boolean isDisabled(final double threshold) {
            return threshold <= EPSILON;
        }

        @Override
        public boolean test(
                final double noise3D,
                final double noiseA,
                final double noiseB,
                final double threshold) {
            return Math.abs(noise3D) < threshold;
        }
    },

    CHEESE {
        @Override
        public double threshold(final GeoForgeConfig config) {
            return config.caveCheeseThreshold();
        }

        @Override
        public boolean isDisabled(final double threshold) {
            return threshold >= 1.0 - EPSILON;
        }

        @Override
        public boolean test(
                final double noise3D,
                final double noiseA,
                final double noiseB,
                final double threshold) {
            return noise3D > threshold;
        }
    },

    NOODLE {
        @Override
        public double threshold(final GeoForgeConfig config) {
            return config.caveNoodleThreshold();
        }

        @Override
        public boolean isDisabled(final double threshold) {
            return threshold <= EPSILON;
        }

        @Override
        public boolean test(
                final double noise3D,
                final double noiseA,
                final double noiseB,
                final double threshold) {
            return (Math.abs(noiseA) + Math.abs(noiseB)) < threshold;
        }
    };

    /**
     * Cached array of all CaveType values for hot-path iteration.
     * Use this instead of {@link #values()} in performance-critical loops.
     */
    @SuppressFBWarnings("MS_PKGPROTECT")
    public static final CaveType[] VALUES = values();

    private static final double EPSILON = 1e-12;

    /**
     * Extracts this cave type's raw threshold value from the given configuration.
     *
     * @param config the engine configuration
     * @return the raw (unclamped) threshold for this cave type
     */
    public abstract double threshold(GeoForgeConfig config);

    /**
     * Returns {@code true} when the threshold is at its extreme (disabled) value
     * and no carving of this type can occur.
     *
     * @param threshold the clamped threshold value (in {@code [0, 1]})
     * @return {@code true} if this cave type is disabled
     */
    public abstract boolean isDisabled(double threshold);

    /**
     * Tests whether the carving condition for this cave type is satisfied.
     *
     * @param noise3D  the 3D cave noise value (used by spaghetti and cheese)
     * @param noiseA   first noise component for noodle cave detection
     * @param noiseB   second noise component for noodle cave detection
     * @param threshold the clamped threshold value
     * @return {@code true} if the carving condition is met
     */
    public abstract boolean test(
            double noise3D, double noiseA, double noiseB, double threshold);
}
