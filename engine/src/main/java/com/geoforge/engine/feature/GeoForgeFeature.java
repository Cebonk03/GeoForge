package com.geoforge.engine.feature;

import java.util.random.RandomGenerator;

/**
 * Functional interface for surface feature placement (trees, vegetation, etc.).
 *
 * <p>Each feature placer is called once per surface column in a chunk, after the
 * surface block and sub-surface blocks have been placed. Implementations receive
 * a {@link BlockSetter} abstraction to remain Bukkit-independent.
 */
@FunctionalInterface
public interface GeoForgeFeature {

    /**
     * Places a feature at or near the given surface column.
     *
     * @param setter   block placement abstraction (wraps {@code ChunkData.setBlock()}
     *                 in the plugin module)
     * @param blockX   world x-coordinate of the column
     * @param blockZ   world z-coordinate of the column
     * @param surfaceY surface height at this column (the top solid block)
     * @param biomeId  biome ID at this column
     * @param random   shared random for stochastic placement decisions
     */
    void place(BlockSetter setter, int blockX, int blockZ, int surfaceY,
               String biomeId, RandomGenerator random);
}
