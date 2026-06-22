package com.geoforge.adapters.v1_21_x;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

class Paper1_21_xAdapterTest {

    private ServerMock server;
    private JavaPlugin plugin;
    private Paper1_21_xAdapter adapter;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        adapter = new Paper1_21_xAdapter(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void mapBlock_stone_returnsMaterialStone() {
        assertEquals(Material.STONE, adapter.mapBlock("stone"));
    }

    @Test
    void mapBlock_grassBlock_returnsMaterialGrassBlock() {
        assertEquals(Material.GRASS_BLOCK, adapter.mapBlock("grass_block"));
    }

    @Test
    void mapBlock_unknown_returnsStoneFallback() {
        assertEquals(Material.STONE, adapter.mapBlock("__nonexistent_geoforge_internal__"));
    }

    @Test
    void mapBiome_plains_returnsNonNull() {
        Biome biome = adapter.mapBiome("plains");
        assertNotNull(biome);
    }

    @Test
    void mapBiome_cherryGrove_returnsNonNull() {
        Biome biome = adapter.mapBiome("cherry_grove");
        assertNotNull(biome);
    }

    @Test
    void mapBiome_unknown_returnsPlainsFallback() {
        Biome biome = adapter.mapBiome("__nonexistent_biome__");
        assertNotNull(biome);
    }

    @Test
    void isFolia_returnsFalse() {
        assertFalse(adapter.isFolia());
    }
}
