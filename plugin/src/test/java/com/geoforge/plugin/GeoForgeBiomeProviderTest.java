package com.geoforge.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class GeoForgeBiomeProviderTest {

    private ServerMock server;
    private GeoForgeBiomeProvider biomeProvider;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        GeoForgeAdapter adapter =
                new com.geoforge.adapters.v1_21_x.Paper1_21_xAdapter(
                        MockBukkit.createMockPlugin());
        GeoForgeEngine engine = new GeoForgeEngine(42L);
        biomeProvider = new GeoForgeBiomeProvider(adapter, engine);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void getBiomes_returnsNonEmpty() {
        var biomes = biomeProvider.getBiomes(null);
        assertNotNull(biomes);
        assertFalse(biomes.isEmpty(), "getBiomes() should return non-empty list");
    }

    @Test
    void getBiome_returnsNonNull() {
        Biome biome = biomeProvider.getBiome(null, 0, 63, 0);
        assertNotNull(biome);
    }

    @Test
    void getBiome_variousPositions_allNonNull() {
        for (int x = -50; x <= 50; x += 25) {
            for (int z = -50; z <= 50; z += 25) {
                Biome biome = biomeProvider.getBiome(null, x, 63, z);
                assertNotNull(biome, "Biome should not be null at (" + x + ",63," + z + ")");
            }
        }
    }
}
