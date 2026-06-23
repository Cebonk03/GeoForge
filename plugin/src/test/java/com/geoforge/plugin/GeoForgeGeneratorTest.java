package com.geoforge.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.GeoForgeConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

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
        generator = new GeoForgeGenerator(adapter, engine);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void shouldGenerateMethods_correctValues() {
        assertFalse(generator.shouldGenerateNoise());
        assertFalse(generator.shouldGenerateSurface());
        assertTrue(generator.shouldGenerateBedrock());
        assertTrue(generator.shouldGenerateCaves());
        assertTrue(generator.shouldGenerateDecorations());
        assertTrue(generator.shouldGenerateStructures());
        assertTrue(generator.shouldGenerateMobs());
    }

    @Test
    void getDefaultBiomeProvider_returnsNonNull() {
        var provider = generator.getDefaultBiomeProvider(null);
        assertNotNull(provider);
        assertInstanceOf(GeoForgeBiomeProvider.class, provider);
    }
}
