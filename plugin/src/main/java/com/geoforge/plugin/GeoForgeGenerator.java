package com.geoforge.plugin;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import org.bukkit.Material;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Random;

/**
 * The custom chunk generator for GeoForge.
 *
 * <p>Generates terrain using the engine's multi-octave height function modulated by tectonic
 * plate continentalness, optionally applies hydraulic erosion, and places biome-specific
 * surface blocks. Biome assignment is delegated to {@link GeoForgeBiomeProvider}.
 */
public final class GeoForgeGenerator extends ChunkGenerator {

    private static final int SURFACE_DEPTH = 3;
    private static final int CHUNK_SIZE = 16;

    private final GeoForgeAdapter adapter;
    private final GeoForgeEngine engine;
    private final GeoForgeBiomeProvider biomeProvider;

    /** Per-thread cache for the 16×16 heightmap shared between generateNoise and generateSurface. */
    private final ThreadLocal<float[]> heightCache =
            ThreadLocal.withInitial(() -> new float[CHUNK_SIZE * CHUNK_SIZE]);

    public GeoForgeGenerator(GeoForgeAdapter adapter, GeoForgeEngine engine) {
        this.adapter = adapter;
        this.engine = engine;
        this.biomeProvider = new GeoForgeBiomeProvider(adapter, engine);
    }

    @Override
    public void generateNoise(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX, int chunkZ,
            @NotNull ChunkData chunkData) {

        int minY = chunkData.getMinHeight();
        int maxY = chunkData.getMaxHeight();
        float[] heightmap = heightCache.get();

        // Step 1: Sample all 256 columns from the engine
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int blockX = chunkX * CHUNK_SIZE + x;
                int blockZ = chunkZ * CHUNK_SIZE + z;
                heightmap[z * CHUNK_SIZE + x] = (float) engine.getHeightAt(blockX, blockZ);
            }
        }

        // Step 2: Apply hydraulic erosion across the chunk heightmap
        engine.erode(heightmap, CHUNK_SIZE, random.nextLong());

        // Step 3: Fill blocks from eroded heights
        int seaLevel = engine.seaLevel();
        Material stone = adapter.mapBlock("stone");
        Material water = adapter.mapBlock("water");

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int heightY = (int) Math.round(heightmap[z * CHUNK_SIZE + x]);
                int fillY = Math.min(heightY, maxY - 1);

                // Fill column with stone up to terrain height
                for (int y = minY; y <= fillY; y++) {
                    chunkData.setBlock(x, y, z, stone);
                }

                // Fill water from height+1 up to sea level
                if (heightY < seaLevel) {
                    for (int y = heightY + 1; y <= seaLevel && y < maxY; y++) {
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
        float[] heightmap = heightCache.get();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int blockX = chunkX * CHUNK_SIZE + x;
                int blockZ = chunkZ * CHUNK_SIZE + z;
                int heightY = (int) Math.round(heightmap[z * CHUNK_SIZE + x]);

                if (heightY < minY || heightY >= maxY) continue;

                // Query biome at actual terrain height — altitude affects temperature
                String biomeId = engine.getBiomeId(blockX, heightY, blockZ);
                Material topBlock = resolveTopBlock(biomeId);
                Material subBlock = resolveSubBlock(biomeId);

                // Top block
                chunkData.setBlock(x, heightY, z, topBlock);

                // Sub-surface blocks
                for (int dy = 1; dy <= SURFACE_DEPTH; dy++) {
                    int y = heightY - dy;
                    if (y >= minY && y < maxY) {
                        chunkData.setBlock(x, y, z, subBlock);
                    }
                }
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
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return true;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    private Material resolveTopBlock(String biomeId) {
        return switch (biomeId) {
            case "desert" -> adapter.mapBlock("sand");
            case "badlands" -> adapter.mapBlock("red_sand");
            case "snowy_plains", "ice_spikes", "snowy_taiga", "snowy_beach",
                 "frozen_peaks", "grove" -> adapter.mapBlock("snow_block");
            case "beach", "stony_shore" -> adapter.mapBlock("sand");
            case "mushroom_fields" -> adapter.mapBlock("mycelium");
            default -> adapter.mapBlock("grass_block");
        };
    }

    private Material resolveSubBlock(String biomeId) {
        return switch (biomeId) {
            case "desert", "badlands" -> adapter.mapBlock("sandstone");
            case "beach", "stony_shore" -> adapter.mapBlock("sand");
            case "ocean", "deep_ocean", "cold_ocean", "deep_cold_ocean",
                 "lukewarm_ocean", "deep_lukewarm_ocean", "frozen_ocean",
                 "deep_frozen_ocean", "warm_ocean" -> adapter.mapBlock("gravel");
            default -> adapter.mapBlock("dirt");
        };
    }
}
