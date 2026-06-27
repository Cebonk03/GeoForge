package com.geoforge.api.adapter;

import com.geoforge.api.util.FoliaDetector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.function.Function;

/**
 * Abstract base class for Paper version-specific adapters.
 *
 * <p>Eliminates the ~70% code duplication between {@link
 * com.geoforge.adapters.v1_21_x.Paper1_21_xAdapter} and {@link
 * com.geoforge.adapters.v26_x.Paper26xAdapter}. Subclasses provide the
 * constructor with version-appropriate registry lookups; all four
 * {@link GeoForgeAdapter} methods are inherited from this class.
 */
public abstract class AbstractPaperAdapter implements GeoForgeAdapter {

    /**
     * The owning JavaPlugin instance, used for task scheduling.
     */
    protected final JavaPlugin plugin;

    /**
     * Function mapping engine-internal block IDs to Paper {@link Material}.
     * Package-visible so subclasses can inject test lookups.
     */
    protected final Function<String, Material> blockLookup;

    /**
     * Function mapping engine-internal biome IDs to Paper {@link Biome}.
     * Package-visible so subclasses can inject test lookups.
     */
    protected final Function<String, Biome> biomeLookup;

    /**
     * Creates an AbstractPaperAdapter with the given plugin and lookup functions.
     *
     * @param plugin      the owning plugin
     * @param blockLookup maps engine block IDs to Paper Materials (null = not found)
     * @param biomeLookup maps engine biome IDs to Paper Biomes (null = not found)
     */
    protected AbstractPaperAdapter(
            @NotNull JavaPlugin plugin,
            @NotNull Function<String, Material> blockLookup,
            @NotNull Function<String, Biome> biomeLookup) {
        this.plugin = plugin;
        this.blockLookup = blockLookup;
        this.biomeLookup = biomeLookup;
    }

    @Override
    public @NotNull Material mapBlock(@NotNull String engineId) {
        Material mat = blockLookup.apply(engineId);
        return mat != null ? mat : Material.STONE;
    }

    @Override
    public @NotNull Biome mapBiome(@NotNull String engineId) {
        Biome biome = biomeLookup.apply(engineId);
        if (biome == null) {
            biome = biomeLookup.apply("plains");
            if (biome == null) {
                throw new IllegalStateException(
                        "plains biome missing — corrupt server installation on "
                                + Bukkit.getMinecraftVersion());
            }
        }
        return biome;
    }

    @Override
    public void scheduleTask(@NotNull Location loc, @NotNull Runnable run) {
        Bukkit.getServer().getRegionScheduler().execute(plugin, loc, run);
    }

    @Override
    public boolean isFolia() {
        return FoliaDetector.isFolia();
    }
}
