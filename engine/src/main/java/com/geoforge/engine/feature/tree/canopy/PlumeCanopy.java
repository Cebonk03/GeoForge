package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * A plume canopy — a small, tight cluster of leaves at the very top
 * of a tall, thin trunk. Designed for bamboo jungle trees where
 * foliage concentrates in a small crown high above the ground.
 *
 * The plume is 2-3 blocks tall with radius 1-2, creating a
 * fountain-like or feathery silhouette.
 */
public final class PlumeCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        int baseY = tipY + 1;
        int plumeHeight = Math.min(3, Math.max(2, trunkHeight / 4));
        int plumeRadius = Math.min(2, Math.max(1, trunkHeight / 5));

        for (int dy = 0; dy < plumeHeight; dy++) {
            int radius = plumeRadius - (dy > 0 ? 1 : 0);
            if (radius < 1) radius = 1;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distSq = dx * dx + dz * dz;
                    if (distSq <= radius * radius) {
                        if (distSq <= (radius - 1) * (radius - 1) || random.nextDouble() < leafDensity) {
                            setter.setBlock(tipX + dx, baseY + dy, tipZ + dz, leafMaterial);
                        }
                    }
                }
            }
        }

        // Tip top
        if (random.nextDouble() < leafDensity) {
            setter.setBlock(tipX, baseY + plumeHeight, tipZ, leafMaterial);
        }
    }
}
