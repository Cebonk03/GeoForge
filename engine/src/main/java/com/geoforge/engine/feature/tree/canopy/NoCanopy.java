package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * A no-op canopy that places zero leaf blocks.
 * Used for dead trees, fallen logs, or any tree variant
 * that should have only a trunk without foliage.
 */
public final class NoCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        // No blocks placed — dead tree
    }
}
