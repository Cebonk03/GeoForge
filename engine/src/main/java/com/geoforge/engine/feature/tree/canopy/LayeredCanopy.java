package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * A layered canopy — 3-4 distinct flat circular tiers at decreasing
 * radii toward the top, with vertical gaps between layers.
 * This creates a pagoda-like silhouette typical of jungle trees.
 */
public final class LayeredCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        int baseY = tipY + 1;
        int layers = Math.min(3 + trunkHeight / 5, 5);

        for (int layer = 0; layer < layers; layer++) {
            int ly = baseY - 1 + layer * 2; // gap of 1 between layers
            int radius = Math.max(1, layers - layer); // bottom = widest

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        if (dx * dx + dz * dz <= (radius - 1) * (radius - 1)
                                || random.nextDouble() < leafDensity) {
                            setter.setBlock(tipX + dx, ly, tipZ + dz, leafMaterial);
                        }
                    }
                }
            }
        }

        // Top cap
        int topY = baseY - 1 + (layers - 1) * 2;
        setter.setBlock(tipX, topY + 1, tipZ, leafMaterial);
    }
}
