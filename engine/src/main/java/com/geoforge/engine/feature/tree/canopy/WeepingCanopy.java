package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * A weeping canopy — narrow at the top, expanding outward and downward
 * to create a drooping, trailing silhouette. Suitable for swamp trees,
 * mangroves, and weeping willows.
 *
 * The canopy has three zones:
 * <ol>
 *   <li>Top cluster: dense sphere (radius 1-2)</li>
 *   <li>Middle ring: widest point, extends outward</li>
 *   <li>Lower skirt: drips downward with decreasing radius</li>
 * </ol>
 */
public final class WeepingCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        int baseY = tipY + 1;
        int topRadius = Math.min(2, Math.max(1, trunkHeight / 5));
        int skirtRadius = Math.min(4, Math.max(2, trunkHeight / 3));
        int weepLength = Math.min(3, Math.max(1, trunkHeight / 4));

        // Top cluster (dense)
        for (int dx = -topRadius; dx <= topRadius; dx++) {
            for (int dy = -topRadius; dy <= topRadius; dy++) {
                for (int dz = -topRadius; dz <= topRadius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= topRadius * topRadius) {
                        setter.setBlock(tipX + dx, baseY + dy, tipZ + dz, leafMaterial);
                    }
                }
            }
        }

        // Middle ring (widest)
        for (int dx = -skirtRadius; dx <= skirtRadius; dx++) {
            for (int dz = -skirtRadius; dz <= skirtRadius; dz++) {
                double distSq = dx * dx + dz * dz;
                if (distSq <= skirtRadius * skirtRadius && distSq >= topRadius * topRadius / 2) {
                    if (random.nextDouble() < leafDensity) {
                        setter.setBlock(tipX + dx, baseY - 1, tipZ + dz, leafMaterial);
                    }
                }
            }
        }

        // Lower skirt (weeping downward)
        for (int w = 1; w <= weepLength; w++) {
            int weepR = Math.max(1, skirtRadius - w);
            for (int dx = -weepR; dx <= weepR; dx++) {
                for (int dz = -weepR; dz <= weepR; dz++) {
                    double distSq = dx * dx + dz * dz;
                    if (distSq <= weepR * weepR && random.nextDouble() < leafDensity * 0.7) {
                        setter.setBlock(tipX + dx, baseY - 1 - w, tipZ + dz, leafMaterial);
                    }
                }
            }
        }
    }
}
