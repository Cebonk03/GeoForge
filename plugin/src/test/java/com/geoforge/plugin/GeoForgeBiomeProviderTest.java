package com.geoforge.plugin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.GeoForgeConfig;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.Set;

class GeoForgeBiomeProviderTest {

    private ServerMock server;
    private GeoForgeBiomeProvider biomeProvider;

    @Nested
    class MockitoTests {
        @Test
        void getBiomes_returnsAllFromEngine() {
            var engine = mock(GeoForgeEngine.class);
            var adapter = mock(GeoForgeAdapter.class);
            when(engine.getAllBiomeIds()).thenReturn(Set.of("plains", "desert"));
            when(adapter.mapBiome("plains")).thenReturn(mock(Biome.class));
            when(adapter.mapBiome("desert")).thenReturn(mock(Biome.class));
            var provider = new GeoForgeBiomeProvider(adapter, engine);
            assertEquals(2, provider.getBiomes(null).size());
        }

        @Test
        void getBiome_usesEngineAndAdapter() {
            var engine = mock(GeoForgeEngine.class);
            var adapter = mock(GeoForgeAdapter.class);
            var expectedBiome = mock(Biome.class);
            when(engine.getBiomeId(100, 64, 200)).thenReturn("desert");
            when(adapter.mapBiome("desert")).thenReturn(expectedBiome);
            var provider = new GeoForgeBiomeProvider(adapter, engine);
            assertSame(expectedBiome, provider.getBiome(null, 100, 64, 200));
            verify(engine).getBiomeId(100, 64, 200);
            verify(adapter).mapBiome("desert");
        }
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        GeoForgeAdapter adapter =
                new com.geoforge.adapters.v1_21_x.Paper1_21_xAdapter(
                        MockBukkit.createMockPlugin());
        GeoForgeEngine engine = new GeoForgeEngine(42L, GeoForgeConfig.defaults());
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
