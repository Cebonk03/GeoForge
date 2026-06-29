package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * A wide, spreading canopy — several flat layers with decreasing radii.
 * The bottom layer is the widest (radius = max(2, trunkHeight/2)),
 * giving a broad overhang typical of large shade trees and dark oaks.
 */
public final class SpreadingCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        int baseY = tipY + 1;
        int bottomRadius = Math.max(2, trunkHeight / 2);
        int layers = 3;

        for (int layer = 0; layer < layers; layer++) {
            int ly = baseY + layer;
            int radius = Math.max(1, bottomRadius - layer);

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distSq = dx * dx + dz * dz;
                    if (distSq <= radius * radius) {
                        if (distSq <= (radius - 1) * (radius - 1) || random.nextDouble() < leafDensity) {
                            setter.setBlock(tipX + dx, ly, tipZ + dz, leafMaterial);
                        }
                    }
                }
            }
        }

        // Top center
        setter.setBlock(tipX, baseY + layers, tipZ, leafMaterial);
    }
}
