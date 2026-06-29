package com.geoforge.engine.biome;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("unit")
@DisplayName("Biome lookup table tests")
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
                    "pale_garden",
                    "mangrove_swamp");

    @DisplayName("Mid temp, mid humidity lookup returns a known biome")
    @Test
    void lookup_midTempMidHumidity_returnsNonNull() {
        String biome = BiomeLookupTable.lookup(0.0, 0.5);
        assertNotNull(biome);
        assertThat(VALID_BIOMES).contains(biome);
    }

    @DisplayName("Cold and dry lookup returns a known biome")
    @Test
    void lookup_coldDry_returnsNonNull() {
        String biome = BiomeLookupTable.lookup(-1.0, 0.0);
        assertNotNull(biome);
        assertThat(VALID_BIOMES).contains(biome);
    }

    @DisplayName("Hot and wet lookup returns a known biome")
    @Test
    void lookup_hotWet_returnsNonNull() {
        String biome = BiomeLookupTable.lookup(1.0, 1.0);
        assertNotNull(biome);
        assertThat(VALID_BIOMES).contains(biome);
    }

    @DisplayName("All four corners return valid biomes")
    @Test
    void lookup_allCornersValid() {
        assertNotNull(BiomeLookupTable.lookup(-1.0, 0.0));
        assertNotNull(BiomeLookupTable.lookup(-1.0, 1.0));
        assertNotNull(BiomeLookupTable.lookup(1.0, 0.0));
        assertNotNull(BiomeLookupTable.lookup(1.0, 1.0));
    }

    @DisplayName("Out-of-range values are clamped without throwing")
    @Test
    void lookup_clampsOutOfRangeValues() {
        assertNotNull(BiomeLookupTable.lookup(-2.0, 0.5));
        assertNotNull(BiomeLookupTable.lookup(2.0, 0.5));
        assertNotNull(BiomeLookupTable.lookup(0.0, -0.5));
        assertNotNull(BiomeLookupTable.lookup(0.0, 1.5));
    }

    @DisplayName("getAllBiomeIds returns non-empty set with sufficient biomes")
    @Test
    void getAllBiomeIds_returnsNonEmptySet() {
        Set<String> ids = BiomeLookupTable.getAllBiomeIds();
        assertThat(ids).isNotEmpty();
        assertThat(ids.size()).isGreaterThan(5);
    }

    @DisplayName("All biome IDs returned are valid entries")
    @Test
    void getAllBiomeIds_allEntriesAreValid() {
        for (String id : BiomeLookupTable.getAllBiomeIds()) {
            assertThat(VALID_BIOMES).contains(id);
        }
    }

    @DisplayName("getAllBiomeIds returns an unmodifiable set")
    @Test
    void getAllBiomeIds_isUnmodifiable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> BiomeLookupTable.getAllBiomeIds().add("test"));
    }

    @DisplayName("No biome appears more than twice in the 8x8 grid")
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
            assertThat(entry.getValue())
                    .as("Biome '%s' frequency", entry.getKey())
                    .isLessThanOrEqualTo(2);
        }
    }

    @DisplayName("Mangrove swamp is present in biome list")
    @Test
    void mangroveSwamp_isPresent() {
        assertThat(BiomeLookupTable.getAllBiomeIds()).contains("mangrove_swamp");
    }

    @DisplayName("Three-param lookup matches two-param with continentalness=0.6")
    @ParameterizedTest
    @MethodSource("gridPositions")
    void lookup_threeParam_backwardCompatible(double temp, double hum) {
        String twoParam = BiomeLookupTable.lookup(temp, hum);
        String threeParam = BiomeLookupTable.lookup(temp, hum, 0.6);
        assertEquals(twoParam, threeParam,
                "3-param with c=0.6 must match 2-param at temp=" + temp + ", hum=" + hum);
    }

    @DisplayName("Ocean continentalness returns ocean biomes")
    @ParameterizedTest
    @MethodSource("gridPositions")
    void lookup_oceanContinentalness_returnsOceanBiome(double temp, double hum) {
        var oceanBiomes = Set.of(
                "frozen_ocean", "deep_frozen_ocean",
                "cold_ocean", "deep_cold_ocean",
                "ocean", "deep_ocean",
                "lukewarm_ocean", "deep_lukewarm_ocean",
                "warm_ocean");
        String biome = BiomeLookupTable.lookup(temp, hum, 0.15);
        assertThat(oceanBiomes).contains(biome);
    }

    @DisplayName("Coast continentalness returns coast biomes")
    @ParameterizedTest
    @MethodSource("gridPositions")
    void lookup_coastContinentalness_returnsCoastBiome(double temp, double hum) {
        var coastBiomes = Set.of("beach", "snowy_beach", "stony_shore");
        String biome = BiomeLookupTable.lookup(temp, hum, 0.4);
        assertThat(coastBiomes).contains(biome);
    }

    @DisplayName("Inland continentalness matches base lookup")
    @ParameterizedTest
    @MethodSource("gridPositions")
    void lookup_inlandContinentalness_matchesBaseLookup(double temp, double hum) {
        String expected = BiomeLookupTable.lookup(temp, hum);
        String actual = BiomeLookupTable.lookup(temp, hum, 0.6);
        assertEquals(expected, actual,
                "Inland c=0.6 must match base lookup at temp=" + temp + ", hum=" + hum);
    }

    @DisplayName("Highland continentalness returns highland biomes")
    @ParameterizedTest
    @MethodSource("gridPositions")
    void lookup_highlandContinentalness_returnsHighlandBiome(double temp, double hum) {
        var highlandBiomes = Set.of(
                "frozen_peaks", "jagged_peaks",
                "grove",
                "meadow", "cherry_grove",
                "windswept_hills", "windswept_forest",
                "windswept_savanna",
                "badlands");
        String biome = BiomeLookupTable.lookup(temp, hum, 0.85);
        assertThat(highlandBiomes).contains(biome);
    }

    @DisplayName("Same continentalness inputs are deterministic")
    @Test
    void lookup_deterministic_withContinentalness() {
        String first = BiomeLookupTable.lookup(0.3, 0.7, 0.9);
        for (int i = 0; i < 10; i++) {
            assertEquals(first, BiomeLookupTable.lookup(0.3, 0.7, 0.9),
                    "Same continentalness inputs must be deterministic");
        }
    }

    @DisplayName("All continentalness boundary values return valid biomes")
    @Test
    void lookup_continentalnessBoundaries() {
        assertNotNull(BiomeLookupTable.lookup(0.0, 0.5, 0.0));
        assertNotNull(BiomeLookupTable.lookup(0.0, 0.5, 0.3));
        assertNotNull(BiomeLookupTable.lookup(0.0, 0.5, 0.5));
        assertNotNull(BiomeLookupTable.lookup(0.0, 0.5, 0.7));
        assertNotNull(BiomeLookupTable.lookup(0.0, 0.5, 1.0));
    }

    private static Stream<Arguments> gridPositions() {
        var args = new java.util.ArrayList<Arguments>();
        for (int ti = 0; ti < 8; ti++) {
            for (int hi = 0; hi < 8; hi++) {
                double temp = -1.0 + ti * 0.25 + 0.125;
                double hum = hi * 0.125 + 0.0625;
                args.add(Arguments.of(temp, hum));
            }
        }
        return args.stream();
    }
}
