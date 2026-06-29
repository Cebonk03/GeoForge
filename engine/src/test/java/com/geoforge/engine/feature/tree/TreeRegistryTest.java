package com.geoforge.engine.feature.tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TreeRegistry immutable config tests")
class TreeRegistryTest {

    private static final List<TreeType> OAK_BIRCH = List.of(TreeType.OAK, TreeType.BIRCH);
    private static final List<TreeType> OAK_SPRUCE = List.of(TreeType.OAK, TreeType.SPRUCE);
    private static final List<TreeType> DARK_OAK_OAK = List.of(TreeType.DARK_OAK, TreeType.OAK);
    private static final List<TreeType> OAK_MANGROVE = List.of(TreeType.OAK, TreeType.MANGROVE);

    // ──────────────────────────────────────────────
    //  Builder
    // ──────────────────────────────────────────────

    @DisplayName("Builder builds valid TreeRegistry")
    @Test
    void builder_buildsValidRegistry() {
        var registry = new TreeRegistry.Builder()
                .putBiomeTreeOverride("forest", List.of(TreeType.OAK, TreeType.BIRCH))
                .putBiomeTreeOverride("taiga", List.of(TreeType.SPRUCE))
                .build();

        assertThat(registry.speciesForBiome("forest")).containsExactly(TreeType.OAK, TreeType.BIRCH);
        assertThat(registry.speciesForBiome("taiga")).containsExactly(TreeType.SPRUCE);
    }

    @DisplayName("Builder with custom config stores config for known biome")
    @Test
    void builder_withCustomConfig_storesConfig() {
        var config = new BiomeTreeConfig(0.5, 4, 8, Map.of());
        var registry = new TreeRegistry.Builder()
                .putBiome("forest", List.of(TreeType.OAK), config)
                .build();

        assertThat(registry.configForBiome("forest")).isSameAs(config);
    }

    @DisplayName("putBiomeTreeOverride sets config to defaults")
    @Test
    void putBiomeTreeOverride_usesDefaultConfig() {
        var registry = new TreeRegistry.Builder()
                .putBiomeTreeOverride("plains", List.of(TreeType.OAK))
                .build();

        assertThat(registry.configForBiome("plains")).isEqualTo(BiomeTreeConfig.defaults());
    }

