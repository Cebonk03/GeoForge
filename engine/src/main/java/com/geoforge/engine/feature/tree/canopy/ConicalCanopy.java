package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * A conical (Christmas-tree) canopy — multiple flat circular layers at
 * decreasing radii toward the top. This is the classic spruce/pine shape.
 */
public final class ConicalCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        int layers = Math.min(trunkHeight - 1, 6);
        int startDy = -2;
        int baseY = tipY + 1;

        for (int layer = 0; layer < layers; layer++) {
            int ly = baseY + startDy + layer;
            int radius = Math.max(1, (layers - layer + 1) / 2);

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
        setter.setBlock(tipX, baseY + startDy + layers, tipZ, leafMaterial);
    }
}
