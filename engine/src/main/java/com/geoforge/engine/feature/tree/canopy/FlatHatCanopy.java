package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * A flat hat canopy — large flat brim with a smaller crown above it.
 * This is the iconic acacia / umbrella thorn silhouette:
 * a broad flat disc topped with a compact crown.
 */
public final class FlatHatCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        int baseY = tipY + 1;
        int brimRadius = Math.min(2 + trunkHeight / 4, 4);

        // Brim: flat circle at base level
        for (int dx = -brimRadius; dx <= brimRadius; dx++) {
            for (int dz = -brimRadius; dz <= brimRadius; dz++) {
                double distSq = dx * dx + dz * dz;
                if (distSq <= brimRadius * brimRadius) {
                    if (distSq <= (brimRadius - 1) * (brimRadius - 1) || random.nextDouble() < leafDensity) {
                        setter.setBlock(tipX + dx, baseY, tipZ + dz, leafMaterial);
                    }
                }
            }
        }

        // Crown: smaller circle at baseY+1
        int crownRadius = Math.max(1, brimRadius - 2);
        for (int dx = -crownRadius; dx <= crownRadius; dx++) {
            for (int dz = -crownRadius; dz <= crownRadius; dz++) {
                if (dx * dx + dz * dz <= crownRadius * crownRadius) {
                    setter.setBlock(tipX + dx, baseY + 1, tipZ + dz, leafMaterial);
                }
            }
        }

        // Top center block
        setter.setBlock(tipX, baseY + 2, tipZ, leafMaterial);
    }
}
