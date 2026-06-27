package com.geoforge.engine.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Surface feature placer for low vegetation (grass, flowers, mushrooms, dead bushes).
 *
 * <p>Each surface column has a chance (determined by {@code vegetationDensity}) of
 * receiving one vegetation item, selected from the biome's vegetation palette.
 */
public final class VegetationPlacer implements GeoForgeFeature {

    private final double vegetationDensity;

    private static final Map<String, List<String>> BIOME_VEGETATION = buildBiomeVegetationMap();

    /**
     * Creates a vegetation placer with the given density.
     *
     * @param vegetationDensity probability in {@code [0, 1]} that any eligible
     *                          column gets a vegetation item
     */
    public VegetationPlacer(double vegetationDensity) {
        if (vegetationDensity < 0.0 || vegetationDensity > 1.0) {
            throw new IllegalArgumentException(
                    "vegetationDensity must be in [0, 1], got " + vegetationDensity);
        }
        this.vegetationDensity = vegetationDensity;
    }

    public double vegetationDensity() {
        return vegetationDensity;
    }

    /**
     * Returns an unmodifiable view of the biome-to-vegetation mapping.
     */
    public static Map<String, List<String>> biomeVegetationMap() {
        return Collections.unmodifiableMap(BIOME_VEGETATION);
    }

    @Override
    public void place(BlockSetter setter, int blockX, int blockZ, int surfaceY,
                      String biomeId, Random random) {
        if (random.nextDouble() >= vegetationDensity) {
            return;
        }

        List<String> candidates = BIOME_VEGETATION.get(biomeId);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        // Place vegetation on top of the surface block
        String vegType = candidates.get(random.nextInt(candidates.size()));
        setter.setBlock(blockX, surfaceY + 1, blockZ, vegType);
    }

    // ──────────────────────────────────────────────
    //  Biome → vegetation mapping
    // ──────────────────────────────────────────────

    private static Map<String, List<String>> buildBiomeVegetationMap() {
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

        return map;
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
}
