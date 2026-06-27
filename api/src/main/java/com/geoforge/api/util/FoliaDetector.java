package com.geoforge.api.util;

/**
 * Detects whether the server is running Folia's regionised threading model.
 *
 * <p>This class contains the single sanctioned {@code Class.forName()} call in the entire
 * GeoForge codebase, as permitted by the zero-reflection-for-version-logic rule (DP-4).
 */
public final class FoliaDetector {

    private static final String FOLIA_REGIONIZED_SERVER =
            "io.papermc.paper.threadedregions.RegionizedServer";

    private static final Boolean FOLIA_CACHED = computeFolia();

    private FoliaDetector() {}

    private static boolean computeFolia() {
        try {
            Class.forName(FOLIA_REGIONIZED_SERVER);
            return true;
        } catch (ClassNotFoundException | LinkageError | SecurityException ignored) {
            return false;
        }
    }

    /**
     * Returns whether the current server is running Folia.
     *
     * @return true if Folia's RegionizedServer class is present on the classpath
     */
    public static boolean isFolia() {
        return FOLIA_CACHED;
    }
}
