package com.geoforge.engine.feature.tree;

import com.geoforge.engine.feature.BlockSetter;
import java.util.random.RandomGenerator;

/**
 * Strategy for placing tree canopy (leaf) blocks.
 * Each implementation defines a distinct canopy shape.
 */
@FunctionalInterface
public interface CanopyProfile {

    /**
     * Places leaf blocks around the trunk tip.
     *
     * @param setter       block placement abstraction
     * @param tipX         trunk tip X (where canopy attaches)
     * @param tipY         trunk tip Y
     * @param tipZ         trunk tip Z
     * @param trunkHeight  actual placed trunk height
     * @param leafMaterial material name for leaf blocks
     * @param leafDensity  density factor in [0,1]; 1 = fully filled, 0 = no leaves
     * @param random       shared random for stochastic variation
     */
    void place(BlockSetter setter, int tipX, int tipY, int tipZ,
               int trunkHeight, String leafMaterial, double leafDensity,
               RandomGenerator random);
}
