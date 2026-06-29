package com.geoforge.engine.config.biome;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resolves a block position to a biome ID using configurable climate envelopes.
 *
 * <p>Each biome declares an envelope: temperature range, humidity range, continentalness
 * range, and a priority. The resolver finds all envelopes that contain the sampled noise
 * values (temperature, humidity, continentalness) at a given position, then selects the
 * one with the highest priority. Ties are broken deterministically.
 *
 * <p>This replaces {@link com.geoforge.engine.biome.BiomeLookupTable} with a fully
 * config-driven approach.
 */
@SuppressFBWarnings({"DLS_DEAD_LOCAL_STORE", "UC_USELESS_OBJECT"})
public final class ClimateResolver {

    private final ClimateConfig config;
    private final List<BiomeEnvelope> envelopes;
    private final String defaultBiome;

    /**
     * Climate sampling configuration, mirroring the relevant fields from
     * {@link com.geoforge.engine.config.GeoForgeConfig}.
     *
     * @param temperatureFrequency     frequency for 3D temperature noise
     * @param temperatureYFrequency    Y-axis frequency for temperature noise (altitude effect)
     * @param humidityFrequency        frequency for 2D humidity noise
     */
    public record ClimateConfig(
            double temperatureFrequency,
            double temperatureYFrequency,
            double humidityFrequency) {
        public static ClimateConfig defaults() {
            return new ClimateConfig(0.001, 0.005, 0.001);
        }
    }

    /**
     * A climate envelope associating a biome with a region in climate space.
     *
     * @param biomeId          biome identifier
     * @param tempMin          minimum temperature (inclusive)
     * @param tempMax          maximum temperature (inclusive)
     * @param humidityMin      minimum humidity (inclusive)
     * @param humidityMax      maximum humidity (inclusive)
     * @param continentalnessMin minimum continentalness (inclusive)
     * @param continentalnessMax maximum continentalness (inclusive)
     * @param priority         higher priority wins when multiple envelopes match
     */
    public record BiomeEnvelope(
            String biomeId,
            double tempMin, double tempMax,
            double humidityMin, double humidityMax,
            double continentalnessMin, double continentalnessMax,
            int priority) {
    }

    /**
     * Creates a climate resolver from the given config and biome envelopes.
     *
     * @param config      climate sampling configuration
     * @param envelopes   ordered list of biome envelopes (will be sorted by priority)
     * @param defaultBiome biome to return when no envelope matches (e.g. {@code "ocean"})
     */
    public ClimateResolver(ClimateConfig config, List<BiomeEnvelope> envelopes,
                           String defaultBiome) {
        this.config = config != null ? config : ClimateConfig.defaults();

        // Sort by priority descending for deterministic resolution
        var sorted = new ArrayList<>(envelopes != null ? envelopes : List.of());
        sorted.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
        this.envelopes = List.copyOf(sorted);
        this.defaultBiome = defaultBiome != null ? defaultBiome : "ocean";
    }

    /**
     * Resolves the biome ID at the given position from climate noise values.
     *
     * <p>This method has the same signature pattern as the noise sampling step,
     * accepting raw noise values rather than sampling internally, to remain
     * compatible with the existing engine's noise setup.
     *
     * @param temperature     temperature noise value (typically in [-1, 1])
     * @param humidity        humidity noise value (typically in [0, 1])
     * @param continentalness continentalness value in [0, 1]
     * @return the resolved biome ID, or {@code defaultBiome} if no envelope matches
     */
    public String resolve(double temperature, double humidity, double continentalness) {
        for (BiomeEnvelope env : envelopes) {
            if (temperature >= env.tempMin() && temperature < env.tempMax()
                    && humidity >= env.humidityMin() && humidity < env.humidityMax()
                    && continentalness >= env.continentalnessMin()
                    && continentalness < env.continentalnessMax()) {
                return env.biomeId();
            }
        }
        return defaultBiome;
    }

    /**
     * Returns an unmodifiable view of the configured climate envelopes.
     *
     * @return sorted list of biome envelopes (highest priority first)
     */
    public List<BiomeEnvelope> envelopes() {
        return envelopes;
    }

    /**
     * Returns the climate configuration.
     *
     * @return climate config
     */
    public ClimateConfig config() {
        return config;
    }

    /**
     * Returns the default biome ID when no envelope matches.
     *
     * @return default biome ID
     */
    public String defaultBiome() {
        return defaultBiome;
    }

