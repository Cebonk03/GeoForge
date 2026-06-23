package com.geoforge.adapters.v26_x;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

/**
 * Tests for Paper26xAdapter using constructor-injected lookup functions.
 *
 * <p>The production constructor uses live Paper 26.x registries (not available in
 * unit tests). These tests use the package-private constructor with mock lookups,
 * avoiding the need for a running server or MockBukkit's registry coupling.
 *
 * <p>Biome references use {@code mock(Biome.class)} rather than {@code
 * Biome.PLAINS} because Paper 26.x changed Biome from an enum to a registry-backed
 * type whose static fields reference a live server registry.
 */
class Paper26xAdapterTest {

    private final JavaPlugin mockPlugin = mock(JavaPlugin.class);
    private final Biome mockPlains = mock(Biome.class);

    @Test
    void mapBlock_stone_returnsMaterialStone() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> "stone".equals(id) ? Material.STONE : null,
                id -> mockPlains);
        assertEquals(Material.STONE, adapter.mapBlock("stone"));
    }

    @Test
    void mapBlock_returnsStone_whenRegistryReturnsNull() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> null,
                id -> mockPlains);
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
                id -> mockPlains);
        assertEquals(Material.STONE, adapter.mapBlock("stone"));
        assertEquals(Material.DIRT, adapter.mapBlock("dirt"));
        assertEquals(Material.GRASS_BLOCK, adapter.mapBlock("grass_block"));
        assertEquals(Material.STONE, adapter.mapBlock("nonexistent"));
    }

    @Test
    void mapBiome_plains_returnsPlains() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> Material.STONE,
                id -> "plains".equals(id) ? mockPlains : null);
        assertSame(mockPlains, adapter.mapBiome("plains"));
    }

    @Test
    void mapBiome_unknown_fallsBackToPlains() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> Material.STONE,
                id -> "plains".equals(id) ? mockPlains : null);
        assertSame(mockPlains, adapter.mapBiome("cherry_grove"));
    }

    @Test
    void isFolia_returnsFalse() {
        var adapter = new Paper26xAdapter(mockPlugin,
                id -> Material.STONE,
                id -> mockPlains);
        assertFalse(adapter.isFolia());
    }
}
