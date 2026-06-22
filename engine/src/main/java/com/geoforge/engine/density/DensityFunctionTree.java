package com.geoforge.engine.density;

/**
 * A composable density function tree, inspired by Minecraft's density function system.
 *
 * <p>Each node in the tree produces a {@code double} value from 3D coordinates. Nodes can be
 * combined to create complex terrain height formulas.
 */
@FunctionalInterface
public interface DensityFunctionTree {

    /**
     * Samples the density function at the given coordinates.
     *
     * @param x the x-coordinate (block space)
     * @param y the y-coordinate (block space)
     * @param z the z-coordinate (block space)
     * @return the density value at this point
     */
    double sample(double x, double y, double z);
}
