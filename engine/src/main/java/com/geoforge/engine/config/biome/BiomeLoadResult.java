package com.geoforge.engine.config.biome;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the result of loading biome configuration files.
 *
 * <p>Contains successfully parsed biomes plus any errors or warnings
 * encountered during loading. Use {@link #hasErrors()} to check if the
 * load was fully successful.
 *
 * @param biomes   map of biome ID to successfully parsed {@link BiomeDefinition}
 * @param errors   list of fatal error messages (empty if fully successful)
 * @param warnings list of non-fatal warning messages
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public record BiomeLoadResult(
        Map<String, BiomeDefinition> biomes,
        List<String> errors,
        List<String> warnings) {

    /**
     * Returns {@code true} if no fatal errors occurred during loading.
     *
     * @return {@code true} if the errors list is empty
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns an empty result with no biomes, no errors, and no warnings.
     *
     * @return an empty successful result
     */
    public static BiomeLoadResult empty() {
        return new BiomeLoadResult(Map.of(), List.of(), List.of());
    }

    /**
     * Merges two load results, combining their biomes, errors, and warnings.
     *
     * <p>If both results define the same biome ID, the one from {@code b} wins.
     *
     * @param a first result
     * @param b second result (takes precedence on biome ID conflicts)
     * @return a new combined result
     */
    public static BiomeLoadResult merge(BiomeLoadResult a, BiomeLoadResult b) {
        var mergedBiomes = new java.util.LinkedHashMap<String, BiomeDefinition>();
        if (a != null && a.biomes() != null) {
            mergedBiomes.putAll(a.biomes());
        }
        if (b != null && b.biomes() != null) {
            mergedBiomes.putAll(b.biomes());
        }

        var mergedErrors = new ArrayList<String>();
        if (a != null && a.errors() != null) mergedErrors.addAll(a.errors());
        if (b != null && b.errors() != null) mergedErrors.addAll(b.errors());

        var mergedWarnings = new ArrayList<String>();
        if (a != null && a.warnings() != null) mergedWarnings.addAll(a.warnings());
        if (b != null && b.warnings() != null) mergedWarnings.addAll(b.warnings());

        return new BiomeLoadResult(
                Collections.unmodifiableMap(mergedBiomes),
                Collections.unmodifiableList(mergedErrors),
                Collections.unmodifiableList(mergedWarnings));
    }
}
