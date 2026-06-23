package com.geoforge.adapters.v26_x;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.api.util.FoliaDetector;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.function.Function;
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
    private final Function<@NotNull String, Material> blockLookup;
    private final Function<@NotNull String, Biome> biomeLookup;

    /**
     * Creates an adapter using the live Paper 26.x server registries.
     */
    public Paper26xAdapter(@NotNull JavaPlugin plugin) {
        this(plugin,
                id -> Registry.MATERIAL.get(NamespacedKey.minecraft(id)),
                id -> {
                    var reg = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
                    return reg.get(NamespacedKey.minecraft(id));
                });
    }

    /**
     * Package-private constructor for testing with injected lookup functions.
     * Allows unit tests to verify block and biome mapping without a live 26.x registry.
     *
     * @param plugin      the plugin instance
     * @param blockLookup  maps engine block IDs to Paper Materials (null = not found)
     * @param biomeLookup  maps engine biome IDs to Paper Biomes (null = not found)
     */
    Paper26xAdapter(
            @NotNull JavaPlugin plugin,
            @NotNull Function<@NotNull String, Material> blockLookup,
            @NotNull Function<@NotNull String, Biome> biomeLookup) {
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
