package com.geoforge.engine.feature;

import com.geoforge.engine.biome.BiomeTerrainConfig;
import com.geoforge.engine.feature.tree.TreeType;
import com.geoforge.engine.feature.tree.TreeVariant;
import com.geoforge.engine.feature.tree.TreeVariantSelector;
import com.geoforge.engine.feature.tree.TreeRegistry;
import com.geoforge.engine.feature.tree.trunk.StraightTrunk;
import com.geoforge.engine.feature.tree.trunk.BentTrunk;
import com.geoforge.engine.feature.tree.trunk.LeaningTrunk;
import com.geoforge.engine.feature.tree.trunk.TwistedTrunk;
import com.geoforge.engine.feature.tree.trunk.MultiStemTrunk;
import com.geoforge.engine.feature.tree.trunk.FallenTrunk;
import com.geoforge.engine.feature.tree.canopy.RoundCanopy;
import com.geoforge.engine.feature.tree.canopy.OvalCanopy;
import com.geoforge.engine.feature.tree.canopy.DomedCanopy;
import com.geoforge.engine.feature.tree.canopy.ConicalCanopy;
import com.geoforge.engine.feature.tree.canopy.LayeredCanopy;
import com.geoforge.engine.feature.tree.canopy.SpreadingCanopy;
import com.geoforge.engine.feature.tree.canopy.FlatHatCanopy;
import com.geoforge.engine.feature.tree.canopy.SparseCanopy;
import com.geoforge.engine.feature.tree.canopy.WeepingCanopy;
import com.geoforge.engine.feature.tree.canopy.PlumeCanopy;
import com.geoforge.engine.feature.tree.canopy.NoCanopy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import java.util.logging.Logger;

/**
 * Surface feature placer for trees.
 *
 * <p>For each tree-eligible surface column, a roll against tree density determines
 * whether a tree spawns. The tree type is selected from the biome's tree palette
 * (via {@link TreeRegistry}), and a suitable trunk-and-leaves variant is chosen
 * deterministically from position using {@link TreeVariantSelector}.
 *
 * <p>This class replaces the old hardcoded {@code buildBiomeTreeMap()} approach
 * with a fully config-driven system. Deprecated constructors provide backward
 * compatibility for existing callers.
 */
public final class TreePlacer implements GeoForgeFeature {

    private static final Logger LOG = Logger.getLogger(TreePlacer.class.getName());

    private final double treeDensity;
    private final int maxTreeHeight;
    private final int minTreeHeight;
    private final TreeVariantSelector variantSelector;
    private final TreeRegistry treeRegistry;
    private final Map<String, BiomeTerrainConfig> biomeConfigs;

    /** Default variants per TreeType — used when no external variant config is provided. */
    static final Map<TreeType, List<TreeVariant>> DEFAULT_VARIANTS = buildDefaultVariants();

