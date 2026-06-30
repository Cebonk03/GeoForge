package com.geoforge.engine.config.biome;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides hardcoded, tuned {@link BiomeDefinition} instances for all vanilla
 * Minecraft biomes.
 *
 * <p>This utility class replaces the deleted YAML biome loading system with
 * statically defined, merge-based biome definitions. Each biome is created by
 * merging only the fields that differ from {@link BiomeDefinition#defaults()}
 * via the {@link BiomeDefinition#merge(BiomeDefinition)} method.
 *
 * <p>Biomes are grouped by climate category for maintainability.
 */
public final class GeoForgeBiomeDefaults {

    private GeoForgeBiomeDefaults() {
        // utility class
    }

    /**
     * Returns an immutable map of all vanilla biome IDs to their corresponding
     * {@link BiomeDefinition} instances.
     *
     * @return unmodifiable map of biome ID → BiomeDefinition
     */
    public static Map<String, BiomeDefinition> createDefaults() {
        Map<String, BiomeDefinition> map = new HashMap<>();

        // ── Forests & Forest Variants ──
        map.put("forest", custom("forest", 1.0, "grass_block", "dirt", 1.0, 0.12,
                List.of("grass", "dandelion", "poppy", "cornflower")));
        map.put("flower_forest", custom("flower_forest", 1.0, "grass_block", "dirt", 1.0, 0.12,
                List.of("dandelion", "poppy", "allium", "azure_bluet",
                        "oxeye_daisy", "cornflower", "lilac", "rose_bush", "peony")));
        map.put("birch_forest", custom("birch_forest", 1.0, "grass_block", "dirt", 1.0, 0.1,
                List.of()));
        map.put("old_growth_birch_forest", custom("old_growth_birch_forest", 1.2,
                "grass_block", "dirt", 1.0, 0.15, List.of()));
        map.put("dark_forest", custom("dark_forest", 1.0, "grass_block", "dirt", 1.0, 0.2,
                List.of()));

        // ── Cold / Taiga ──
        map.put("taiga", custom("taiga", 1.0, "grass_block", "dirt", 1.0, 0.1,
                List.of()));
        map.put("snowy_taiga", custom("snowy_taiga", 1.0, "grass_block", "dirt", 1.0, 0.08,
                List.of()));
        map.put("old_growth_pine_taiga", custom("old_growth_pine_taiga", 1.3,
                "grass_block", "dirt", 1.0, 0.12, List.of()));
        map.put("old_growth_spruce_taiga", custom("old_growth_spruce_taiga", 1.4,
                "grass_block", "dirt", 1.0, 0.12, List.of()));
        map.put("grove", custom("grove", 1.0, "grass_block", "dirt", 1.0, 0.05,
                List.of()));

        // ── Plains & Meadows ──
        map.put("plains", custom("plains", 1.0, "grass_block", "dirt", 1.0, 0.05,
                List.of("grass", "dandelion", "poppy")));
        map.put("sunflower_plains", custom("sunflower_plains", 1.0, "grass_block", "dirt", 1.0, 0.05,
                List.of("sunflower", "grass", "dandelion", "poppy")));
        map.put("meadow", custom("meadow", 1.0, "grass_block", "dirt", 1.0, 0.02,
                List.of("grass", "dandelion", "poppy", "cornflower")));

        // ── Jungle ──
        map.put("jungle", custom("jungle", 1.5, "grass_block", "dirt", 1.0, 0.3,
                List.of("grass", "fern")));
        map.put("bamboo_jungle", custom("bamboo_jungle", 1.0, "grass_block", "dirt", 1.0, 0.2,
                List.of()));
        map.put("sparse_jungle", custom("sparse_jungle", 1.2, "grass_block", "dirt", 1.0, 0.1,
                List.of()));

        // ── Savanna ──
        map.put("savanna", custom("savanna", 1.0, "grass_block", "dirt", 1.0, 0.04,
                List.of()));
        map.put("savanna_plateau", custom("savanna_plateau", 1.0, "grass_block", "dirt", 1.0, 0.04,
                List.of()));
        map.put("windswept_savanna", custom("windswept_savanna", 1.3,
                "grass_block", "dirt", 1.0, 0.02, List.of()));

        // ── Desert & Badlands ──
        map.put("desert", custom("desert", 1.0, "sand", "sandstone", 1.5, 0.0,
                List.of("dead_bush", "cactus")));
        map.put("badlands", custom("badlands", 1.0, "red_sand", "red_sandstone", 1.3, 0.0,
                List.of()));
        map.put("eroded_badlands", custom("eroded_badlands", 1.0, "red_sand", "red_sandstone", 1.3, 0.0,
                List.of()));
        map.put("wooded_badlands", custom("wooded_badlands", 1.0, "red_sand", "dirt", 1.0, 0.03,
                List.of()));

        // ── Swamp ──
        map.put("swamp", custom("swamp", 1.0, "grass_block", "dirt", 1.2, 0.1,
                List.of("grass", "lily_pad")));
        map.put("mangrove_swamp", custom("mangrove_swamp", 1.0, "mud", "dirt", 1.0, 0.15,
                List.of()));

        // ── Mountains ──
        map.put("windswept_hills", custom("windswept_hills", 1.6, "grass_block", "stone", 1.0, -1.0,
                List.of()));
        map.put("windswept_forest", custom("windswept_forest", 1.5, "grass_block", "dirt", 1.0, 0.06,
                List.of()));
        map.put("windswept_gravelly_hills", custom("windswept_gravelly_hills", 1.6,
                "grass_block", "stone", 1.0, -1.0, List.of()));
        map.put("frozen_peaks", custom("frozen_peaks", 1.8, "stone", "stone", 1.0, -1.0,
                List.of()));
        map.put("jagged_peaks", custom("jagged_peaks", 2.0, "stone", "stone", 1.0, -1.0,
                List.of()));
        map.put("stony_peaks", custom("stony_peaks", 1.7, "stone", "stone", 1.0, -1.0,
                List.of()));
        map.put("snowy_slopes", custom("snowy_slopes", 1.5, "snow_block", "stone", 1.0, -1.0,
                List.of()));

        // ── Snowy / Frozen ──
        map.put("snowy_plains", custom("snowy_plains", 1.0, "snow_block", "dirt", 1.0, 0.0,
                List.of()));
        map.put("ice_spikes", custom("ice_spikes", 1.0, "snow_block", "dirt", 1.0, 0.0,
                List.of()));

        // ── Ocean ──
        map.put("ocean", custom("ocean", 1.0, "stone", "stone", 0.8, -1.0, List.of()));
        map.put("deep_ocean", custom("deep_ocean", 1.0, "stone", "stone", 0.6, -1.0, List.of()));
        map.put("cold_ocean", custom("cold_ocean", 1.0, "stone", "stone", 0.8, -1.0, List.of()));
        map.put("deep_cold_ocean", custom("deep_cold_ocean", 1.0, "stone", "stone", 0.6, -1.0,
                List.of()));
        map.put("frozen_ocean", custom("frozen_ocean", 1.0, "ice", "stone", 0.8, -1.0, List.of()));
        map.put("deep_frozen_ocean", custom("deep_frozen_ocean", 1.0, "ice", "stone", 0.6, -1.0,
                List.of()));
        map.put("lukewarm_ocean", custom("lukewarm_ocean", 1.0, "stone", "stone", 0.8, -1.0,
                List.of()));
        map.put("deep_lukewarm_ocean", custom("deep_lukewarm_ocean", 1.0, "stone", "stone", 0.6, -1.0,
                List.of()));
        map.put("warm_ocean", custom("warm_ocean", 1.0, "sand", "sand", 1.0, -1.0, List.of()));

        // ── Beach & Shore ──
        map.put("beach", custom("beach", 1.0, "sand", "sand", 1.0, -1.0, List.of()));
        map.put("snowy_beach", custom("snowy_beach", 1.0, "snow_block", "sand", 1.0, -1.0,
                List.of()));
        map.put("stony_shore", custom("stony_shore", 1.0, "stone", "stone", 1.0, -1.0, List.of()));

        // ── Rivers ──
        map.put("river", custom("river", 1.0, "grass_block", "dirt", 1.0, -1.0, List.of()));
        map.put("frozen_river", custom("frozen_river", 1.0, "ice", "dirt", 1.0, -1.0, List.of()));

        // ── Special / Cave Biomes ──
        map.put("cherry_grove", custom("cherry_grove", 1.0, "grass_block", "dirt", 1.0, 0.08,
                List.of("grass", "dandelion", "poppy", "pink_petals")));
        map.put("mushroom_fields", custom("mushroom_fields", 1.0, "mycelium", "dirt", 1.0, 0.0,
                List.of()));
        map.put("pale_garden", custom("pale_garden", 1.0, "pale_moss_block", "dirt", 1.0, 0.06,
                List.of("grass", "dandelion", "poppy")));
        map.put("deep_dark", neutral("deep_dark"));
        map.put("dripstone_caves", neutral("dripstone_caves"));
        map.put("lush_caves", neutral("lush_caves"));

        // ── Nether ──
        map.put("nether_wastes", neutral("nether_wastes"));
        map.put("crimson_forest", neutral("crimson_forest"));
        map.put("warped_forest", neutral("warped_forest"));
        map.put("soul_sand_valley", neutral("soul_sand_valley"));
        map.put("basalt_deltas", neutral("basalt_deltas"));

        // ── End ──
        map.put("the_end", neutral("the_end"));
        map.put("end_highlands", neutral("end_highlands"));
        map.put("end_midlands", neutral("end_midlands"));
        map.put("end_barrens", neutral("end_barrens"));
        map.put("small_end_islands", neutral("small_end_islands"));

        // ── Miscellaneous ──
        map.put("the_void", neutral("the_void"));

        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates a neutral biome definition with only the ID set and everything
     * else at default values.
     *
     * @param id biome identifier
     * @return a merged BiomeDefinition with just the ID overridden
     */
    private static BiomeDefinition neutral(String id) {
        return BiomeDefinition.defaults().merge(new BiomeDefinition(
                id, 0.0, 1.0, "", "", 0.5, 1.0, "", -1.0, 0, 0,
                Map.of(), List.of(), 0.3, false,
                -1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0));
    }

    /**
     * Creates a biome definition with commonly overridden fields merged on top
     * of defaults.
     *
     * @param id         biome identifier
     * @param amp        amplitude multiplier (1.0 = default)
     * @param surface    surface block material ID
     * @param subSurface sub-surface block material ID
     * @param caveAmp    cave amplitude modifier (1.0 = default)
     * @param treeDensity tree density in [0, 1], or -1.0 for global default
     * @param veg        list of vegetation block IDs
     * @return a merged BiomeDefinition with the specified overrides applied
     */
    private static BiomeDefinition custom(String id, double amp, String surface,
                                           String subSurface, double caveAmp,
                                           double treeDensity, List<String> veg) {
        return BiomeDefinition.defaults().merge(new BiomeDefinition(
                id, 0.0, amp, surface, subSurface, 0.5, caveAmp, "", treeDensity, 0, 0,
                Map.of(), veg, 0.3, false,
                -1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0));
    }
}
