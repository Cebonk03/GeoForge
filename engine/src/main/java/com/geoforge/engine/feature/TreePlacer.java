package com.geoforge.engine.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Surface feature placer for trees.
 *
 * <p>For each tree-eligible surface column, a roll against {@code treeDensity}
 * determines whether a tree spawns. The tree type is selected from the biome's
 * tree palette, and a suitable trunk-and-leaves shape is placed.
 *
 * <p>Supported tree types: oak, birch, dark_oak, jungle, spruce.
 */
public class TreePlacer implements GeoForgeFeature {

    private final double treeDensity;
    private final int maxTreeHeight;

    private static final Map<String, List<TreeType>> BIOME_TREES = buildBiomeTreeMap();

    /**
     * Creates a tree placer with the given density and height cap.
     *
     * @param treeDensity  probability in {@code [0, 1]} that any eligible column gets a tree
     * @param maxTreeHeight maximum trunk height in blocks
     */
    public TreePlacer(double treeDensity, int maxTreeHeight) {
        if (treeDensity < 0.0 || treeDensity > 1.0) {
            throw new IllegalArgumentException(
                    "treeDensity must be in [0, 1], got " + treeDensity);
        }
        if (maxTreeHeight < 4) {
            throw new IllegalArgumentException(
                    "maxTreeHeight must be >= 4, got " + maxTreeHeight);
        }
        this.treeDensity = treeDensity;
        this.maxTreeHeight = maxTreeHeight;
    }

    public double treeDensity() {
        return treeDensity;
    }

    public int maxTreeHeight() {
        return maxTreeHeight;
    }

    /**
     * Returns an unmodifiable view of the biome-to-tree-type mapping.
     */
    public static Map<String, List<TreeType>> biomeTreeMap() {
        return Collections.unmodifiableMap(BIOME_TREES);
    }

    /**
     * Supported vanilla tree types.
     */
    public enum TreeType {
        OAK("oak_log", "oak_leaves"),
        BIRCH("birch_log", "birch_leaves"),
        DARK_OAK("dark_oak_log", "dark_oak_leaves"),
        JUNGLE("jungle_log", "jungle_leaves"),
        SPRUCE("spruce_log", "spruce_leaves");

        private final String logName;
        private final String leavesName;

        TreeType(String logName, String leavesName) {
            this.logName = logName;
            this.leavesName = leavesName;
        }

        public String logName() {
            return logName;
        }

        public String leavesName() {
            return leavesName;
        }
    }

    @Override
    public void place(BlockSetter setter, int blockX, int blockZ, int surfaceY,
                      String biomeId, Random random) {
        if (random.nextDouble() >= treeDensity) {
            return;
        }

        List<TreeType> candidates = BIOME_TREES.get(biomeId);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        TreeType type = candidates.get(random.nextInt(candidates.size()));
        placeTree(setter, blockX, blockZ, surfaceY, type, random);
    }

    private void placeTree(BlockSetter setter, int blockX, int blockZ,
                           int surfaceY, TreeType type, Random random) {
        switch (type) {
            case OAK -> placeOak(setter, blockX, blockZ, surfaceY, random);
            case BIRCH -> placeBirch(setter, blockX, blockZ, surfaceY, random);
            case DARK_OAK -> placeDarkOak(setter, blockX, blockZ, surfaceY, random);
            case JUNGLE -> placeJungle(setter, blockX, blockZ, surfaceY, random);
            case SPRUCE -> placeSpruce(setter, blockX, blockZ, surfaceY, random);
        }
    }

    // ──────────────────────────────────────────────
    //  Tree shape placers
    // ──────────────────────────────────────────────

    private void placeOak(BlockSetter setter, int bx, int bz,
                          int sy, Random random) {
        int height = 4 + random.nextInt(Math.min(maxTreeHeight - 3, 3));
        String log = TreeType.OAK.logName();
        String leaf = TreeType.OAK.leavesName();

        // Trunk
        for (int dy = 1; dy <= height; dy++) {
            setter.setBlock(bx, sy + dy, bz, log);
        }

        // Canopy: 2 layers of 3x3 leaves
        fillLeavesFlat(setter, bx, sy + height, bz, 1, leaf);
        fillLeavesFlat(setter, bx, sy + height + 1, bz, 1, leaf);
    }

    private void placeBirch(BlockSetter setter, int bx, int bz,
                            int sy, Random random) {
        int height = 5 + random.nextInt(Math.min(maxTreeHeight - 4, 2));
        String log = TreeType.BIRCH.logName();
        String leaf = TreeType.BIRCH.leavesName();

        // Trunk
        for (int dy = 1; dy <= height; dy++) {
            setter.setBlock(bx, sy + dy, bz, log);
        }

        // Canopy: 2 layers of 3x3 leaves (birch has slightly smaller canopy)
        fillLeavesFlat(setter, bx, sy + height, bz, 1, leaf);
        fillLeavesFlat(setter, bx, sy + height + 1, bz, 1, leaf);
    }

