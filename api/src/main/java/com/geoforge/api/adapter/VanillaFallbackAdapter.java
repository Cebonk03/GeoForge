package com.geoforge.api.adapter;

import com.geoforge.api.util.FoliaDetector;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * A degraded adapter used when the server version is not in the supported range. Provides
 * minimal functionality — always returns {@link Material#STONE} and the plains biome.
 *
 * <p>This class lives in the {@code api} module (not alongside version-specific adapters in
 * the {@code adapters/} directory) because it has no version-specific dependencies — it must
 * compile and function on any server version. Keeping it here avoids a circular dependency
 * between {@code plugin} (which selects adapters) and the adapter implementations.
 */
public final class VanillaFallbackAdapter implements GeoForgeAdapter {

    private final JavaPlugin plugin;

    public VanillaFallbackAdapter(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Material mapBlock(@NotNull String engineId) {
        return Material.STONE;
    }

    @Override
    public @NotNull Biome mapBiome(@NotNull String engineId) {
        var reg = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        Biome biome = reg.get(NamespacedKey.minecraft("plains"));
        if (biome == null) {
            throw new IllegalStateException(
                    "plains biome missing — corrupt server installation on "
                            + Bukkit.getMinecraftVersion());
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
