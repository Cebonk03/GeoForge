package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * A domed canopy — hemispherical (flat bottom, rounded top).
 * The flat circular base sits at the center of the trunk tip.
 * Upper layers form a dome with decreasing radius toward the top.
 */
public final class DomedCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        int radius = Math.max(2, trunkHeight / 3);
        int baseY = tipY + 1;

        // Bottom (dy=0): full circle
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    setter.setBlock(tipX + dx, baseY, tipZ + dz, leafMaterial);
                }
            }
        }

        // Upper layers (dy>0): hemisphere
        for (int dy = 1; dy <= radius; dy++) {
            int layerRadius = (int) Math.sqrt(radius * radius - dy * dy);
            for (int dx = -layerRadius; dx <= layerRadius; dx++) {
                for (int dz = -layerRadius; dz <= layerRadius; dz++) {
                    double distSq = dx * dx + dz * dz + dy * dy;
                    if (distSq <= radius * radius) {
                        double innerDist = dx * dx + dz * dz + (dy - 1) * (dy - 1);
                        if (innerDist <= (radius - 1) * (radius - 1) || random.nextDouble() < leafDensity) {
                            setter.setBlock(tipX + dx, baseY + dy, tipZ + dz, leafMaterial);
                        }
                    }
                }
            }
        }
    }
}
