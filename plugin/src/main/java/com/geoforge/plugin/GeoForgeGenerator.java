package com.geoforge.plugin;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Random;

/**
 * The custom chunk generator for GeoForge.
 *
 * <p>Generates terrain using the engine's noise-based height function and applies surface
 * blocks according to biome-specific rules. Biome assignment is delegated to {@link
 * GeoForgeBiomeProvider}.
 */
public final class GeoForgeGenerator extends ChunkGenerator {

    /** Sea level constant — always reference this, never use the literal 63. */
    public static final int SEA_LEVEL = 63;

    private static final int SURFACE_DEPTH = 3;

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

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int blockX = chunkX * 16 + x;
                int blockZ = chunkZ * 16 + z;
                double height = engine.getHeightAt(blockX, blockZ);
                int heightY = (int) Math.round(height);

                // Fill column with stone up to terrain height
                Material stone = adapter.mapBlock("stone");
                int fillY = Math.min(heightY, maxY - 1);
                for (int y = minY; y <= fillY; y++) {
                    chunkData.setBlock(x, y, z, stone);
                }

                // Fill water from height+1 up to sea level
                if (heightY < SEA_LEVEL) {
                    Material water = adapter.mapBlock("water");
                    for (int y = heightY + 1; y <= SEA_LEVEL && y < maxY; y++) {
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

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int blockX = chunkX * 16 + x;
                int blockZ = chunkZ * 16 + z;
                int heightY = (int) Math.round(engine.getHeightAt(blockX, blockZ));

                if (heightY < minY || heightY >= maxY) continue;

                String biomeId = engine.getBiomeId(blockX, SEA_LEVEL, blockZ);
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
