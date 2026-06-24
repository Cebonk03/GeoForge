package com.geoforge.engine.density;

/**
 * Interface for river carving in the 3D density field.
 *
 * <p>Implementations modify density values to carve river valleys, gorges, and
 * canyons into the terrain. The interface receives the original density and
 * returns the modified density. This allows different carving strategies to
 * be swapped in without changing the engine.
 *
 * <p>The {@link NoopRiverCarver} implementation returns density unchanged,
 * acting as a safe default.
 */
@FunctionalInterface
public interface RiverCarver {

    /**
     * Returns the carved density at the given position.
     *
     * @param density the original density value (positive = solid, negative = air)
     * @param blockX  the x-coordinate in block space
     * @param blockY  the y-coordinate in block space
     * @param blockZ  the z-coordinate in block space
     * @return the modified density after river carving
     */
    double carve(double density, int blockX, int blockY, int blockZ);
}
