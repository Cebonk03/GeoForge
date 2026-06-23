package com.geoforge.adapters.v26_x;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

/**
 * Tests for Paper26xAdapter block mapping and Folia detection.
 *
 * <p>Uses the package-private constructor with injected lookup functions. Biome
 * mapping tests are not included because {@code Biome} in Paper 26.x is a
 * registry-backed type whose class initialization requires a live server registry.
 * Biome mapping is verified by the runtime-test workflow on real Paper servers.
 */
class Paper26xAdapterTest {

    private final JavaPlugin mockPlugin = mock(JavaPlugin.class);

    @Test
    void mapBlock_stone_returnsMaterialStone() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> "stone".equals(id) ? Material.STONE : null,
                id -> null);
        assertEquals(Material.STONE, adapter.mapBlock("stone"));
    }

    @Test
    void mapBlock_returnsStone_whenRegistryReturnsNull() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> null,
                id -> null);
        assertEquals(Material.STONE, adapter.mapBlock("unknown_block_id"));
    }

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
        assertEquals(Material.STONE, adapter.mapBlock("stone"));
        assertEquals(Material.DIRT, adapter.mapBlock("dirt"));
        assertEquals(Material.GRASS_BLOCK, adapter.mapBlock("grass_block"));
        assertEquals(Material.STONE, adapter.mapBlock("nonexistent"));
    }

    @Test
    void isFolia_returnsFalse() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> Material.STONE,
                id -> null);
        assertFalse(adapter.isFolia());
    }
}
