package com.geoforge.engine.biome;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

class BiomeLookupTableTest {

    private static final Set<String> VALID_BIOMES =
            Set.of(
                    "plains", "forest", "birch_forest", "dark_forest", "taiga", "snowy_taiga",
                    "meadow", "cherry_grove", "savanna", "badlands", "jungle", "bamboo_jungle",
                    "sparse_jungle", "desert", "snowy_plains", "ice_spikes", "grove",
                    "jagged_peaks", "frozen_peaks", "stony_peaks", "windswept_hills",
                    "windswept_gravelly_hills", "windswept_forest", "windswept_savanna",
                    "old_growth_pine_taiga", "old_growth_spruce_taiga", "old_growth_birch_forest",
                    "beach", "snowy_beach", "stony_shore", "mushroom_fields",
                    "ocean", "deep_ocean", "cold_ocean", "deep_cold_ocean",
                    "lukewarm_ocean", "deep_lukewarm_ocean", "frozen_ocean", "deep_frozen_ocean",
                    "warm_ocean");

    @Test
    void lookup_midTempMidHumidity_returnsNonNull() {
        String biome = BiomeLookupTable.lookup(0.0, 0.5);
        assertNotNull(biome);
        assertTrue(VALID_BIOMES.contains(biome), "Unknown biome: " + biome);
    }

    @Test
    void lookup_coldDry_returnsNonNull() {
        String biome = BiomeLookupTable.lookup(-1.0, 0.0);
        assertNotNull(biome);
        assertTrue(VALID_BIOMES.contains(biome), "Unknown biome: " + biome);
    }

    @Test
    void lookup_hotWet_returnsNonNull() {
        String biome = BiomeLookupTable.lookup(1.0, 1.0);
        assertNotNull(biome);
        assertTrue(VALID_BIOMES.contains(biome), "Unknown biome: " + biome);
    }

    @Test
    void lookup_allCornersValid() {
        assertNotNull(BiomeLookupTable.lookup(-1.0, 0.0));
        assertNotNull(BiomeLookupTable.lookup(-1.0, 1.0));
        assertNotNull(BiomeLookupTable.lookup(1.0, 0.0));
        assertNotNull(BiomeLookupTable.lookup(1.0, 1.0));
    }

    @Test
    void lookup_clampsOutOfRangeValues() {
        // These should not throw
        assertNotNull(BiomeLookupTable.lookup(-2.0, 0.5));
        assertNotNull(BiomeLookupTable.lookup(2.0, 0.5));
        assertNotNull(BiomeLookupTable.lookup(0.0, -0.5));
        assertNotNull(BiomeLookupTable.lookup(0.0, 1.5));
    }

    @Test
    void getAllBiomeIds_returnsNonEmptySet() {
        Set<String> ids = BiomeLookupTable.getAllBiomeIds();
        assertNotNull(ids);
        assertFalse(ids.isEmpty(), "getAllBiomeIds() returned empty set");
        assertTrue(ids.size() > 5, "Expected at least 5 biome IDs");
    }

    @Test
    void getAllBiomeIds_allEntriesAreValid() {
        for (String id : BiomeLookupTable.getAllBiomeIds()) {
            assertTrue(VALID_BIOMES.contains(id), "Invalid biome ID: " + id);
        }
    }

    @Test
    void getAllBiomeIds_isUnmodifiable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> BiomeLookupTable.getAllBiomeIds().add("test"));
    }
}
