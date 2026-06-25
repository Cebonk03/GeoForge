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
import java.util.function.Function;
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
    private final Function<@NotNull String, @NotNull Material> blockLookup;
    private final Function<@NotNull String, @NotNull Biome> biomeLookup;
    public Paper1_21_xAdapter(@NotNull JavaPlugin plugin) {
        this(plugin,
                id -> Registry.MATERIAL.get(NamespacedKey.minecraft(id)),
                id -> {
                    var reg = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
                    return reg.get(NamespacedKey.minecraft(id));
                });
    }

    Paper1_21_xAdapter(
            @NotNull JavaPlugin plugin,
            @NotNull Function<String, @NotNull Material> blockLookup,
            @NotNull Function<String, @NotNull Biome> biomeLookup) {
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
