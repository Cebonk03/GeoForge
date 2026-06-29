package com.geoforge.engine.feature.tree.canopy;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.CanopyProfile;
import java.util.random.RandomGenerator;

/**
 * An oval (vertically elongated) canopy — shaped like an ellipsoid.
 * The vertical radius is larger than the horizontal radius,
 * creating a taller, narrower silhouette than {@link RoundCanopy}.
 */
public final class OvalCanopy implements CanopyProfile {

    @Override
    public void place(BlockSetter setter, int tipX, int tipY, int tipZ,
                      int trunkHeight, String leafMaterial, double leafDensity,
                      RandomGenerator random) {
        int rH = Math.max(2, trunkHeight / 4);
        int rV = Math.max(2, trunkHeight / 3);
        int centerY = tipY + 1;

        for (int dx = -rH; dx <= rH; dx++) {
            for (int dy = -rV; dy <= rV; dy++) {
                for (int dz = -rH; dz <= rH; dz++) {
                    double dist = (double)(dx * dx) / (rH * rH)
                            + (double)(dy * dy) / (rV * rV)
                            + (double)(dz * dz) / (rH * rH);
                    if (dist <= 1.0) {
                        // Apply leafDensity probability for outer shell
                        double innerDist = (double)(dx * dx) / ((rH - 1) * (rH - 1))
                                + (double)(dy * dy) / ((rV - 1) * (rV - 1))
                                + (double)(dz * dz) / ((rH - 1) * (rH - 1));
                        if (innerDist <= 1.0 || random.nextDouble() < leafDensity) {
                            setter.setBlock(tipX + dx, centerY + dy, tipZ + dz, leafMaterial);
                        }
                    }
                }
            }
        }
    }
}
