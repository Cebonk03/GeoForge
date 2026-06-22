package com.geoforge.adapters.v1_21_x;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.api.util.FoliaDetector;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter for Paper 1.21.x server versions (1.21.4 through 1.21.11).
 *
 * <p>Uses {@link RegistryAccess} for biome resolution (not the removed {@code
 * Registry.BIOME}) to maintain binary compatibility with 26.x. Scheduling is done via
 * {@code RegionScheduler}, which works on both Paper and Folia.
 */
public final class Paper1_21_xAdapter implements GeoForgeAdapter {

    private final JavaPlugin plugin;

    public Paper1_21_xAdapter(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Material mapBlock(@NotNull String engineId) {
        Material mat = Registry.MATERIAL.get(NamespacedKey.minecraft(engineId));
        if (mat == null) {
            return Material.STONE;
        }
        return mat;
    }

    @Override
    public @NotNull Biome mapBiome(@NotNull String engineId) {
        var reg = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        Biome biome = reg.get(NamespacedKey.minecraft(engineId));
        if (biome == null) {
            biome = reg.get(NamespacedKey.minecraft("plains"));
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
