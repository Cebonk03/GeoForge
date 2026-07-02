package com.geoforge.engine.feature.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable registry of biome-to-tree-type mappings with per-biome configuration.
 *
 * <p>Provides a static {@link #defaults()} factory that reproduces the current
 * tree-type assignment for all vanilla Minecraft biomes, including bug fixes and
 * previously missing entries.
 *
 * <p>Use the inner {@link Builder} to create custom registries.
 *
 * @see TreeType
 * @see BiomeTreeConfig
 */
@Deprecated
public final class TreeRegistry {

    private final Map<String, List<TreeType>> biomeTreeMap;
    private final Map<String, BiomeTreeConfig> biomeConfigs;

    private TreeRegistry(Map<String, List<TreeType>> biomeTreeMap,
                         Map<String, BiomeTreeConfig> biomeConfigs) {
        this.biomeTreeMap = Map.copyOf(biomeTreeMap);
        this.biomeConfigs = Map.copyOf(biomeConfigs);
    }

    /**
     * Returns the list of tree species that can spawn in the given biome,
     * or an empty list if the biome has no registered trees.
     *
     * @param biomeId the biome identifier (e.g. {@code "forest"})
     * @return unmodifiable list of available tree types, never {@code null}
     */
    public List<TreeType> speciesForBiome(String biomeId) {
        return biomeTreeMap.getOrDefault(biomeId, List.of());
    }

    /**
     * Returns the per-biome tree configuration for the given biome,
     * or {@link BiomeTreeConfig#defaults()} if no config was registered.
     *
     * @param biomeId the biome identifier
     * @return the biome tree config, never {@code null}
     */
    public BiomeTreeConfig configForBiome(String biomeId) {
        return biomeConfigs.getOrDefault(biomeId, BiomeTreeConfig.defaults());
    }

    /**
     * Returns an unmodifiable view of the biome-to-species mapping.
     *
     * @return unmodifiable map of biome ID to tree-type list
     */
    public Map<String, List<TreeType>> biomeTreeMap() {
        return biomeTreeMap;
    }

    /**
     * Returns a {@link TreeRegistry} that reproduces the current biome-to-tree-type
     * mapping with all entries using {@link BiomeTreeConfig#defaults()}.
     *
     * <p>This includes bug fixes ({@code cherry_grove} &rarr; {@link TreeType#CHERRY},
     * {@code mangrove_swamp} &rarr; {@link TreeType#MANGROVE}) and previously missing
     * entries ({@code swamp}, {@code pale_garden}, {@code mushroom_fields}).
     *
     * @return the default tree registry
     */
    public static TreeRegistry defaults() {
        return new Builder()
                // Forest biomes — oak and birch
                .putBiomeTreeOverride("forest", List.of(TreeType.OAK, TreeType.BIRCH))
                .putBiomeTreeOverride("flower_forest", List.of(TreeType.OAK, TreeType.BIRCH))
                .putBiomeTreeOverride("windswept_forest", List.of(TreeType.OAK, TreeType.SPRUCE))
                .putBiomeTreeOverride("old_growth_birch_forest", List.of(TreeType.BIRCH))

                // Pure birch
                .putBiomeTreeOverride("birch_forest", List.of(TreeType.BIRCH))

                // Dark forest
                .putBiomeTreeOverride("dark_forest", List.of(TreeType.DARK_OAK, TreeType.OAK))

                // Taiga / cold — spruce
                .putBiomeTreeOverride("taiga", List.of(TreeType.SPRUCE))
                .putBiomeTreeOverride("snowy_taiga", List.of(TreeType.SPRUCE))
                .putBiomeTreeOverride("old_growth_pine_taiga", List.of(TreeType.SPRUCE))
                .putBiomeTreeOverride("old_growth_spruce_taiga", List.of(TreeType.SPRUCE))
                .putBiomeTreeOverride("grove", List.of(TreeType.SPRUCE))

                // Jungle
                .putBiomeTreeOverride("jungle", List.of(TreeType.JUNGLE))
                .putBiomeTreeOverride("bamboo_jungle", List.of(TreeType.JUNGLE))
                .putBiomeTreeOverride("sparse_jungle", List.of(TreeType.JUNGLE))

                // Open / grassy biomes — occasional oak
                .putBiomeTreeOverride("plains", List.of(TreeType.OAK))
                .putBiomeTreeOverride("sunflower_plains", List.of(TreeType.OAK))
                .putBiomeTreeOverride("meadow", List.of(TreeType.OAK))

                // FIXED: cherry_grove should use CHERRY, not OAK
                .putBiomeTreeOverride("cherry_grove", List.of(TreeType.CHERRY))

                // FIXED: mangrove_swamp should use MANGROVE, not OAK
                .putBiomeTreeOverride("mangrove_swamp", List.of(TreeType.MANGROVE))

                // Windswept
                .putBiomeTreeOverride("windswept_hills", List.of(TreeType.OAK, TreeType.SPRUCE))

                // Savanna — acacia
                .putBiomeTreeOverride("savanna", List.of(TreeType.ACACIA))
                .putBiomeTreeOverride("windswept_savanna", List.of(TreeType.ACACIA))

                // NEW — was missing
                .putBiomeTreeOverride("swamp", List.of(TreeType.OAK, TreeType.MANGROVE))
                .putBiomeTreeOverride("pale_garden", List.of(TreeType.PALE_OAK))
                .putBiomeTreeOverride("mushroom_fields", List.of())

                .build();
    }

    // ──────────────────────────────────────────────
    //  Builder
    // ──────────────────────────────────────────────

    /**
     * Builder for constructing immutable {@link TreeRegistry} instances.
     *
     * <p>Biome entries are accumulated via {@link #putBiome(String, List, BiomeTreeConfig)}
     * <p>Biome entries are accumulated via {@link #putBiome(String, List, BiomeTreeConfig)}
     */
    public static final class Builder {
        private final Map<String, List<TreeType>> biomeTreeMap;
        private final Map<String, BiomeTreeConfig> biomeConfigs;

        public Builder() {
            this.biomeTreeMap = new HashMap<>();
            this.biomeConfigs = new HashMap<>();
        }

        /**
         * Registers tree species and per-biome configuration for the given biome.
         *
         * @param biomeId the biome identifier (must not be null or blank)
         * @param species the list of tree species (must not be null)
         * @param config  the per-biome configuration (must not be null)
         * @return this builder for chaining
         * @throws NullPointerException     if any argument is null
         * @throws IllegalArgumentException if biomeId is blank
         */
        public Builder putBiome(String biomeId, List<TreeType> species, BiomeTreeConfig config) {
            Objects.requireNonNull(biomeId, "biomeId must not be null");
            Objects.requireNonNull(species, "species must not be null");
            Objects.requireNonNull(config, "config must not be null");
            if (biomeId.isBlank()) {
                throw new IllegalArgumentException("biomeId must not be blank");
            }
            biomeTreeMap.put(biomeId, List.copyOf(species));
            biomeConfigs.put(biomeId, config);
            return this;
        }

        /**
         * Registers tree species for the given biome with {@link BiomeTreeConfig#defaults()}.
         *
         * @param biomeId the biome identifier (must not be null or blank)
         * @param species the list of tree species (must not be null)
         * @return this builder for chaining
         * @throws NullPointerException     if any argument is null
         * @throws IllegalArgumentException if biomeId is blank
         */
        public Builder putBiomeTreeOverride(String biomeId, List<TreeType> species) {
            return putBiome(biomeId, species, BiomeTreeConfig.defaults());
        }

        /**
         * Builds an immutable {@link TreeRegistry}.
         *
         * @return a new immutable TreeRegistry
         */
        public TreeRegistry build() {
            return new TreeRegistry(biomeTreeMap, biomeConfigs);
        }
    }
}
