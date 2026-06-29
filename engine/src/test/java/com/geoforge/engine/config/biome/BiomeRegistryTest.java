package com.geoforge.engine.config.biome;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("BiomeRegistry — central biome definition registry")
class BiomeRegistryTest {

    private static final ClimateResolver.ClimateConfig CFG =
            new ClimateResolver.ClimateConfig(0.001, 0.005, 0.001);

    @Test
    @DisplayName("empty registry returns defaults for unknown biome")
    void emptyRegistry_returnsDefaults() {
        var resolver = new ClimateResolver(CFG, List.of(), "ocean");
        var result = new BiomeLoadResult(Map.of(), List.of(), List.of());
        var registry = new BiomeRegistry(result, resolver);
        assertTrue(registry.isEmpty());
        assertEquals(0, registry.size());
        var def = registry.forBiome("unknown");
        assertEquals("", def.id());
    }

    @Test
    @DisplayName("registry returns stored biome definition")
    void registry_returnsStoredBiome() {
        var plains = new BiomeDefinition("plains", 0, 1, "", "", 0.5, 1, "", -1, 0, 0,
                Map.of(), List.of(), 0.3, false, -1, 1, 0, 1, 0, 1, 0);
        var result = new BiomeLoadResult(Map.of("plains", plains), List.of(), List.of());
        var resolver = new ClimateResolver(CFG, ClimateResolver.exportFromLegacyTable(), "ocean");
        var registry = new BiomeRegistry(result, resolver);

        assertFalse(registry.isEmpty());
        assertEquals(1, registry.size());
        assertEquals("plains", registry.forBiome("plains").id());
    }

    @Test
    @DisplayName("getAllBiomeIds includes registry and climate biome IDs")
    void getAllBiomeIds_includesBoth() {
        var desert = new BiomeDefinition("desert", 0, 1, "", "", 0.5, 1, "", -1, 0, 0,
                Map.of(), List.of(), 0.3, false, -1, 1, 0, 1, 0, 1, 0);
        var result = new BiomeLoadResult(Map.of("desert", desert), List.of(), List.of());
        var resolver = new ClimateResolver(CFG, ClimateResolver.exportFromLegacyTable(), "ocean");
        var registry = new BiomeRegistry(result, resolver);

        var allIds = registry.getAllBiomeIds();
        assertTrue(allIds.contains("desert"));
        assertTrue(allIds.contains("plains")); // from climate envelopes
    }

    @Test
    @DisplayName("constructor handles null gracefully")
    void constructor_handlesNull() {
        var registry = new BiomeRegistry(null, null);
        assertTrue(registry.isEmpty());
        assertEquals("ocean", registry.climateResolver().defaultBiome());
    }
}
