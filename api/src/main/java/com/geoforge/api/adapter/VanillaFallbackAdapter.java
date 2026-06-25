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
import java.util.function.Function;

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
    private final Function<String, Material> blockLookup;
    private final Function<String, Biome> biomeLookup;

    /**
     * Creates a fallback adapter with the given plugin.
     * Uses hardcoded fallback mappings: always STONE and plains biome.
     *
     * @param plugin the owning plugin
     */
    public VanillaFallbackAdapter(@NotNull JavaPlugin plugin) {
        this(plugin, id -> Material.STONE, id -> {
            var reg = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
            Biome biome = reg.get(NamespacedKey.minecraft("plains"));
            if (biome == null) {
                throw new IllegalStateException(
                        "plains biome missing — corrupt server installation on "
                                + Bukkit.getMinecraftVersion());
            }
            return biome;
        });
    }

    /**
     * Package-private constructor for testing.
     *
     * @param plugin      the owning plugin
     * @param blockLookup function mapping engine block IDs to Materials
     * @param biomeLookup function mapping engine biome IDs to Biomes
     */
    VanillaFallbackAdapter(@NotNull JavaPlugin plugin,
                           @NotNull Function<String, Material> blockLookup,
                           @NotNull Function<String, Biome> biomeLookup) {
        this.plugin = plugin;
        this.blockLookup = blockLookup;
        this.biomeLookup = biomeLookup;
    }

    @Override
    public @NotNull Material mapBlock(@NotNull String engineId) {
        Material block = blockLookup.apply(engineId);
        return block != null ? block : Material.STONE;
    }

    @Override
    public @NotNull Biome mapBiome(@NotNull String engineId) {
        Biome biome = biomeLookup.apply(engineId);
        if (biome == null) {
            throw new IllegalStateException(
                    "biome lookup failed for " + engineId + " — corrupt server installation on "
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