    @DisplayName("Builder.build() validates null biomeId")
    @Test
    void builder_rejectsNullBiomeId() {
        assertThatThrownBy(() -> new TreeRegistry.Builder()
                .putBiomeTreeOverride(null, List.of(TreeType.OAK)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("biomeId");
    }

    @DisplayName("Builder.build() validates null species")
    @Test
    void builder_rejectsNullSpecies() {
        assertThatThrownBy(() -> new TreeRegistry.Builder()
                .putBiomeTreeOverride("forest", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("species");
    }

    @DisplayName("Builder.build() validates null config")
    @Test
    void builder_rejectsNullConfig() {
        assertThatThrownBy(() -> new TreeRegistry.Builder()
                .putBiome("forest", List.of(TreeType.OAK), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("config");
    }

    @DisplayName("Builder rejects blank biomeId")
    @Test
    void builder_rejectsBlankBiomeId() {
        assertThatThrownBy(() -> new TreeRegistry.Builder()
                .putBiomeTreeOverride("  ", List.of(TreeType.OAK)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("biomeId must not be blank");
    }

    // ──────────────────────────────────────────────
    //  Query methods
    // ──────────────────────────────────────────────

    @DisplayName("speciesForBiome returns correct species for known biome")
    @Test
    void speciesForBiome_returnsCorrectSpecies() {
        var registry = new TreeRegistry.Builder()
                .putBiomeTreeOverride("forest", OAK_BIRCH)
                .build();

        assertThat(registry.speciesForBiome("forest"))
                .containsExactly(TreeType.OAK, TreeType.BIRCH);
    }

    @DisplayName("speciesForBiome returns empty list for unknown biome")
    @Test
    void speciesForBiome_returnsEmptyListForUnknown() {
        var registry = new TreeRegistry.Builder()
                .putBiomeTreeOverride("forest", List.of(TreeType.OAK))
                .build();

        assertThat(registry.speciesForBiome("unknown_biome")).isEmpty();
    }

    @DisplayName("configForBiome returns BiomeTreeConfig.defaults() for unknown biome")
    @Test
    void configForBiome_returnsDefaultsForUnknown() {
        var registry = new TreeRegistry.Builder()
                .putBiomeTreeOverride("forest", List.of(TreeType.OAK))
                .build();

        assertThat(registry.configForBiome("unknown_biome"))
                .isEqualTo(BiomeTreeConfig.defaults());
    }

    @DisplayName("configForBiome returns custom config for known biome")
    @Test
    void configForBiome_returnsCustomConfig() {
        var config = new BiomeTreeConfig(0.75, 6, 10, Map.of("tall", 2.0));
        var registry = new TreeRegistry.Builder()
                .putBiome("jungle", List.of(TreeType.JUNGLE), config)
                .build();

        assertThat(registry.configForBiome("jungle"))
                .isEqualTo(config);
    }

    // ──────────────────────────────────────────────
    //  Immutability
    // ──────────────────────────────────────────────

    @DisplayName("biomeTreeMap() returns unmodifiable map")
    @Test
    void biomeTreeMap_isUnmodifiable() {
        var registry = new TreeRegistry.Builder()
                .putBiomeTreeOverride("forest", List.of(TreeType.OAK))
                .build();

        assertThatThrownBy(() -> registry.biomeTreeMap().put("new_biome", List.of(TreeType.BIRCH)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @DisplayName("Builder species list is copied and immutable in constructed registry")
    @Test
    void builderSpeciesListIsCopied() {
        var mutable = new java.util.ArrayList<>(List.of(TreeType.OAK));
        var builder = new TreeRegistry.Builder();
        builder.putBiomeTreeOverride("forest", mutable);
        mutable.add(TreeType.BIRCH); // modify after put

        assertThat(builder.build().speciesForBiome("forest")).containsExactly(TreeType.OAK);
    }

    // ──────────────────────────────────────────────
    //  defaults()
    // ──────────────────────────────────────────────

    @DisplayName("defaults() contains all expected biome entries")
    @Test
    void defaults_containsAllExpectedBiomes() {
        var registry = TreeRegistry.defaults();
        var map = registry.biomeTreeMap();

        // Should have 25 entries (22 original + 3 new)
        assertThat(map).hasSize(25);

        // Forest biomes
        assertThat(map.get("forest")).containsExactly(TreeType.OAK, TreeType.BIRCH);
        assertThat(map.get("flower_forest")).containsExactly(TreeType.OAK, TreeType.BIRCH);
        assertThat(map.get("windswept_forest")).containsExactly(TreeType.OAK, TreeType.SPRUCE);
        assertThat(map.get("old_growth_birch_forest")).containsExactly(TreeType.BIRCH);

        // Pure birch
        assertThat(map.get("birch_forest")).containsExactly(TreeType.BIRCH);

        // Dark forest
        assertThat(map.get("dark_forest")).containsExactly(TreeType.DARK_OAK, TreeType.OAK);

        // Taiga / cold
        assertThat(map.get("taiga")).containsExactly(TreeType.SPRUCE);
        assertThat(map.get("snowy_taiga")).containsExactly(TreeType.SPRUCE);
        assertThat(map.get("old_growth_pine_taiga")).containsExactly(TreeType.SPRUCE);
        assertThat(map.get("old_growth_spruce_taiga")).containsExactly(TreeType.SPRUCE);
        assertThat(map.get("grove")).containsExactly(TreeType.SPRUCE);

        // Jungle
        assertThat(map.get("jungle")).containsExactly(TreeType.JUNGLE);
        assertThat(map.get("bamboo_jungle")).containsExactly(TreeType.JUNGLE);
        assertThat(map.get("sparse_jungle")).containsExactly(TreeType.JUNGLE);

        // Open / grassy
        assertThat(map.get("plains")).containsExactly(TreeType.OAK);
        assertThat(map.get("sunflower_plains")).containsExactly(TreeType.OAK);
        assertThat(map.get("meadow")).containsExactly(TreeType.OAK);
        assertThat(map.get("cherry_grove")).containsExactly(TreeType.CHERRY);
        assertThat(map.get("mangrove_swamp")).containsExactly(TreeType.MANGROVE);

        // Windswept
        assertThat(map.get("windswept_hills")).containsExactly(TreeType.OAK, TreeType.SPRUCE);

        // Savanna
        assertThat(map.get("savanna")).containsExactly(TreeType.ACACIA);
        assertThat(map.get("windswept_savanna")).containsExactly(TreeType.ACACIA);

        // New entries
        assertThat(map.get("swamp")).containsExactly(TreeType.OAK, TreeType.MANGROVE);
        assertThat(map.get("pale_garden")).containsExactly(TreeType.PALE_OAK);
        assertThat(map.get("mushroom_fields")).isEmpty();
    }

    @DisplayName("defaults() uses BiomeTreeConfig.defaults() for all entries")
    @Test
    void defaults_usesDefaultConfigForAll() {
        var registry = TreeRegistry.defaults();
        var defaultConfig = BiomeTreeConfig.defaults();

        for (var biomeId : registry.biomeTreeMap().keySet()) {
            assertThat(registry.configForBiome(biomeId))
                    .as("config for %s", biomeId)
                    .isEqualTo(defaultConfig);
        }
    }

    @DisplayName("defaults() entries all use unmodifiable lists")
    @Test
    void defaults_entriesAreUnmodifiable() {
        var map = TreeRegistry.defaults().biomeTreeMap();

        assertThatThrownBy(() -> map.get("forest").add(TreeType.SPRUCE))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @DisplayName("defaults() speciesForBiome matches biomeTreeMap() values")
    @Test
    void defaults_speciesForBiomeMatchesMap() {
        var registry = TreeRegistry.defaults();
        var map = registry.biomeTreeMap();

        for (var entry : map.entrySet()) {
            assertThat(registry.speciesForBiome(entry.getKey()))
                    .as("speciesForBiome for %s", entry.getKey())
                    .containsExactlyElementsOf(entry.getValue());
        }
    }

    // ──────────────────────────────────────────────
    //  Edge cases
    // ──────────────────────────────────────────────

    @DisplayName("Empty builder produces empty registry")
    @Test
    void emptyBuilder_producesEmptyRegistry() {
        var registry = new TreeRegistry.Builder().build();

        assertThat(registry.biomeTreeMap()).isEmpty();
        assertThat(registry.speciesForBiome("anything")).isEmpty();
        assertThat(registry.configForBiome("anything")).isEqualTo(BiomeTreeConfig.defaults());
    }

    @DisplayName("Builder chaining works")
    @Test
    void builder_chainingWorks() {
        var registry = new TreeRegistry.Builder()
                .putBiomeTreeOverride("a", List.of(TreeType.OAK))
                .putBiomeTreeOverride("b", List.of(TreeType.BIRCH))
                .putBiomeTreeOverride("c", List.of(TreeType.SPRUCE))
                .build();

        assertThat(registry.biomeTreeMap()).hasSize(3);
        assertThat(registry.speciesForBiome("a")).containsExactly(TreeType.OAK);
        assertThat(registry.speciesForBiome("b")).containsExactly(TreeType.BIRCH);
        assertThat(registry.speciesForBiome("c")).containsExactly(TreeType.SPRUCE);
    }
}
