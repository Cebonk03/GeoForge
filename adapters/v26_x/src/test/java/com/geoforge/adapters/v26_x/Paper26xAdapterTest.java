package com.geoforge.adapters.v26_x;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for Paper26xAdapter block mapping and Folia detection.
 *
 * <p>Uses the package-private constructor with injected lookup functions. Biome
 * mapping tests are not included because {@code Biome} in Paper 26.x is a
 * registry-backed type whose class initialization requires a live server registry.
 * Biome mapping is verified by the runtime-test workflow on real Paper servers.
 */
@Tag("unit")
@DisplayName("Paper 26.x adapter tests")
class Paper26xAdapterTest {

    private final JavaPlugin mockPlugin = mock(JavaPlugin.class);

    @DisplayName("mapBlock('stone') returns Material.STONE")
    @Test
    void mapBlock_stone_returnsMaterialStone() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> "stone".equals(id) ? Material.STONE : null,
                id -> null);
        assertThat(adapter.mapBlock("stone")).isEqualTo(Material.STONE);
    }

    @DisplayName("mapBlock unknown returns stone when registry returns null")
    @Test
    void mapBlock_returnsStone_whenRegistryReturnsNull() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> null,
                id -> null);
        assertThat(adapter.mapBlock("unknown_block_id")).isEqualTo(Material.STONE);
    }

    @DisplayName("mapBlock returns correct materials by ID")
    @Test
    void mapBlock_differentMaterials() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> switch (id) {
                    case "stone" -> Material.STONE;
                    case "dirt" -> Material.DIRT;
                    case "grass_block" -> Material.GRASS_BLOCK;
                    default -> null;
                },
                id -> null);
        assertThat(adapter.mapBlock("stone")).isEqualTo(Material.STONE);
        assertThat(adapter.mapBlock("dirt")).isEqualTo(Material.DIRT);
        assertThat(adapter.mapBlock("grass_block")).isEqualTo(Material.GRASS_BLOCK);
        assertThat(adapter.mapBlock("nonexistent")).isEqualTo(Material.STONE);
    }

    @DisplayName("isFolia returns false in test environment")
    @Test
    void isFolia_returnsFalse() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> Material.STONE,
                id -> null);
        assertThat(adapter.isFolia()).isFalse();
    }
}
