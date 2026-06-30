package com.geoforge.plugin;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.TreePlacer;
import com.geoforge.engine.feature.VegetationPlacer;
import com.geoforge.engine.feature.ScenicFeatureDetector;
import com.geoforge.engine.util.ThreadLocalBuffers;
import com.geoforge.engine.biome.BiomeTerrainConfig;
import org.bukkit.Material;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Random;
import java.util.SplittableRandom;

/**
 * The custom chunk generator for GeoForge.
 *
 * <p>Generates terrain using the engine's 3D density field modulated by tectonic
 * plate continentalness and 3D cave noise, places biome-specific surface blocks,
 * and delegates biome assignment to {@link GeoForgeBiomeProvider}.
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public final class GeoForgeGenerator extends ChunkGenerator {

    private static final int CHUNK_SIZE = 16;
    private static final int BEDROCK_LAYERS = 5;

    private final GeoForgeAdapter adapter;
    private final GeoForgeEngine engine;
    private final GeoForgeBiomeProvider biomeProvider;
    private final TreePlacer treePlacer;
    private final VegetationPlacer vegetationPlacer;
    private final long worldSeed;

    /** ThreadLocal cache for eroded heights — thread-safe under Folia parallel chunk gen. */
    private final ThreadLocal<CachedErosion> erosionCache =
            ThreadLocal.withInitial(CachedErosion::new);

    private static final class CachedErosion {
        int chunkX = Integer.MIN_VALUE;
        int chunkZ = Integer.MIN_VALUE;
        float[] erodedHeights;
    }

    public GeoForgeGenerator(GeoForgeAdapter adapter, GeoForgeEngine engine, long worldSeed) {
        this.adapter = adapter;
        this.engine = engine;
        this.worldSeed = worldSeed;
        this.biomeProvider = new GeoForgeBiomeProvider(adapter, engine);
        // Build biome variant modifiers from TreeRegistry
        var registry = com.geoforge.engine.feature.tree.TreeRegistry.defaults();
        var biomeModifiers = new java.util.HashMap<String, java.util.Map<String, Double>>();
        for (String biomeId : registry.biomeTreeMap().keySet()) {
            var cfg = registry.configForBiome(biomeId);
            if (cfg != null && !cfg.variantModifiers().isEmpty()) {
                biomeModifiers.put(biomeId, cfg.variantModifiers());
            }
        }
        var variantSelector = new com.geoforge.engine.feature.tree.TreeVariantSelector(
                worldSeed, engine.config().treeDensityFrequency(),
                java.util.Collections.unmodifiableMap(biomeModifiers));
        this.treePlacer = new TreePlacer(
                0.1,   // treeDensity global fallback (now per-biome)
                12,    // maxTreeHeight global fallback (now per-biome)
                4,     // minTreeHeight global fallback (now per-biome)
                variantSelector,
                registry,
                engine.getBiomeConfigs());
        this.vegetationPlacer = new VegetationPlacer(
                0.3,   // vegetationDensity global fallback (now per-biome)
                engine.getBiomeConfigs(),
                engine.getBiomeVegetation());
    }

    @Override
    public void generateNoise(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX, int chunkZ,
            @NotNull ChunkData chunkData) {

        int minY = chunkData.getMinHeight();
        int maxY = chunkData.getMaxHeight();
        int seaLevel = engine.seaLevel();
        Material stone = adapter.mapBlock("stone");
        Material water = adapter.mapBlock("water");
        Material bedrock = adapter.mapBlock("bedrock");

        // Pre-compute eroded surface heights for this chunk (recycled buffer)
        float[] erodedHeights;
        try (var bufs = ThreadLocalBuffers.acquire()) {
            erodedHeights = bufs.floatArray(CHUNK_SIZE * CHUNK_SIZE);
            engine.erodeColumn(erodedHeights, CHUNK_SIZE,
                    chunkX * CHUNK_SIZE, chunkZ * CHUNK_SIZE,
                    worldInfo.getSeed());
        }

        // Cache eroded heights for reuse in generateSurface() (ThreadLocal = thread-safe under Folia)
        CachedErosion cache = erosionCache.get();
        cache.erodedHeights = erodedHeights.clone();
        cache.chunkX = chunkX;
        cache.chunkZ = chunkZ;
        boolean erosionActive = engine.config().erosionIterations() > 0
                && engine.config().erosionDropletCount() > 0;

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int blockX = chunkX * CHUNK_SIZE + x;
                int blockZ = chunkZ * CHUNK_SIZE + z;
                int surfaceY = Math.round(erodedHeights[z * CHUNK_SIZE + x]);

                boolean ocean = false;
                for (int y = minY; y < maxY; y++) {
                    // Place bedrock at bottom layers
                    if (y < minY + BEDROCK_LAYERS) {
                        chunkData.setBlock(x, y, z, bedrock);
                        continue;
                    }

                    double density = engine.getDensity(blockX, y, blockZ);
                    // When erosion is active, constrain blocks to at or below the eroded surface;
                    // when inactive, use original density-only placement (identical to pre-erosion)
                    if (density > 0 && (y <= surfaceY || !erosionActive)) {
                        chunkData.setBlock(x, y, z, stone);
                    } else if (!ocean && y < seaLevel) {
                        // Start filling water when we hit air below sea level
                        ocean = true;
                    }
                }

                // Fill water from eroded surface down to sea level
                if (ocean) {
                    for (int y = surfaceY + 1; y <= seaLevel && y < maxY; y++) {
                        chunkData.setBlock(x, y, z, water);
                    }
                }
            }
        }
    }

    @Override
    public void generateSurface(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX, int chunkZ,
            @NotNull ChunkData chunkData) {

        int minY = chunkData.getMinHeight();
        int maxY = chunkData.getMaxHeight();

        // BlockSetter adapter: converts world coordinates to chunk-local coordinates
        // and material names to server Material instances via the adapter
        BlockSetter blockSetter = (wx, wy, wz, mat) -> {
            int lx = wx - chunkX * CHUNK_SIZE;
            int lz = wz - chunkZ * CHUNK_SIZE;
            if (lx >= 0 && lx < CHUNK_SIZE && lz >= 0 && lz < CHUNK_SIZE && wy >= minY && wy < maxY) {
                chunkData.setBlock(lx, wy, lz, adapter.mapBlock(mat));
            }
        };
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int blockX = chunkX * CHUNK_SIZE + x;
                int blockZ = chunkZ * CHUNK_SIZE + z;
                // Reuse surface heights from ThreadLocal cache (thread-safe under Folia)
                CachedErosion cache = erosionCache.get();
                int heightY;
                if (chunkX == cache.chunkX && chunkZ == cache.chunkZ && cache.erodedHeights != null) {
                    heightY = Math.round(cache.erodedHeights[z * CHUNK_SIZE + x]);
                } else {
                    heightY = engine.getSurfaceHeight(blockX, blockZ);
                }

                if (heightY < minY || heightY >= maxY) continue;

                // Query biome at actual terrain height — altitude affects temperature
                String biomeId = engine.getBiomeId(blockX, heightY, blockZ);
                BiomeTerrainConfig biomeConfig = engine.getBiomeConfig(biomeId);
                String surfaceBlockStr = biomeConfig != null ? biomeConfig.surfaceBlock() : "";
                Material topBlock = !surfaceBlockStr.isEmpty()
                        ? adapter.mapBlock(surfaceBlockStr)
                        : adapter.mapBlock("grass_block");
                String subSurfaceBlockStr = biomeConfig != null ? biomeConfig.subSurfaceBlock() : "";
                Material subBlock = !subSurfaceBlockStr.isEmpty()
                        ? adapter.mapBlock(subSurfaceBlockStr)
                        : adapter.mapBlock("dirt");
                // Top block
                chunkData.setBlock(x, heightY, z, topBlock);

                // Sub-surface blocks — use per-biome surface depth
                int surfaceDepth = biomeConfig != null ? biomeConfig.surfaceDepth() : 3;
                for (int dy = 1; dy <= surfaceDepth; dy++) {
                    int y = heightY - dy;
                    if (y >= minY && y < maxY) {
                        chunkData.setBlock(x, y, z, subBlock);
                    }
                }

            // Scenic feature detection for wow moments
            if (heightY >= minY + 1 && heightY < maxY) {
                var feature = engine.getScenicDetector().detect(engine, blockX, blockZ, heightY);
                switch (feature.type()) {
                    case EDGE_VISTA: {
                        // Replace surface block with stone at cliff edge
                        chunkData.setBlock(x, heightY, z, adapter.mapBlock("stone"));
                        break;
                    }
                    case HIDDEN_VALLEY: {
                        // Replace surface block with stone on the ridge crest
                        chunkData.setBlock(x, heightY, z, adapter.mapBlock("stone"));
                        break;
                    }
                    case EMERGENCE: {
                        // Stone outcropping at potential cave exit
                        chunkData.setBlock(x, heightY, z, adapter.mapBlock("stone"));
                        if (heightY > minY + 2) {
                            chunkData.setBlock(x, heightY - 1, z, adapter.mapBlock("stone"));
                        }
                        break;
                    }
                    default: {}
                }
            }

            // Surface features (trees and vegetation)
            long fs = engine.config().featureSeedOffset();
            var fr = fs != 0 ? new SplittableRandom(random.nextLong() ^ fs) : random;
            treePlacer.place(blockSetter, blockX, blockZ, heightY, biomeId, fr);
            vegetationPlacer.place(blockSetter, blockX, blockZ, heightY, biomeId, fr);
            }
        }
    }

    @Override
    public @Nullable BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return biomeProvider;
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateBedrock() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false; // GeoForge handles features in generateSurface()
    }

    @Override
    public boolean shouldGenerateStructures() {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

}
