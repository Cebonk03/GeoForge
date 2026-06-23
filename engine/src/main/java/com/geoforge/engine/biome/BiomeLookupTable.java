package com.geoforge.engine.biome;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An 8×8 temperature-humidity lookup table that maps climate noise values to Minecraft biome
 * identifiers.
 *
 * <p>The temperature axis (−1.0 = coldest, 1.0 = hottest) is divided into 8 bands. The
 * humidity axis (0.0 = driest, 1.0 = wettest) is divided into 8 bands. Every cell contains a
 * valid vanilla biome ID that exists in all target versions (1.21.4 through 26.2).
 */
public final class BiomeLookupTable {

    // @formatter:off
    // Rows = temperature (0 cold → 7 hot), Columns = humidity (0 dry → 7 wet)
    private static final String[][] TABLE = {
        // cold/dry → cold/wet
        {"snowy_plains",       "ice_spikes",        "frozen_peaks",      "grove",
         "snowy_taiga",        "jagged_peaks",      "frozen_ocean",      "deep_frozen_ocean"},
        {"snowy_taiga",        "taiga",             "old_growth_pine_taiga", "snowy_beach",
         "stony_shore",        "cold_ocean",        "deep_cold_ocean",   "frozen_ocean"},
        // low/dry → low/wet
        {"windswept_hills",    "windswept_forest",  "taiga",             "old_growth_spruce_taiga",
         "birch_forest",       "ocean",             "deep_ocean",        "cold_ocean"},
        // moderate/dry → moderate/wet
        {"meadow",             "forest",            "birch_forest",      "dark_forest",
         "plains",             "ocean",             "deep_ocean",        "lukewarm_ocean"},
        // mild/dry → mild/wet
        {"meadow",             "forest",            "old_growth_birch_forest", "dark_forest",
         "cherry_grove",       "stony_shore",        "plains",            "deep_lukewarm_ocean"},
        // warm/dry → warm/wet
        {"savanna",            "savanna",           "windswept_savanna", "jungle",
         "bamboo_jungle",      "beach",             "mangrove_swamp", "deep_lukewarm_ocean"},
        // hot/dry → hot/wet
        {"desert",             "windswept_savanna",  "badlands",          "jungle",
         "sparse_jungle",      "beach",             "mangrove_swamp",    "lukewarm_ocean"},
        // hottest/dry → hottest/wet
        {"desert",             "sparse_jungle",      "badlands",          "bamboo_jungle",
         "mushroom_fields",    "mushroom_fields",   "warm_ocean",        "warm_ocean"},
    };
    // @formatter:on

    private static final int SIZE = 8;
    private static final Set<String> ALL_BIOMES = buildAllBiomes();

    private static Set<String> buildAllBiomes() {
        var set = new LinkedHashSet<String>();
        for (String[] row : TABLE) {
            Collections.addAll(set, row);
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Returns all biome IDs that this table can ever produce.
     *
     * @return an immutable set of all valid biome IDs
     */
    public static Set<String> getAllBiomeIds() {
        return ALL_BIOMES;
    }

    /**
     * Looks up a biome ID from temperature and humidity values.
     *
     * @param temperature noise value in [-1.0, 1.0] (clamped)
     * @param humidity    noise value in [0.0, 1.0] (clamped)
     * @return a valid Minecraft biome ID string
     */
    public static String lookup(double temperature, double humidity) {
        int ti = clampIndex(temperature, -1.0, 1.0);
        int hi = clampIndex(humidity, 0.0, 1.0);
        return TABLE[ti][hi];
    }

    private static int clampIndex(double value, double min, double max) {
        if (value <= min) return 0;
        if (value >= max) return SIZE - 1;
        double norm = (value - min) / (max - min);
        int idx = (int) (norm * SIZE);
        if (idx >= SIZE) idx = SIZE - 1;
        return idx;
    }
}
