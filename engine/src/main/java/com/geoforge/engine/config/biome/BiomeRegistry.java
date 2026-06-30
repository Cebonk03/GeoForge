package com.geoforge.engine.config.biome;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central thread-safe registry of all biome definitions.
 *
 * <p>Uses {@link ConcurrentHashMap} for O(1) point lookups and immutable
 * {@link BiomeDefinition} records. Built from a {@code Map<String, BiomeDefinition>}
 * and a {@link ClimateResolver} for position-based biome resolution.
 *
 * <p>This class is thread-safe after construction. All public methods are safe
 * to call from concurrent chunk generation threads.
 */
public final class BiomeRegistry {

    private final ConcurrentHashMap<String, BiomeDefinition> biomeMap;
    private final ClimateResolver climateResolver;
    private final Set<String> allBiomeIds;

    /**
     * Creates a new BiomeRegistry from the given biome definitions and resolver.
     *
     * @param biomes          the biome definitions keyed by biome ID (must not be null)
     * @param climateResolver the climate resolver for position-based lookups
     */
    public BiomeRegistry(Map<String, BiomeDefinition> biomes, ClimateResolver climateResolver) {
        this.climateResolver = climateResolver != null ? climateResolver
                : new ClimateResolver(ClimateResolver.ClimateConfig.defaults(),
                        java.util.List.of(), "ocean");
        this.biomeMap = new ConcurrentHashMap<>();

        if (biomes != null) {
            this.biomeMap.putAll(biomes);
        }

        // Derive all biome IDs from registry + climate envelopes
        var ids = new LinkedHashSet<String>();
        ids.addAll(this.biomeMap.keySet());
        for (var env : this.climateResolver.envelopes()) {
            ids.add(env.biomeId());
        }
        this.allBiomeIds = Collections.unmodifiableSet(ids);
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
}