    private static Map<TreeType, List<TreeVariant>> buildDefaultVariants() {
        Map<TreeType, List<TreeVariant>> map = new HashMap<>();
        var bent = new BentTrunk();
        var leaning = new LeaningTrunk();
        var twisted = new TwistedTrunk();
        var straight = new StraightTrunk();
        var multiStem = new MultiStemTrunk();
        var fallen = new FallenTrunk();
        var round = new RoundCanopy();
        var oval = new OvalCanopy();
        var domed = new DomedCanopy();
        var conical = new ConicalCanopy();
        var layered = new LayeredCanopy();
        var spreading = new SpreadingCanopy();
        var flatHat = new FlatHatCanopy();
        var sparse = new SparseCanopy();
        var weeping = new WeepingCanopy();
        var plume = new PlumeCanopy();
        var none = new NoCanopy();

        // OAK — 11 variants covering forest interior, edge, plains, clearings, fallen
        map.put(TreeType.OAK, List.of(
                new TreeVariant("oak_forest_tall", straight, round, 6, 10, 1.0, 0.8),
                new TreeVariant("oak_forest_standard", straight, round, 5, 7, 1.0, 0.6),
                new TreeVariant("oak_forest_dense", straight, layered, 6, 9, 1.0, 0.7),
                new TreeVariant("oak_forest_edge", bent, spreading, 4, 6, 0.8, 0.4),
                new TreeVariant("oak_forest_light", straight, oval, 5, 8, 0.9, 0.5),
                new TreeVariant("oak_plains_exposed", twisted, sparse, 3, 5, 0.5, 0.3),
                new TreeVariant("oak_plains_lone", straight, spreading, 5, 8, 0.9, 0.4),
                new TreeVariant("oak_plains_old", multiStem, domed, 4, 6, 0.8, 0.3),
                new TreeVariant("oak_clearing", leaning, domed, 4, 7, 0.7, 0.4),
                new TreeVariant("oak_sapling", straight, sparse, 3, 4, 1.0, 0.2),
                new TreeVariant("oak_fallen", fallen, none, 3, 5, 0.0, 0.2)));
        // BIRCH — 6 variants
        map.put(TreeType.BIRCH, List.of(
                new TreeVariant("birch_standard", straight, round, 5, 8, 0.9, 0.7),
                new TreeVariant("birch_tall", straight, oval, 6, 10, 0.8, 0.4),
                new TreeVariant("birch_old", straight, domed, 5, 7, 1.0, 0.3),
                new TreeVariant("birch_bent", bent, sparse, 4, 6, 0.7, 0.3),
                new TreeVariant("birch_fallen", fallen, sparse, 3, 5, 0.3, 0.3),
                new TreeVariant("birch_sapling", straight, sparse, 3, 4, 0.8, 0.2)));
        // SPRUCE — 6 variants
        map.put(TreeType.SPRUCE, List.of(
                new TreeVariant("spruce_standard", straight, conical, 5, 8, 1.0, 0.6),
                new TreeVariant("spruce_tall", straight, conical, 7, 14, 0.9, 0.3),
                new TreeVariant("spruce_column", straight, plume, 8, 16, 0.8, 0.2),
                new TreeVariant("spruce_short", straight, conical, 4, 6, 1.0, 0.4),
                new TreeVariant("spruce_bent", bent, conical, 5, 7, 0.8, 0.3),
                new TreeVariant("spruce_fallen", fallen, none, 3, 5, 0.0, 0.1)));
        // JUNGLE — 6 variants
        map.put(TreeType.JUNGLE, List.of(
                new TreeVariant("jungle_standard", straight, spreading, 6, 10, 1.0, 0.5),
                new TreeVariant("jungle_tall", straight, spreading, 8, 18, 0.9, 0.3),
                new TreeVariant("jungle_weeping", straight, weeping, 7, 14, 0.8, 0.3),
                new TreeVariant("jungle_multi", multiStem, spreading, 5, 8, 0.7, 0.3),
                new TreeVariant("jungle_bent", bent, oval, 5, 8, 0.7, 0.3),
                new TreeVariant("jungle_fallen", fallen, sparse, 4, 6, 0.3, 0.2)));
        // ACACIA — 6 variants
        map.put(TreeType.ACACIA, List.of(
                new TreeVariant("acacia_standard", straight, flatHat, 4, 7, 0.9, 0.7),
                new TreeVariant("acacia_tall", straight, flatHat, 6, 10, 0.8, 0.3),
                new TreeVariant("acacia_bent", bent, sparse, 3, 5, 0.6, 0.3),
                new TreeVariant("acacia_spreading", straight, spreading, 4, 6, 0.7, 0.3),
                new TreeVariant("acacia_twisted", twisted, flatHat, 4, 6, 0.7, 0.2),
                new TreeVariant("acacia_fallen", fallen, sparse, 3, 5, 0.3, 0.3)));
        // DARK_OAK — 6 variants
        map.put(TreeType.DARK_OAK, List.of(
                new TreeVariant("dark_oak_standard", multiStem, spreading, 6, 9, 1.0, 0.7),
                new TreeVariant("dark_oak_tall", multiStem, spreading, 7, 12, 0.9, 0.3),
                new TreeVariant("dark_oak_round", straight, round, 5, 8, 0.8, 0.4),
                new TreeVariant("dark_oak_domed", multiStem, domed, 6, 9, 0.9, 0.3),
                new TreeVariant("dark_oak_bent", bent, round, 5, 7, 0.7, 0.3),
                new TreeVariant("dark_oak_fallen", fallen, sparse, 4, 6, 0.3, 0.3)));
        // PALE_OAK — 6 variants
        map.put(TreeType.PALE_OAK, List.of(
                new TreeVariant("pale_oak_standard", multiStem, round, 5, 9, 0.9, 0.5),
                new TreeVariant("pale_oak_tall", straight, round, 6, 11, 0.8, 0.3),
                new TreeVariant("pale_oak_sparse", straight, sparse, 5, 9, 0.4, 0.3),
                new TreeVariant("pale_oak_weeping", straight, weeping, 5, 8, 0.7, 0.3),
                new TreeVariant("pale_oak_bent", bent, oval, 4, 7, 0.6, 0.3),
                new TreeVariant("pale_oak_fallen", fallen, none, 3, 5, 0.0, 0.2)));
        // CHERRY — 6 variants
        map.put(TreeType.CHERRY, List.of(
                new TreeVariant("cherry_standard", straight, round, 4, 7, 1.0, 0.7),
                new TreeVariant("cherry_tall", straight, round, 5, 9, 0.9, 0.3),
                new TreeVariant("cherry_bent", bent, oval, 4, 6, 0.8, 0.4),
                new TreeVariant("cherry_weeping", straight, weeping, 5, 8, 0.7, 0.3),
                new TreeVariant("cherry_multi", multiStem, round, 4, 6, 0.8, 0.3),
                new TreeVariant("cherry_fallen", fallen, sparse, 3, 5, 0.3, 0.3)));
        // MANGROVE — 6 variants
        map.put(TreeType.MANGROVE, List.of(
                new TreeVariant("mangrove_standard", straight, spreading, 5, 9, 1.0, 0.6),
                new TreeVariant("mangrove_tall", straight, spreading, 7, 14, 0.9, 0.2),
                new TreeVariant("mangrove_multi", multiStem, spreading, 5, 8, 0.8, 0.3),
                new TreeVariant("mangrove_bent", bent, sparse, 4, 6, 0.6, 0.3),
                new TreeVariant("mangrove_weeping", straight, weeping, 5, 9, 0.7, 0.2),
                new TreeVariant("mangrove_fallen", fallen, sparse, 3, 5, 0.3, 0.2)));

        return Collections.unmodifiableMap(map);
    }

