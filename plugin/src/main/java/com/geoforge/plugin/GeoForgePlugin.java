package com.geoforge.plugin;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.GeoForgeConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main plugin class for GeoForge world generation.
 *
 * <p>Initialises the version-specific adapter and the engine on enable, then provides the
 * custom {@link ChunkGenerator} for world creation.
 */
public final class GeoForgePlugin extends JavaPlugin {

    private GeoForgeAdapter adapter;
    private GeoForgeEngine engine;

    @Override
    public void onEnable() {
        this.adapter = AdapterFactory.create(this);
        saveDefaultConfig();
        FileConfiguration cfg = getConfig();
        long seed = cfg.getLong("seed", 0L);

        GeoForgeConfig engineConfig = GeoForgeConfig.builder()
                .minHeight(cfg.getInt("terrain.min-height", -64))
                .maxHeight(cfg.getInt("terrain.max-height", 180))
                .seaLevel(cfg.getInt("terrain.sea-level", 63))
                .continentalBase(cfg.getDouble("terrain.continental-base", 50.0))
                .continentalHeightAmplitude(cfg.getDouble("terrain.continental-height-amplitude", 120.0))
                .continentalFrequency(cfg.getDouble("noise.continental-frequency", 0.004))
                .continentalOctaves(cfg.getInt("noise.continental-octaves", 4))
                .continentalLacunarity(cfg.getDouble("noise.continental-lacunarity", 2.0))
                .continentalPersistence(cfg.getDouble("noise.continental-persistence", 0.5))
                .temperatureFrequency(cfg.getDouble("climate.temperature-frequency", 0.001))
                .temperatureYFrequency(cfg.getDouble("climate.temperature-y-frequency", 0.005))
                .humidityFrequency(cfg.getDouble("climate.humidity-frequency", 0.001))
                .caveFrequency(cfg.getDouble("noise.cave-frequency", 0.03))
                .caveAmplitude(cfg.getDouble("noise.cave-amplitude", 8.0))
                .caveOctaves(cfg.getInt("noise.cave-octaves", 2))
                .caveLacunarity(cfg.getDouble("noise.cave-lacunarity", 2.0))
                .cavePersistence(cfg.getDouble("noise.cave-persistence", 0.5))
                .riverFrequency(cfg.getDouble("river.frequency", 0.01))
                .riverDepth(cfg.getInt("river.depth", 8))
                .riverWidth(cfg.getInt("river.width", 3))
                .erosionMaxDropletSteps(cfg.getInt("erosion.max-droplet-steps", 10))
                .erosionIterations(cfg.getInt("erosion.iterations", 64))
                .build();
        this.engine = new GeoForgeEngine(seed, engineConfig);

        getLogger().info(
                "GeoForge enabled | adapter=" + adapter.getClass().getSimpleName()
                        + " | folia=" + adapter.isFolia()
                        + " | seed=" + seed
                        + " | version=" + Bukkit.getMinecraftVersion());
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(
            @NotNull String worldName, @Nullable String id) {
        return new GeoForgeGenerator(adapter, engine);
    }
}
