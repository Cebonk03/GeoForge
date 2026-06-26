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
                    "warm_ocean",
                    "mangrove_swamp");

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

    @Test
    void noBiomeAppearsMoreThanTwice() {
        var counts = new java.util.HashMap<String, Integer>();
        for (int ti = 0; ti < 8; ti++) {
            for (int hi = 0; hi < 8; hi++) {
                double temp = -1.0 + ti * 0.25 + 0.125;
                double hum = hi * 0.125 + 0.0625;
                String biome = BiomeLookupTable.lookup(temp, hum);
                counts.merge(biome, 1, Integer::sum);
            }
        }
        for (var entry : counts.entrySet()) {
            assertTrue(entry.getValue() <= 2,
                "Biome '" + entry.getKey() + "' appears " + entry.getValue() + " times (>2)");
        }
    }

    @Test
    void mangroveSwamp_isPresent() {
        assertTrue(BiomeLookupTable.getAllBiomeIds().contains("mangrove_swamp"));
    }

    @Test
    void lookup_threeParam_backwardCompatible() {
        for (int ti = 0; ti < 8; ti++) {
            for (int hi = 0; hi < 8; hi++) {
                double temp = -1.0 + ti * 0.25 + 0.125;
                double hum = hi * 0.125 + 0.0625;
                String twoParam = BiomeLookupTable.lookup(temp, hum);
                String threeParam = BiomeLookupTable.lookup(temp, hum, 0.6);
                assertEquals(twoParam, threeParam,
                        "3-param with c=0.6 must match 2-param at temp=" + temp + ", hum=" + hum);
            }
        }
    }

    @Test
    void lookup_oceanContinentalness_returnsOceanBiome() {
        var oceanBiomes = Set.of(
                "frozen_ocean", "deep_frozen_ocean",
                "cold_ocean", "deep_cold_ocean",
                "ocean", "deep_ocean",
                "lukewarm_ocean", "deep_lukewarm_ocean",
                "warm_ocean");
        for (int ti = 0; ti < 8; ti++) {
            for (int hi = 0; hi < 8; hi++) {
                double temp = -1.0 + ti * 0.25 + 0.125;
                double hum = hi * 0.125 + 0.0625;
                String biome = BiomeLookupTable.lookup(temp, hum, 0.15);
                assertTrue(oceanBiomes.contains(biome),
                        "Ocean c=0.15 should return ocean biome, got '" + biome
                                + "' at temp=" + temp + ", hum=" + hum);
            }
        }
    }

    @Test
    void lookup_coastContinentalness_returnsCoastBiome() {
        var coastBiomes = Set.of("beach", "snowy_beach", "stony_shore");
        for (int ti = 0; ti < 8; ti++) {
            for (int hi = 0; hi < 8; hi++) {
                double temp = -1.0 + ti * 0.25 + 0.125;
                double hum = hi * 0.125 + 0.0625;
                String biome = BiomeLookupTable.lookup(temp, hum, 0.4);
                assertTrue(coastBiomes.contains(biome),
                        "Coast c=0.4 should return coast biome, got '" + biome
                                + "' at temp=" + temp + ", hum=" + hum);
            }
        }
    }

    @Test
    void lookup_inlandContinentalness_matchesBaseLookup() {
        for (int ti = 0; ti < 8; ti++) {
            for (int hi = 0; hi < 8; hi++) {
                double temp = -1.0 + ti * 0.25 + 0.125;
                double hum = hi * 0.125 + 0.0625;
                String expected = BiomeLookupTable.lookup(temp, hum);
                String actual = BiomeLookupTable.lookup(temp, hum, 0.6);
                assertEquals(expected, actual,
                        "Inland c=0.6 must match base lookup at temp=" + temp + ", hum=" + hum);
            }
        }
    }

    @Test
    void lookup_highlandContinentalness_returnsHighlandBiome() {
        var highlandBiomes = Set.of(
                "frozen_peaks", "jagged_peaks",
                "grove",
                "meadow", "cherry_grove",
                "windswept_hills", "windswept_forest",
                "windswept_savanna",
                "badlands");
        for (int ti = 0; ti < 8; ti++) {
            for (int hi = 0; hi < 8; hi++) {
                double temp = -1.0 + ti * 0.25 + 0.125;
                double hum = hi * 0.125 + 0.0625;
                String biome = BiomeLookupTable.lookup(temp, hum, 0.85);
                assertTrue(highlandBiomes.contains(biome),
                        "Highland c=0.85 should return highland biome, got '" + biome
                                + "' at temp=" + temp + ", hum=" + hum);
            }
        }
    }

    @Test
    void lookup_deterministic_withContinentalness() {
        String first = BiomeLookupTable.lookup(0.3, 0.7, 0.9);
        for (int i = 0; i < 10; i++) {
            assertEquals(first, BiomeLookupTable.lookup(0.3, 0.7, 0.9),
                    "Same continentalness inputs must be deterministic");
        }
    }

    @Test
    void lookup_continentalnessBoundaries() {
        assertNotNull(BiomeLookupTable.lookup(0.0, 0.5, 0.0));
        assertNotNull(BiomeLookupTable.lookup(0.0, 0.5, 0.3));
        assertNotNull(BiomeLookupTable.lookup(0.0, 0.5, 0.5));
        assertNotNull(BiomeLookupTable.lookup(0.0, 0.5, 0.7));
        assertNotNull(BiomeLookupTable.lookup(0.0, 0.5, 1.0));
    }
}
