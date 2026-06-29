package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * A sparse canopy — leaves are placed within a spherical bounding box,
 * but each individual leaf block has a {@code leafDensity} probability
 * of being placed. This creates natural gaps and a translucent canopy.
 */
public final class SparseCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        int radius = Math.max(2, trunkHeight / 3);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq <= radius * radius && random.nextDouble() < leafDensity) {
                        setter.setBlock(tipX + dx, tipY + 1 + dy, tipZ + dz, leafMaterial);
                    }
                }
            }
        }
    }
}
