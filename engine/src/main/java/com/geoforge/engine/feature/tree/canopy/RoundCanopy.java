package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * A round / spherical canopy — leaves fill a sphere centered just above the trunk tip.
 * The sphere radius scales with trunk height: {@code max(2, trunkHeight / 3)}.
 *
 * <p>Interior blocks are always placed; border blocks (within 0.5 of the surface) are
 * placed probabilistically according to {@code leafDensity}.
 */
public final class RoundCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        int radius = Math.max(2, trunkHeight / 3);
        int cx = tipX;
        int cy = tipY + 1;
        int cz = tipZ;

        int rSq = radius * radius;
        int innerRSq = (radius - 1) * (radius - 1);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq <= rSq) {
                        if (distSq <= innerRSq) {
                            // Solid interior — always place
                            setter.setBlock(cx + dx, cy + dy, cz + dz, leafMaterial);
                        } else if (random.nextDouble() < leafDensity) {
                            // Border layer — probabilistic
                            setter.setBlock(cx + dx, cy + dy, cz + dz, leafMaterial);
                        }
                    }
                }
            }
        }
    }
}