    private void placeSpruce(BlockSetter setter, int bx, int bz,
                             int sy, Random random) {
        int height = 5 + random.nextInt(Math.min(maxTreeHeight - 4, 3));
        String log = TreeType.SPRUCE.logName();
        String leaf = TreeType.SPRUCE.leavesName();

        // Trunk
        for (int dy = 1; dy <= height; dy++) {
            setter.setBlock(bx, sy + dy, bz, log);
        }

        // Conical canopy: wider at bottom, narrower at top
        int leafStart = sy + height - 2;
        int leafLayers = Math.min(height - 1, 4);

        for (int layer = 0; layer < leafLayers; layer++) {
            int ly = leafStart + layer;
            // Radius decreases toward the top
            int radius = (leafLayers - layer + 1) / 2;
            if (radius < 1) radius = 1;
            fillLeavesFlat(setter, bx, ly, bz, radius, leaf);
        }
        // Top cap
        setter.setBlock(bx, sy + height + 1, bz, leaf);
    }

    private void placeJungle(BlockSetter setter, int bx, int bz,
                             int sy, Random random) {
        int height = 5 + random.nextInt(Math.min(maxTreeHeight - 4, 3));
        String log = TreeType.JUNGLE.logName();
        String leaf = TreeType.JUNGLE.leavesName();

        // Trunk
        for (int dy = 1; dy <= height; dy++) {
            setter.setBlock(bx, sy + dy, bz, log);
        }

        // Wide canopy: 3 layers, the bottom two are 5x5, top is 3x3
        fillLeavesFlat(setter, bx, sy + height - 1, bz, 2, leaf);
        fillLeavesFlat(setter, bx, sy + height, bz, 2, leaf);
        fillLeavesFlat(setter, bx, sy + height + 1, bz, 1, leaf);
    }

    private void placeDarkOak(BlockSetter setter, int bx, int bz,
                              int sy, Random random) {
        int height = 6 + random.nextInt(Math.min(maxTreeHeight - 5, 2));
        String log = TreeType.DARK_OAK.logName();
        String leaf = TreeType.DARK_OAK.leavesName();

        // 2×2 trunk
        for (int dy = 1; dy <= height; dy++) {
            setter.setBlock(bx, sy + dy, bz, log);
            setter.setBlock(bx + 1, sy + dy, bz, log);
            setter.setBlock(bx, sy + dy, bz + 1, log);
            setter.setBlock(bx + 1, sy + dy, bz + 1, log);
        }

        // Wide canopy: 3 layers of 5×5 leaves
        for (int layer = 0; layer < 3; layer++) {
            fillLeavesFlat(setter, bx, sy + height - 1 + layer, bz, 2, leaf);
        }
        // Extra top leaves
        fillLeavesFlat(setter, bx, sy + height + 2, bz, 1, leaf);
    }

    // ──────────────────────────────────────────────
    //  Utility
    // ──────────────────────────────────────────────

    /**
     * Fills a flat (single y-level) square of leaves centered at (cx, cz).
     * The square spans {@code [cx - radius, cx + radius]} × {@code [cz - radius, cz + radius]}.
     */
    private static void fillLeavesFlat(BlockSetter setter, int cx, int cy,
                                       int cz, int radius, String leafMat) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                setter.setBlock(cx + dx, cy, cz + dz, leafMat);
            }
        }
    }

    // ──────────────────────────────────────────────
    //  Biome → tree type mapping
    // ──────────────────────────────────────────────

    private static Map<String, List<TreeType>> buildBiomeTreeMap() {
        Map<String, List<TreeType>> map = new HashMap<>();

        // Forest biomes — oak and birch
        map.put("forest", List.of(TreeType.OAK, TreeType.BIRCH));
        map.put("flower_forest", List.of(TreeType.OAK, TreeType.BIRCH));
        map.put("windswept_forest", List.of(TreeType.OAK, TreeType.SPRUCE));
        map.put("old_growth_birch_forest", List.of(TreeType.BIRCH));

        // Pure birch
        map.put("birch_forest", List.of(TreeType.BIRCH));

        // Dark forest
        map.put("dark_forest", List.of(TreeType.DARK_OAK, TreeType.OAK));

        // Taiga / cold — spruce
        map.put("taiga", List.of(TreeType.SPRUCE));
        map.put("snowy_taiga", List.of(TreeType.SPRUCE));
        map.put("old_growth_pine_taiga", List.of(TreeType.SPRUCE));
        map.put("old_growth_spruce_taiga", List.of(TreeType.SPRUCE));
        map.put("grove", List.of(TreeType.SPRUCE));

        // Jungle
        map.put("jungle", List.of(TreeType.JUNGLE));
        map.put("bamboo_jungle", List.of(TreeType.JUNGLE));
        map.put("sparse_jungle", List.of(TreeType.JUNGLE));

        // Open / grassy biomes — occasional oak
        map.put("plains", List.of(TreeType.OAK));
        map.put("sunflower_plains", List.of(TreeType.OAK));
        map.put("meadow", List.of(TreeType.OAK));
        map.put("cherry_grove", List.of(TreeType.OAK));

        // Swamp
        map.put("mangrove_swamp", List.of(TreeType.OAK));

        // Windswept
        map.put("windswept_hills", List.of(TreeType.OAK, TreeType.SPRUCE));

        // Savanna — oak substitute for acacia
        map.put("savanna", List.of(TreeType.OAK));
        map.put("windswept_savanna", List.of(TreeType.OAK));

        return map;
    }
}
