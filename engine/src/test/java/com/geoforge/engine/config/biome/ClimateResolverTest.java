package com.geoforge.engine.config.biome;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("ClimateResolver — envelope-based biome resolution")
class ClimateResolverTest {

    private static final ClimateResolver.ClimateConfig CFG =
            new ClimateResolver.ClimateConfig(0.001, 0.005, 0.001);

    @Test
    @DisplayName("exportFromLegacyTable produces 256 envelopes")
    void export_produces256Envelopes() {
        var envelopes = ClimateResolver.exportFromLegacyTable();
        assertEquals(256, envelopes.size(), "Should export 256 envelopes (8×8×4)");
    }

    @Test
    @DisplayName("resolve returns defaultBiome when no envelope matches")
    void resolve_returnsDefault_whenNoMatch() {
        var resolver = new ClimateResolver(CFG, List.of(), "ocean");
        assertEquals("ocean", resolver.resolve(-999, -999, -999));
    }

    @Test
    @DisplayName("resolve picks higher priority envelope over lower")
    void resolve_picksHigherPriority() {
        var envelopes = List.of(
                new ClimateResolver.BiomeEnvelope("low", -1, 1, 0, 1, 0, 1, 5),
                new ClimateResolver.BiomeEnvelope("high", -1, 1, 0, 1, 0, 1, 10));
        var resolver = new ClimateResolver(CFG, envelopes, "ocean");
        assertEquals("high", resolver.resolve(0, 0.5, 0.5));
    }

    @Test
    @DisplayName("resolve matches valid temperature/humidity/continentalness")
    void resolve_matchesValidValues() {
        var envelopes = List.of(
                new ClimateResolver.BiomeEnvelope("plains",
                        -0.2, 0.4, 0.2, 0.7, 0.5, 0.7, 10));
        var resolver = new ClimateResolver(CFG, envelopes, "ocean");
        assertEquals("plains", resolver.resolve(0.1, 0.45, 0.6));
    }

    @Test
    @DisplayName("resolve returns default when outside all envelopes")
    void resolve_outsideEnvelope_returnsDefault() {
        var envelopes = List.of(
                new ClimateResolver.BiomeEnvelope("plains",
                        -0.2, 0.4, 0.2, 0.7, 0.5, 0.7, 10));
        var resolver = new ClimateResolver(CFG, envelopes, "ocean");
        assertEquals("ocean", resolver.resolve(0.9, 0.45, 0.6));
    }

    @Test
    @DisplayName("exported envelopes match BiomeLookupTable for all 64 grid positions at inland tier")
    void exportedEnvelopes_matchInlandTier() {
        var envelopes = ClimateResolver.exportFromLegacyTable();
        var resolver = new ClimateResolver(CFG, envelopes, "ocean");

        // Temp bands (8 equal bands from -1.0 to 1.0):
        // ti=0: [-1.0, -0.75), center -0.875
        // ti=1: [-0.75, -0.5), center -0.625
        // ti=2: [-0.5, -0.25), center -0.375
        // ti=3: [-0.25, 0.0), center -0.125
        // ti=4: [0.0, 0.25), center 0.125
        // ti=5: [0.25, 0.5), center 0.375
        // ti=6: [0.5, 0.75), center 0.625
        // ti=7: [0.75, 1.0], center 0.875
        double[] tempCenters = {-0.875, -0.625, -0.375, -0.125, 0.125, 0.375, 0.625, 0.875};

        // Humidity bands (8 equal bands from 0.0 to 1.0):
        // hi=0: [0.0, 0.125), center 0.0625
        // hi=1: [0.125, 0.25), center 0.1875
        // hi=2: [0.25, 0.375), center 0.3125
        // hi=3: [0.375, 0.5), center 0.4375
        // hi=4: [0.5, 0.625), center 0.5625
        // hi=5: [0.625, 0.75), center 0.6875
        // hi=6: [0.75, 0.875), center 0.8125
        // hi=7: [0.875, 1.0], center 0.9375
        double[] humCenters = {0.0625, 0.1875, 0.3125, 0.4375, 0.5625, 0.6875, 0.8125, 0.9375};

        String[][] expectedGrid = {
            {"snowy_plains", "ice_spikes", "frozen_peaks", "grove",
             "snowy_taiga", "jagged_peaks", "frozen_ocean", "deep_frozen_ocean"},
            {"snowy_taiga", "taiga", "old_growth_pine_taiga", "snowy_beach",
             "stony_shore", "cold_ocean", "deep_cold_ocean", "frozen_ocean"},
            {"windswept_hills", "windswept_forest", "taiga", "old_growth_spruce_taiga",
             "birch_forest", "ocean", "deep_ocean", "cold_ocean"},
            {"meadow", "forest", "birch_forest", "dark_forest",
             "plains", "ocean", "deep_ocean", "lukewarm_ocean"},
            {"meadow", "pale_garden", "old_growth_birch_forest", "dark_forest",
             "cherry_grove", "stony_shore", "plains", "deep_lukewarm_ocean"},
            {"savanna", "savanna", "windswept_savanna", "jungle",
             "bamboo_jungle", "beach", "mangrove_swamp", "deep_lukewarm_ocean"},
            {"desert", "windswept_savanna", "badlands", "jungle",
             "sparse_jungle", "beach", "mangrove_swamp", "lukewarm_ocean"},
            {"desert", "sparse_jungle", "badlands", "bamboo_jungle",
             "mushroom_fields", "mushroom_fields", "warm_ocean", "warm_ocean"}
        };

        for (int ti = 0; ti < 8; ti++) {
            for (int hi = 0; hi < 8; hi++) {
                double temp = tempCenters[ti];
                double hum = humCenters[hi];
                String expected = expectedGrid[ti][hi];
                String actual = resolver.resolve(temp, hum, 0.6);
                assertEquals(expected, actual,
                        "Mismatch at ti=" + ti + " hi=" + hi
                        + " temp=" + temp + " hum=" + hum);
            }
        }
    }