    // ──────────────────────────────────────────────
    //  New constructors (preferred)
    // ──────────────────────────────────────────────

    /**
     * Creates a tree placer with the full variant-based system.
     *
     * @param treeDensity      global tree density [0,1]
     * @param maxTreeHeight    maximum trunk height in blocks
     * @param minTreeHeight    minimum trunk height in blocks
     * @param variantSelector  noise-based variant selector
     * @param treeRegistry     biome-to-species registry
     * @param biomeConfigs     per-biome terrain configs (for density/height overrides)
     */
    public TreePlacer(double treeDensity, int maxTreeHeight, int minTreeHeight,
                      TreeVariantSelector variantSelector, TreeRegistry treeRegistry,
                      Map<String, BiomeTerrainConfig> biomeConfigs) {
        if (treeDensity < 0.0 || treeDensity > 1.0) {
            throw new IllegalArgumentException(
                    "treeDensity must be in [0, 1], got " + treeDensity);
        }
        if (maxTreeHeight < 4) {
            throw new IllegalArgumentException(
                    "maxTreeHeight must be >= 4, got " + maxTreeHeight);
        }
        if (minTreeHeight < 3) {
            throw new IllegalArgumentException(
                    "minTreeHeight must be >= 3, got " + minTreeHeight);
        }
        this.treeDensity = treeDensity;
        this.maxTreeHeight = maxTreeHeight;
        this.minTreeHeight = minTreeHeight;
        this.variantSelector = variantSelector;
        this.treeRegistry = treeRegistry;
        this.biomeConfigs = biomeConfigs != null ? Map.copyOf(biomeConfigs) : Map.of();
    }

    // ──────────────────────────────────────────────
    //  Deprecated constructors (backward compat)
    // ──────────────────────────────────────────────

    @Deprecated
    public TreePlacer(double treeDensity, int maxTreeHeight) {
        this(treeDensity, maxTreeHeight, 4,
                new TreeVariantSelector(0L),
                TreeRegistry.defaults(),
                Map.of());
        LOG.warning("Using deprecated TreePlacer constructor — tree variants will use seed=0");
    }

    @Deprecated
    public TreePlacer(double treeDensity, int maxTreeHeight,
                      Map<String, BiomeTerrainConfig> biomeConfigs) {
        this(treeDensity, maxTreeHeight, 4,
                new TreeVariantSelector(0L),
                TreeRegistry.defaults(),
                biomeConfigs);
        LOG.warning("Using deprecated TreePlacer constructor — tree variants will use seed=0");
    }

    // ──────────────────────────────────────────────
    //  Accessors
    // ──────────────────────────────────────────────

    public double treeDensity() { return treeDensity; }
    public int maxTreeHeight() { return maxTreeHeight; }
    public int minTreeHeight() { return minTreeHeight; }

    /** Returns unmodifiable biome-to-tree-type mapping from the registry. */
    public Map<String, List<TreeType>> biomeTreeMap() {
        return treeRegistry.biomeTreeMap();
    }

    // ──────────────────────────────────────────────
    //  Tree placement
    // ──────────────────────────────────────────────

