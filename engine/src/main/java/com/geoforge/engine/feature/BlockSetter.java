package com.geoforge.engine.feature;

/**
 * Abstracts block placement for feature generation.
 *
 * <p>This interface allows the feature system to remain Bukkit-independent. In the
 * plugin module, a lambda wrapping {@code ChunkData.setBlock()} adapts the engine's
 * feature placers to the actual server implementation.
 */
@FunctionalInterface
public interface BlockSetter {

    /**
     * Places a block at the given world coordinates.
     *
     * @param x            the world x-coordinate
     * @param y            the world y-coordinate
     * @param z            the world z-coordinate
     * @param materialName the Minecraft material name (e.g. "oak_log", "grass_block")
     */
    void setBlock(int x, int y, int z, String materialName);
}
