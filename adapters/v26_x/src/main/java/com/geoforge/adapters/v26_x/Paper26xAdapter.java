package com.geoforge.adapters.v26_x;

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
 * Adapter for Paper 26.x server versions (26.1, 26.1.2, 26.2 alpha).
 *
 * <p>Uses {@link RegistryAccess} for biome resolution (the {@code Registry.BIOME} static
 * field is removed in 26.x). Scheduling is done via {@code RegionScheduler}, which works on
 * both Paper and Folia.
 *
 * <p>This class uses Java 25 language features and must be compiled with the Java 25
 * toolchain. It is structurally identical to {@link
 * com.geoforge.adapters.v1_21_x.Paper1_21_xAdapter} in terms of API calls; the separate
 * class exists for future 26.x API divergence and correct Java toolchain selection.
 */
public final class Paper26xAdapter implements GeoForgeAdapter {

    private final JavaPlugin plugin;

    public Paper26xAdapter(@NotNull JavaPlugin plugin) {
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