    @Override
    public void place(BlockSetter setter, int blockX, int blockZ, int surfaceY,
                      String biomeId, RandomGenerator random) {
        // Resolve effective density: per-biome override or global
        double effectiveDensity = resolveDensity(biomeId);
        if (random.nextDouble() >= effectiveDensity) {
            return;
        }

        // Resolve tree type: biome config override → registry → none
        TreeType type = resolveTreeType(biomeId, blockX, blockZ);
        if (type == null) {
            return;
        }

        // Resolve effective height range (with guard against min > max)
        int effMinHeight = resolveMinHeight(biomeId);
        int effMaxHeight = resolveMaxHeight(biomeId);
        if (effMinHeight > effMaxHeight) {
            int temp = effMinHeight;
            effMinHeight = effMaxHeight;
            effMaxHeight = temp;
        }

        // Place tree with variant selection
        placeTreeWithVariant(setter, blockX, blockZ, surfaceY, type,
                effMinHeight, effMaxHeight, biomeId, random);
    }

    private void placeTreeWithVariant(BlockSetter setter, int blockX, int blockZ,
                                       int surfaceY, TreeType type,
                                       int effMinHeight, int effMaxHeight,
                                       String biomeId, RandomGenerator random) {
        // Get variants for this species — from DEFAULT_VARIANTS, which is always populated
        List<TreeVariant> variants = DEFAULT_VARIANTS.getOrDefault(type, List.of());
        if (variants.isEmpty()) {
            // Fallback: straight trunk + round canopy
            int h = effMinHeight + random.nextInt(effMaxHeight - effMinHeight + 1);
            var tr = new StraightTrunk().place(setter, blockX, surfaceY, blockZ,
                    h, type.logName(), random);
            new RoundCanopy().place(setter, tr.tipX(), tr.tipY(), tr.tipZ(),
                    tr.placedHeight(), type.leavesName(), 1.0, random);
            return;
        }

        // Select variant via noise (deterministic from position)
        TreeVariant variant = variantSelector.select(variants, blockX, blockZ, biomeId);

        // Compute height within variant range and effective caps
        int vMin = Math.max(variant.minHeight(), effMinHeight);
        int vMax = Math.min(variant.maxHeight(), effMaxHeight);
        if (vMax < vMin) vMax = vMin;
        int height = vMin + random.nextInt(vMax - vMin + 1);

        // Place trunk
        var trunkResult = variant.trunk().place(setter, blockX, surfaceY, blockZ,
                height, type.logName(), random);

        // Place canopy (skip for fallen trees with NoCanopy / placedHeight=0)
        if (trunkResult.placedHeight() > 0 && variant.leafDensity() > 0.0) {
            variant.canopy().place(setter, trunkResult.tipX(), trunkResult.tipY(),
                    trunkResult.tipZ(), trunkResult.placedHeight(),
                    type.leavesName(), variant.leafDensity(), random);
        }
    }

    // ──────────────────────────────────────────────
    //  Resolution helpers
    // ──────────────────────────────────────────────

    private double resolveDensity(String biomeId) {
        BiomeTerrainConfig cfg = biomeConfigs.get(biomeId);
        if (cfg != null && cfg.treeDensity() >= 0.0) {
            return cfg.treeDensity();
        }
        return treeDensity;
    }

    private int resolveMinHeight(String biomeId) {
        BiomeTerrainConfig cfg = biomeConfigs.get(biomeId);
        if (cfg != null && cfg.minTreeHeight() > 0) {
            return cfg.minTreeHeight();
        }
        return minTreeHeight;
    }

    private int resolveMaxHeight(String biomeId) {
        BiomeTerrainConfig cfg = biomeConfigs.get(biomeId);
        if (cfg != null && cfg.maxTreeHeight() > 0) {
            return Math.min(cfg.maxTreeHeight(), maxTreeHeight);
        }
        return maxTreeHeight;
    }

    private TreeType resolveTreeType(String biomeId, int blockX, int blockZ) {
        // Check biome config override first
        BiomeTerrainConfig cfg = biomeConfigs.get(biomeId);
        if (cfg != null && !cfg.treeType().isEmpty()) {
            TreeType mapped = TreeType.LOOKUP.get(cfg.treeType().toLowerCase());
            if (mapped != null) {
                return mapped;
            }
            LOG.warning("Ignoring invalid tree type '" + cfg.treeType()
                    + "' for biome '" + biomeId + "': not found in TreeType enum");
        }

        // Fall back to TreeRegistry — position-deterministic selection
        List<TreeType> candidates = treeRegistry.speciesForBiome(biomeId);
        if (candidates.isEmpty()) {
            if (cfg != null && !cfg.treeType().isEmpty()) {
                LOG.warning("Biome '" + biomeId + "' has invalid treeType '" + cfg.treeType()
                        + "' and no registry fallback — no trees placed");
            }
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        // Use position-based noise for deterministic species selection
        // Simple hash of position into candidate index
        long hash = (blockX * 1664525L + blockZ * 1013904223L) ^ 0xDEADBEEFL;
        int idx = (int) ((hash & 0x7FFFFFFF) % candidates.size());
        return candidates.get(idx);
    }
}
