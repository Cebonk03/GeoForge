package com.geoforge.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.GeoForgeConfig;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.Set;

@Tag("integration")
@DisplayName("GeoForgeBiomeProvider tests")
class GeoForgeBiomeProviderTest {

    private ServerMock server;
    private GeoForgeBiomeProvider biomeProvider;

    @Nested
    @DisplayName("Mockito-based tests")
    class MockitoTests {

        @DisplayName("getBiomes returns all biomes from engine")
        @Test
        void getBiomes_returnsAllFromEngine() {
            var engine = mock(GeoForgeEngine.class);
            var adapter = mock(GeoForgeAdapter.class);
            when(engine.getAllBiomeIds()).thenReturn(Set.of("plains", "desert"));
            when(adapter.mapBiome("plains")).thenReturn(mock(Biome.class));
            when(adapter.mapBiome("desert")).thenReturn(mock(Biome.class));
            var provider = new GeoForgeBiomeProvider(adapter, engine);
            assertThat(provider.getBiomes(null)).hasSize(2);
        }

        @DisplayName("getBiome delegates to engine and adapter")
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

    @DisplayName("getBiomes returns non-empty list")
    @Test
    void getBiomes_returnsNonEmpty() {
        var biomes = biomeProvider.getBiomes(null);
        assertThat(biomes).isNotNull().isNotEmpty();
    }

    @DisplayName("getBiome returns non-null biome")
    @Test
    void getBiome_returnsNonNull() {
        Biome biome = biomeProvider.getBiome(null, 0, 63, 0);
        assertThat(biome).isNotNull();
    }

    @DisplayName("getBiome returns non-null for all positions")
    @Test
    void getBiome_variousPositions_allNonNull() {
        for (int x = -50; x <= 50; x += 25) {
            for (int z = -50; z <= 50; z += 25) {
                Biome biome = biomeProvider.getBiome(null, x, 63, z);
                assertThat(biome).isNotNull();
            }
        }
    }
}
