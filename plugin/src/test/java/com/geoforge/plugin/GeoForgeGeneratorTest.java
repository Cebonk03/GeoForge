package com.geoforge.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.GeoForgeConfig;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

@Tag("integration")
@DisplayName("GeoForgeGenerator tests")
class GeoForgeGeneratorTest {

    private ServerMock server;
    private GeoForgeGenerator generator;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        GeoForgeAdapter adapter =
                new com.geoforge.adapters.v1_21_x.Paper1_21_xAdapter(
                        MockBukkit.createMockPlugin());
        GeoForgeEngine engine = new GeoForgeEngine(42L, GeoForgeConfig.defaults());
        generator = new GeoForgeGenerator(adapter, engine, 42L);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @DisplayName("Generator pass-through flags match expected values")
    @Test
    void shouldGenerateMethods_correctValues() {
        assertFalse(generator.shouldGenerateNoise());
        assertFalse(generator.shouldGenerateSurface());
        assertFalse(generator.shouldGenerateBedrock());
        assertFalse(generator.shouldGenerateCaves());
        assertFalse(generator.shouldGenerateDecorations());
        assertTrue(generator.shouldGenerateStructures());
        assertTrue(generator.shouldGenerateMobs());
    }

    @DisplayName("getDefaultBiomeProvider returns non-null provider")
    @Test
    void getDefaultBiomeProvider_returnsNonNull() {
        var provider = generator.getDefaultBiomeProvider(null);
        assertThat(provider).isNotNull().isInstanceOf(GeoForgeBiomeProvider.class);
    }
}
