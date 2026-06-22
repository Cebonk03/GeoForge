package com.geoforge.api.adapter;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter interface that encapsulates Paper API version differences.
 * Each server version range gets its own implementation, selected by {@link
 * com.geoforge.api.version.AdapterFactory}.
 *
 * <p>All implementations use {@code RegistryAccess} for biome resolution (not {@code
 * Registry.BIOME}, which is removed in 26.x) and {@code RegionScheduler} for task scheduling
 * (works on both Paper and Folia).
 */
public interface GeoForgeAdapter {

    /**
     * Maps an engine-internal block identifier to a Paper {@link Material}.
     *
     * @param engineId the engine block identifier (e.g. {@code "stone"}, {@code "grass_block"})
     * @return the resolved Material, never null; falls back to {@link Material#STONE} for
     *     unknown identifiers
     */
    @NotNull Material mapBlock(@NotNull String engineId);

    /**
     * Maps an engine-internal biome identifier to a Paper {@link Biome}.
     *
     * @param engineId the engine biome identifier (e.g. {@code "plains"}, {@code "desert"})
     * @return the resolved Biome, never null; falls back to {@code plains} for unknown
     *     identifiers
     * @throws IllegalStateException if the {@code plains} biome itself is missing from the
     *     server registry (indicates a corrupt server installation)
     */
    @NotNull Biome mapBiome(@NotNull String engineId);

    /**
     * Schedules a task to run on the owning region's thread at the given location. On vanilla
     * Paper this routes to the main thread; on Folia it routes to the region thread.
     *
     * @param loc the location that determines which region owns the task
     * @param run the task to execute
     */
    void scheduleTask(@NotNull Location loc, @NotNull Runnable run);

    /**
     * Returns whether the server is running the Folia threading model.
     *
     * @return true if Folia's RegionizedServer class is present on the classpath
     */
    boolean isFolia();
}