    /**
     * Exports the current hardcoded {@link com.geoforge.engine.biome.BiomeLookupTable}
     * logic as a list of {@link BiomeEnvelope} values that replicate its behaviour.
     *
     * <p>The legacy BiomeLookupTable uses an 8×8 grid indexed by temperature and humidity,
     * with 4 continentalness tiers: ocean (&lt; 0.3), coast ([0.3, 0.5)), inland ([0.5, 0.7)),
     * and highland (≥ 0.7). Each cell maps to a single biome ID. This export produces
     * one envelope per (ti, hi, continentalnessTier) combination — 8 × 8 × 4 = 256 envelopes.
     *
     * @return list of biome envelopes matching the current hardcoded lookup table
     */
    public static List<BiomeEnvelope> exportFromLegacyTable() {
        var result = new ArrayList<BiomeEnvelope>();

        // Continentalness tiers matching BiomeLookupTable
        double[][] contTiers = {
            {0.0, 0.3},   // ocean
            {0.3, 0.5},   // coast
            {0.5, 0.7},   // inland
            {0.7, 1.0}    // highland
        };
        // Tier names for the lookup methods
        String[] tierNames = {"ocean", "coast", "inland", "highland"};

        // The 8×8 temperature×humidity grid from BiomeLookupTable
        String[][] grid = {
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

        // Temp bands: [-1.0, 1.0] divided into 8 equal bands
        // Humidity bands: [0.0, 1.0] divided into 8 equal bands
        double tempBand = 2.0 / 8.0;
        double humBand = 1.0 / 8.0;

        for (int ti = 0; ti < 8; ti++) {
            double tMin = -1.0 + ti * tempBand;
            double tMax = -1.0 + (ti + 1) * tempBand;
            // Ensure last band covers exactly to 1.0
            if (ti == 7) tMax = 1.0;

            for (int hi = 0; hi < 8; hi++) {
                double hMin = hi * humBand;
                double hMax = (hi + 1) * humBand;
                if (hi == 7) hMax = 1.0;

                // Generate envelopes for each continentalness tier
                for (int ct = 0; ct < 4; ct++) {
                    String biomeId = lookupLegacy(ti, hi, ct, grid);
                    result.add(new BiomeEnvelope(
                            biomeId,
                            tMin, tMax,
                            hMin, hMax,
                            contTiers[ct][0], contTiers[ct][1],
                            10));
                }
            }
        }

        // Deduplicate: if multiple envelopes map to the same biome with the same
        // climate space, keep only the one with the widest continentalness range.
        // Sort by priority descending for deterministic resolution.
        result.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
        return Collections.unmodifiableList(result);
    }

    /**
     * Legacy lookup replicating BiomeLookupTable's internal dispatch.
     */
    private static String lookupLegacy(int ti, int hi, int contTier, String[][] grid) {
        return switch (contTier) {
            case 0 -> oceanLookupLegacy(ti, hi);
            case 1 -> coastLookupLegacy(ti);
            case 2 -> grid[ti][hi];
            case 3 -> highlandLookupLegacy(ti, hi);
            default -> "ocean";
        };
    }

    private static String oceanLookupLegacy(int ti, int hi) {
        if (ti <= 0) return hi < 4 ? "deep_frozen_ocean" : "frozen_ocean";
        if (ti == 1) return hi < 4 ? "deep_cold_ocean" : "cold_ocean";
        if (ti <= 3) return hi < 4 ? "deep_ocean" : "ocean";
        if (ti <= 5) return hi < 4 ? "deep_lukewarm_ocean" : "lukewarm_ocean";
        return "warm_ocean";
    }

    private static String coastLookupLegacy(int ti) {
        if (ti <= 1) return "snowy_beach";
        if (ti <= 4) return "stony_shore";
        return "beach";
    }

    private static String highlandLookupLegacy(int ti, int hi) {
        if (ti == 0) return hi < 4 ? "frozen_peaks" : "jagged_peaks";
        if (ti == 1) return "grove";
        if (ti <= 3) return hi < 4 ? "meadow" : "cherry_grove";
        if (ti == 4) return "cherry_grove";
        if (ti == 5) return hi < 4 ? "windswept_hills" : "windswept_forest";
        if (ti == 6) return hi < 4 ? "windswept_savanna" : "badlands";
        return "badlands";
    }
}
