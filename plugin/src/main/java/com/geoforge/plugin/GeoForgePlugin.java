package com.geoforge.plugin;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
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
        this.engine = new GeoForgeEngine(seed);
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
