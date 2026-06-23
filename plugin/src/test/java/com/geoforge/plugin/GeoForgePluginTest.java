package com.geoforge.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import org.bukkit.generator.ChunkGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class GeoForgePluginTest {

    private ServerMock server;
    private GeoForgePlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(GeoForgePlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void pluginLoads_successfully() {
        assertNotNull(plugin);
        assertTrue(plugin.isEnabled());
    }

    @Test
    void onEnable_createsAdapter() {
        // After onEnable(), the plugin should have an adapter and engine
        // These are private, so we verify indirectly via getDefaultWorldGenerator
        assertNotNull(plugin.getDefaultWorldGenerator("world", null));
    }

    @Test
    void getDefaultWorldGenerator_returnsGeoForgeGenerator() {
        ChunkGenerator generator = plugin.getDefaultWorldGenerator("world", null);
        assertNotNull(generator);
        assertInstanceOf(GeoForgeGenerator.class, generator);
    }

    @Test
    void getDefaultWorldGenerator_withNullId_returnsGenerator() {
        ChunkGenerator generator = plugin.getDefaultWorldGenerator("test_world", null);
        assertNotNull(generator);
        assertInstanceOf(GeoForgeGenerator.class, generator);
    }

    @Test
    void getDefaultWorldGenerator_returnsNonNullOnRepeatedCalls() {
        ChunkGenerator g1 = plugin.getDefaultWorldGenerator("world", null);
        ChunkGenerator g2 = plugin.getDefaultWorldGenerator("world", null);
        assertNotNull(g1);
        assertNotNull(g2);
        // Generator is created fresh per call (not cached by the plugin)
        // but should always be non-null
    }
}