    @Test
    @DisplayName("exported envelopes match at ocean tier")
    void exportedEnvelopes_matchOceanTier() {
        var envelopes = ClimateResolver.exportFromLegacyTable();
        var resolver = new ClimateResolver(CFG, envelopes, "ocean");

        // Test with center-of-band values at continentalness=0.15 (ocean)
        assertEquals("deep_frozen_ocean", resolver.resolve(-0.875, 0.0625, 0.15));
        assertEquals("frozen_ocean", resolver.resolve(-0.875, 0.75, 0.15));
        assertEquals("deep_cold_ocean", resolver.resolve(-0.625, 0.0625, 0.15));
        assertEquals("cold_ocean", resolver.resolve(-0.625, 0.75, 0.15));
        assertEquals("deep_ocean", resolver.resolve(-0.375, 0.0625, 0.15));
        assertEquals("ocean", resolver.resolve(-0.375, 0.75, 0.15));
        assertEquals("deep_lukewarm_ocean", resolver.resolve(0.125, 0.0625, 0.15));
        assertEquals("lukewarm_ocean", resolver.resolve(0.125, 0.75, 0.15));
        assertEquals("warm_ocean", resolver.resolve(0.875, 0.0625, 0.15));
    }

    @Test
    @DisplayName("exported envelopes match at coast tier")
    void exportedEnvelopes_matchCoastTier() {
        var envelopes = ClimateResolver.exportFromLegacyTable();
        var resolver = new ClimateResolver(CFG, envelopes, "ocean");

        // coast at continentalness=0.4 using band centers
        assertEquals("snowy_beach", resolver.resolve(-0.875, 0.5, 0.4));
        assertEquals("stony_shore", resolver.resolve(-0.125, 0.5, 0.4));
        assertEquals("beach", resolver.resolve(0.875, 0.5, 0.4));
    }

    @Test
    @DisplayName("exported envelopes match at highland tier")
    void exportedEnvelopes_matchHighlandTier() {
        var envelopes = ClimateResolver.exportFromLegacyTable();
        var resolver = new ClimateResolver(CFG, envelopes, "ocean");

        // highland at continentalness=0.8 using band centers
        assertEquals("frozen_peaks", resolver.resolve(-0.875, 0.0625, 0.8));
        assertEquals("grove", resolver.resolve(-0.625, 0.5, 0.8));
        assertEquals("meadow", resolver.resolve(-0.125, 0.0625, 0.8));
        assertEquals("cherry_grove", resolver.resolve(0.125, 0.9375, 0.8));
        assertEquals("windswept_hills", resolver.resolve(0.375, 0.0625, 0.8));
        assertEquals("badlands", resolver.resolve(0.875, 0.9375, 0.8));
    }
}
