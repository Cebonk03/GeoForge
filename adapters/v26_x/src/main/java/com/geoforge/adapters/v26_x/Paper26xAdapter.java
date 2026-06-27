package com.geoforge.adapters.v26_x;

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
 * Adapter for Paper 26.x server versions (26.1, 26.1.2, 26.2 alpha).
 *
 * <p>Uses {@link RegistryAccess} for biome resolution (the {@code Registry.BIOME} static
 * field is removed in 26.x). {@code Registry.MATERIAL} still exists in 26.x and is used
 * for block resolution. Scheduling is done via {@code RegionScheduler}, which works on
 * both Paper and Folia.
 *
 * <p>This class uses Java 25 language features and must be compiled with the Java 25
 * toolchain. Most implementation is inherited from {@link AbstractPaperAdapter}.
 */
public final class Paper26xAdapter extends AbstractPaperAdapter {

    /**
     * Creates an adapter using the live Paper 26.x server registries.
     */
    public Paper26xAdapter(@NotNull JavaPlugin plugin) {
        super(plugin,
                id -> Registry.MATERIAL.get(NamespacedKey.minecraft(id)),
                id -> RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.BIOME)
                        .get(NamespacedKey.minecraft(id)));
    }

    /**
     * Package-private constructor for testing with injected lookup functions.
     * Allows unit tests to verify block mapping without a live 26.x registry.
     *
     * @param plugin      the plugin instance
     * @param blockLookup  maps engine block IDs to Paper Materials (null = not found)
     * @param biomeLookup  maps engine biome IDs to Paper Biomes (null = not found)
     */
    Paper26xAdapter(
            @NotNull JavaPlugin plugin,
            @NotNull Function<String, Material> blockLookup,
            @NotNull Function<String, Biome> biomeLookup) {
        super(plugin, blockLookup, biomeLookup);
    }
}
