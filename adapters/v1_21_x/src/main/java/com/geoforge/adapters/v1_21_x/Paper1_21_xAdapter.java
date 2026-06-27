package com.geoforge.adapters.v1_21_x;

import com.geoforge.api.adapter.AbstractPaperAdapter;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.function.Function;

/**
 * Adapter for Paper 1.21.x server versions (1.21.4 through 1.21.11).
 *
 * <p>Uses {@link RegistryAccess} for biome resolution (not the removed {@code
 * Registry.BIOME}) to maintain binary compatibility with 26.x. Scheduling is done via
 * {@code RegionScheduler}, which works on both Paper and Folia.
 *
 * <p>Most implementation is inherited from {@link AbstractPaperAdapter}.
 */
public final class Paper1_21_xAdapter extends AbstractPaperAdapter {

    /**
     * Creates an adapter using the live Paper 1.21.x server registries.
     */
    public Paper1_21_xAdapter(@NotNull JavaPlugin plugin) {
        super(plugin,
                id -> Registry.MATERIAL.get(NamespacedKey.minecraft(id)),
                id -> {
                    var reg = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
                    return reg.get(NamespacedKey.minecraft(id));
                });
    }

    /**
     * Package-private constructor for testing with injected lookup functions.
     *
     * @param plugin      the plugin instance
     * @param blockLookup  maps engine block IDs to Paper Materials (null = not found)
     * @param biomeLookup  maps engine biome IDs to Paper Biomes (null = not found)
     */
    Paper1_21_xAdapter(
            @NotNull JavaPlugin plugin,
            @NotNull Function<String, Material> blockLookup,
            @NotNull Function<String, Biome> biomeLookup) {
        super(plugin, blockLookup, biomeLookup);
    }
}
