package com.geoforge.adapters.v1_21_x;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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

    // ---- Mockito-based tests (no MockBukkit needed) ----

    private final JavaPlugin mockPlugin = mock(JavaPlugin.class);

    @Test
    void mapBlock_stone_returnsMaterialStone_mockito() {
        var adapter = new Paper1_21_xAdapter(mockPlugin,
                id -> "stone".equals(id) ? Material.STONE : null,
                id -> null);
        assertEquals(Material.STONE, adapter.mapBlock("stone"));
    }

    @Test
    void mapBlock_unknown_returnsStoneFallback_mockito() {
        var adapter = new Paper1_21_xAdapter(mockPlugin,
                id -> null,
                id -> null);
        assertEquals(Material.STONE, adapter.mapBlock("__unknown__"));
    }

    @Test
    void mapBiome_unknown_returnsPlainsFallback_mockito() {
        var plains = mock(Biome.class);
        var adapter = new Paper1_21_xAdapter(mockPlugin,
                id -> null,
                id -> "plains".equals(id) ? plains : null);
        assertSame(plains, adapter.mapBiome("__unknown__"));
    }

    @Test
    void mapBiome_plainsMissing_throws_mockito() {
        var adapter = new Paper1_21_xAdapter(mockPlugin,
                id -> null,
                id -> null);
        assertThrows(IllegalStateException.class,
                () -> adapter.mapBiome("__unknown__"));
    }

    @Test
    void isFolia_returnsFalse_mockito() {
        var adapter = new Paper1_21_xAdapter(mockPlugin,
                id -> Material.STONE,
                id -> null);
        assertFalse(adapter.isFolia());
    }

    // ---- MockBukkit-based tests ----

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
