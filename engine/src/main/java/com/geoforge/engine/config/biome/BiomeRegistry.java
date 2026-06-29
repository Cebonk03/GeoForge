package com.geoforge.engine.config.biome;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central thread-safe registry of all biome definitions with hot reload support.
 *
 * <p>Uses {@link ConcurrentHashMap} for O(1) point lookups and immutable
 * {@link BiomeDefinition} records. The {@link #reload(Path, ClimateResolver.ClimateConfig,
 * long)} method builds an entirely new registry atomically — readers continue to
 * see the old registry until the reference is swapped.
 *
 * <p>This class is thread-safe after construction. All public methods are safe
 * to call from concurrent chunk generation threads.
 */
public final class BiomeRegistry {

    private final ConcurrentHashMap<String, BiomeDefinition> biomeMap;
    private final ClimateResolver climateResolver;
    private final Set<String> allBiomeIds;

    /**
     * Creates a new BiomeRegistry from the given load result and resolver.
     *
     * @param loadResult      the loaded biome definitions (may contain errors)
     * @param climateResolver the climate resolver for position-based lookups
     */
    public BiomeRegistry(BiomeLoadResult loadResult, ClimateResolver climateResolver) {
        this.climateResolver = climateResolver != null ? climateResolver : new ClimateResolver(ClimateResolver.ClimateConfig.defaults(), java.util.List.of(), "ocean");
        this.biomeMap = new ConcurrentHashMap<>();

        if (loadResult != null && loadResult.biomes() != null) {
            this.biomeMap.putAll(loadResult.biomes());
        }

        // Derive all biome IDs from registry + climate envelopes
        var ids = new LinkedHashSet<String>();
        ids.addAll(this.biomeMap.keySet());
        if (climateResolver != null) {
            for (var env : climateResolver.envelopes()) {
                ids.add(env.biomeId());
            }
        }
        this.allBiomeIds = Collections.unmodifiableSet(ids);
    }

    /**
     * Loads biome configs from disk and creates a new registry atomically.
     *
     * @param biomesDir    the root biomes directory
     * @param climateConfig climate sampling configuration
     * @param seed         world seed (for climate resolver noise sources)
     * @return a new BiomeRegistry, or {@code null} if loading failed
     */
    public static BiomeRegistry reload(Path biomesDir,
                                        ClimateResolver.ClimateConfig climateConfig,
                                        long seed) {
        var loader = new BiomeConfigLoader();
        BiomeLoadResult result = loader.loadFromDirectory(biomesDir);

        // Build resolver from climate.yml data
        ClimateResolver resolver = buildResolver(climateConfig, result, seed);

        // Only include successfully parsed biomes (no errors)
        if (result.hasErrors()) {
            return new BiomeLoadResult(
                    result.biomes(),
                    result.errors(),
                    result.warnings()
            ).biomes().isEmpty() ? null : new BiomeRegistry(result, resolver);
        }

        return new BiomeRegistry(result, resolver);
    }

    /**
     * Returns the biome definition for the given biome ID.
     *
     * @param biomeId the biome identifier
     * @return the biome definition, or {@link BiomeDefinition#defaults()} if unknown
     */
    public BiomeDefinition forBiome(String biomeId) {
        BiomeDefinition def = biomeMap.get(biomeId);
        return def != null ? def : BiomeDefinition.defaults();
    }

    /**
     * Returns an unmodifiable set of all known biome IDs.
     *
     * @return an immutable set of biome ID strings
     */
    public Set<String> getAllBiomeIds() {
        return allBiomeIds;
    }

    /**
     * Returns the climate resolver associated with this registry.
     *
     * @return the climate resolver
     */
    public ClimateResolver climateResolver() {
        return climateResolver;
    }

    /**
     * Returns the number of registered biomes.
     *
     * @return biome count
     */
    public int size() {
        return biomeMap.size();
    }

    /**
     * Checks whether this registry has any biome definitions.
     *
     * @return {@code true} if the registry is empty
     */
    public boolean isEmpty() {
        return biomeMap.isEmpty();
    }

    // ──────────────────────────────────────────────
    //  Private helpers
    // ──────────────────────────────────────────────

    private static ClimateResolver buildResolver(ClimateResolver.ClimateConfig climateConfig,
                                                  BiomeLoadResult result, long seed) {
        // For now, use the legacy export until climate.yml-based envelopes are fully supported
        return new ClimateResolver(
                climateConfig != null ? climateConfig : ClimateResolver.ClimateConfig.defaults(),
                ClimateResolver.exportFromLegacyTable(),
                "ocean");
    }
}
