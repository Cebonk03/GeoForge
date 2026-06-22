package com.geoforge.plugin;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Biome provider for GeoForge that assigns biomes based on the engine's temperature-humidity
 * noise lookup.
 *
 * <p>The biome list is computed once at construction from the engine's palette and cached.
 */
public final class GeoForgeBiomeProvider extends BiomeProvider {

    private final GeoForgeAdapter adapter;
    private final GeoForgeEngine engine;
    private final List<Biome> biomeList;

    public GeoForgeBiomeProvider(GeoForgeAdapter adapter, GeoForgeEngine engine) {
        this.adapter = adapter;
        this.engine = engine;
        this.biomeList = engine.getAllBiomeIds().stream()
                .map(adapter::mapBiome)
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public @NotNull Biome getBiome(
            @NotNull WorldInfo worldInfo,
            int x, int y, int z) {
        String engineId = engine.getBiomeId(x, y, z);
        return adapter.mapBiome(engineId);
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return biomeList;
    }
}
