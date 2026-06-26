package com.geoforge.engine.biome;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An 8×8 temperature-humidity lookup table that maps climate noise values to Minecraft biome
 * identifiers, with a continentalness overlay for ocean, coast, inland, and highland biomes.
 *
 * <p>The temperature axis (−1.0 = coldest, 1.0 = hottest) is divided into 8 bands. The
 * humidity axis (0.0 = driest, 1.0 = wettest) is divided into 8 bands. Every cell contains a
 * valid vanilla biome ID that exists in all target versions (1.21.4 through 26.2).
 *
 * <p>The continentalness overlay (see {@link #lookup(double, double, double)}) subdivides
 * terrain into four tiers: ocean (&lt; 0.3), coast (0.3–0.5), inland (0.5–0.7), and
 * highland (≥ 0.7), each with environment-appropriate biome selections.
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
     * <p>Delegates to {@link #lookup(double, double, double)} with continentalness = 0.6
     * (inland default), preserving backward compatibility.
     *
     * @param temperature noise value in [-1.0, 1.0] (clamped)
     * @param humidity    noise value in [0.0, 1.0] (clamped)
     * @return a valid Minecraft biome ID string
     */
    public static String lookup(double temperature, double humidity) {
        return lookup(temperature, humidity, 0.6);
    }

    /**
     * Looks up a biome ID from temperature, humidity, and continentalness values.
     *
     * <p>Continentalness selects the environment tier:
     * <ul>
     *   <li>[0.0, 0.3) — Ocean variants (frozen_ocean, cold_ocean, ocean, lukewarm_ocean, warm_ocean)
     *   <li>[0.3, 0.5) — Coast variants (snowy_beach, stony_shore, beach)
     *   <li>[0.5, 0.7) — Inland (base 8×8 temperature × humidity grid)
     *   <li>[0.7, 1.0] — Highland variants (jagged_peaks, frozen_peaks, meadow, cherry_grove, windswept)
     * </ul>
     *
     * @param temperature     noise value in [-1.0, 1.0] (clamped)
     * @param humidity        noise value in [0.0, 1.0] (clamped)
     * @param continentalness continentalness value in [0.0, 1.0]
     * @return a valid Minecraft biome ID string
     */
    public static String lookup(double temperature, double humidity, double continentalness) {
        int ti = clampIndex(temperature, -1.0, 1.0);
        int hi = clampIndex(humidity, 0.0, 1.0);
        if (continentalness < 0.3) {
            return oceanLookup(ti, hi);
        } else if (continentalness < 0.5) {
            return coastLookup(ti);
        } else if (continentalness < 0.7) {
            return TABLE[ti][hi];
        } else {
            return highlandLookup(ti, hi);
        }
    }

    /**
     * Returns an ocean biome based on temperature and humidity indices.
     */
    private static String oceanLookup(int ti, int hi) {
        if (ti <= 0) {
            return hi < 4 ? "deep_frozen_ocean" : "frozen_ocean";
        } else if (ti == 1) {
            return hi < 4 ? "deep_cold_ocean" : "cold_ocean";
        } else if (ti <= 3) {
            return hi < 4 ? "deep_ocean" : "ocean";
        } else if (ti <= 5) {
            return hi < 4 ? "deep_lukewarm_ocean" : "lukewarm_ocean";
        } else {
            return "warm_ocean";
        }
    }

    /**
     * Returns a coast biome based on temperature index.
     */
    private static String coastLookup(int ti) {
        if (ti <= 1) {
            return "snowy_beach";
        } else if (ti <= 4) {
            return "stony_shore";
        } else {
            return "beach";
        }
    }

    /**
     * Returns a highland biome based on temperature and humidity indices.
     */
    private static String highlandLookup(int ti, int hi) {
        if (ti == 0) {
            return hi < 4 ? "frozen_peaks" : "jagged_peaks";
        } else if (ti == 1) {
            return "grove";
        } else if (ti <= 3) {
            return hi < 4 ? "meadow" : "cherry_grove";
        } else if (ti == 4) {
            return "cherry_grove";
        } else if (ti == 5) {
            return hi < 4 ? "windswept_hills" : "windswept_forest";
        } else if (ti == 6) {
            return hi < 4 ? "windswept_savanna" : "badlands";
        } else {
            return "badlands";
        }
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
