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
 * <p>Generates terrain using the engine's 3D density field modulated by tectonic
 * plate continentalness and 3D cave noise, places biome-specific surface blocks,
 * and delegates biome assignment to {@link GeoForgeBiomeProvider}.
 */
public final class GeoForgeGenerator extends ChunkGenerator {

    private static final int SURFACE_DEPTH = 3;
    private static final int CHUNK_SIZE = 16;
    private static final int BEDROCK_LAYERS = 5;

    private final GeoForgeAdapter adapter;
    private final GeoForgeEngine engine;
    private final GeoForgeBiomeProvider biomeProvider;

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
        int seaLevel = engine.seaLevel();
        Material stone = adapter.mapBlock("stone");
        Material water = adapter.mapBlock("water");
        Material bedrock = adapter.mapBlock("bedrock");

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int blockX = chunkX * CHUNK_SIZE + x;
                int blockZ = chunkZ * CHUNK_SIZE + z;

                boolean ocean = false;
                for (int y = minY; y < maxY; y++) {
                    // Place bedrock at bottom layers
                    if (y < minY + BEDROCK_LAYERS) {
                        chunkData.setBlock(x, y, z, bedrock);
                        continue;
                    }

                    double density = engine.getDensity(blockX, y, blockZ);
                    if (density > 0) {
                        chunkData.setBlock(x, y, z, stone);
                    } else if (!ocean && y < seaLevel) {
                        // Start filling water when we hit air below sea level
                        ocean = true;
                    }
                }

                // Fill water from first air below sea level down to sea level
                if (ocean) {
                    int terrainTop = engine.getSurfaceHeight(blockX, blockZ);
                    for (int y = terrainTop + 1; y <= seaLevel && y < maxY; y++) {
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

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int blockX = chunkX * CHUNK_SIZE + x;
                int blockZ = chunkZ * CHUNK_SIZE + z;
                int heightY = engine.getSurfaceHeight(blockX, blockZ);

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
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
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
