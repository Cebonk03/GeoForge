package com.geoforge.engine.feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import com.geoforge.engine.biome.BiomeTerrainConfig;

/**
 * Surface feature placer for low vegetation (grass, flowers, mushrooms, dead bushes).
 *
 * <p>Each surface column has a chance (determined by {@code vegetationDensity}) of
 * receiving one vegetation item, selected from the biome's vegetation palette.
 *
 * <p>Vegetation types are read from an instance-level map keyed by biome ID. This map
 * is typically provided from {@link com.geoforge.engine.config.biome.BiomeDefinition}
 * via the config-driven YAML system. A hardcoded default mapping exists as a fallback
 * for backward compatibility.
 */
public final class VegetationPlacer implements GeoForgeFeature {

    private final double vegetationDensity;
    private final Map<String, BiomeTerrainConfig> biomeConfigs;
    private final Map<String, List<String>> vegetationMap;

    // ──────────────────────────────────────────────
    //  Default fallback vegetation mapping
    // ──────────────────────────────────────────────

    /**
     * Default vegetation mapping used when no config-driven data is provided.
     * Retained for backward compatibility; production code should supply
     * vegetation data from {@link com.geoforge.engine.config.biome.BiomeDefinition}.
     */
    private static final Map<String, List<String>> DEFAULT_VEGETATION = buildDefaultVegetationMap();

    private static Map<String, List<String>> buildDefaultVegetationMap() {
        Map<String, List<String>> map = new HashMap<>(34);

        // Grassy biomes — short grass + flowers
        var grassFlowers = List.of("grass", "poppy", "dandelion", "azure_bluet",
                "oxeye_daisy", "cornflower");
        var grassOnly = List.of("grass", "fern");

        applyToAll(map, grassFlowers,
                "plains", "sunflower_plains", "forest", "flower_forest",
                "meadow", "cherry_grove", "windswept_forest");

        // Taiga — ferns and berries
        applyToAll(map, List.of("fern", "sweet_berry_bush", "grass"),
                "taiga", "snowy_taiga", "old_growth_pine_taiga",
                "old_growth_spruce_taiga", "grove");

        // Jungle — tall grass / ferns
        applyToAll(map, List.of("grass", "fern"),
                "jungle", "bamboo_jungle", "sparse_jungle");

        // Desert — dead bushes
        applyToAll(map, List.of("dead_bush"),
                "desert", "badlands");

        // Swamp — mushrooms and grass
        applyToAll(map, List.of("brown_mushroom", "red_mushroom", "grass"),
                "mangrove_swamp");

        // Mushroom fields — mushrooms
        applyToAll(map, List.of("brown_mushroom", "red_mushroom"),
                "mushroom_fields");

        // Birch forest — grass and flowers
        applyToAll(map, grassFlowers,
                "birch_forest", "old_growth_birch_forest");

        // Windswept — grass
        applyToAll(map, grassOnly,
                "windswept_hills");

        // Dark forest — mushrooms and grass
        applyToAll(map, List.of("brown_mushroom", "red_mushroom", "grass"),
                "dark_forest");

        // Savanna — grass
        applyToAll(map, List.of("grass"),
                "savanna", "windswept_savanna");

        return Map.copyOf(map);
    }

    /**
     * Applies the given vegetation list to all specified biome IDs.
     */
    private static void applyToAll(Map<String, List<String>> map,
                                   List<String> vegetation,
                                   String... biomes) {
        for (String biome : biomes) {
            map.put(biome, vegetation);
        }
    }

    // ──────────────────────────────────────────────
    //  Constructors
    // ──────────────────────────────────────────────

    /**
     * Creates a vegetation placer with the given density.
     *
     * @param vegetationDensity probability in {@code [0, 1]} that any eligible
     *                          column gets a vegetation item
     */
    public VegetationPlacer(double vegetationDensity) {
        this(vegetationDensity, Map.of(), DEFAULT_VEGETATION);
    }

    /**
     * Creates a vegetation placer with the given density and biome configs.
     *
     * @param vegetationDensity probability in [0, 1] that any eligible column gets vegetation
     * @param biomeConfigs      map of biome ID to terrain config (for allowFloatingPlants check)
     */
    public VegetationPlacer(double vegetationDensity, Map<String, BiomeTerrainConfig> biomeConfigs) {
        this(vegetationDensity, biomeConfigs, DEFAULT_VEGETATION);
    }

    /**
     * Creates a vegetation placer with the given density, biome configs, and
     * config-driven vegetation mapping.
     *
     * @param vegetationDensity probability in [0, 1] that any eligible column gets vegetation
     * @param biomeConfigs      map of biome ID to terrain config (for allowFloatingPlants check)
     * @param vegetationMap     map of biome ID to list of vegetation block types;
     *                          uses the built-in default if {@code null}
     */
    public VegetationPlacer(double vegetationDensity,
                            Map<String, BiomeTerrainConfig> biomeConfigs,
                            Map<String, List<String>> vegetationMap) {
        if (vegetationDensity < 0.0 || vegetationDensity > 1.0) {
            throw new IllegalArgumentException(
                    "vegetationDensity must be in [0, 1], got " + vegetationDensity);
        }
        this.vegetationDensity = vegetationDensity;
        this.biomeConfigs = biomeConfigs != null ? Map.copyOf(biomeConfigs) : Map.of();
        this.vegetationMap = vegetationMap != null ? Map.copyOf(vegetationMap) : Map.of();
    }

    public double vegetationDensity() {
        return vegetationDensity;
    }

    /**
     * Returns an unmodifiable view of the biome-to-vegetation mapping.
     */
    public Map<String, List<String>> biomeVegetationMap() {
        return vegetationMap;
    }

    @Override
    public void place(BlockSetter setter, int blockX, int blockZ, int surfaceY,
                      String biomeId, RandomGenerator random) {
        if (random.nextDouble() >= vegetationDensity) {
            return;
        }

        List<String> candidates = vegetationMap.get(biomeId);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }
        // allowFloatingPlants controls whether vegetation may be placed on water surfaces.
        // The field is defined in BiomeTerrainConfig for future water-vegetation logic;
        // current place() only operates on solid ground above sea level (surfaceY + 1).
        // Full water-surface vegetation placement is deferred.

        // Place vegetation on top of the surface block
        String vegType = candidates.get(random.nextInt(candidates.size()));
        setter.setBlock(blockX, surfaceY + 1, blockZ, vegType);
    }
}
