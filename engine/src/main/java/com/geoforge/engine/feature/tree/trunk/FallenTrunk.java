package com.geoforge.engine.feature.tree.trunk;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.TrunkProfile;
import com.geoforge.engine.feature.tree.TrunkResult;
import java.util.random.RandomGenerator;

/**
 * A fallen tree trunk — horizontal log placed on the ground surface.
 * Length varies from 3 to 6 blocks, oriented along either the X or Z axis.
 * A vertical stump block is placed at the start of the log.
 */
public final class FallenTrunk implements TrunkProfile {

    @Override
    public TrunkResult place(BlockSetter setter, int baseX, int baseY, int baseZ,
                             int height, String logMaterial, RandomGenerator random) {
        // Use height parameter as target length (clamped to [3, 6])
        int length = Math.max(3, Math.min(6, height));
        boolean alongX = random.nextBoolean();

        // Place stump
        setter.setBlock(baseX, baseY, baseZ, logMaterial);

        // Place horizontal log
        for (int i = 1; i < length; i++) {
            if (alongX) {
                setter.setBlock(baseX + i, baseY, baseZ, logMaterial);
            } else {
                setter.setBlock(baseX, baseY, baseZ + i, logMaterial);
            }
        }

        // Tip is at the far end of the fallen trunk, ground level
        int tipX = alongX ? baseX + length - 1 : baseX;
        int tipZ = alongX ? baseZ : baseZ + length - 1;
        return new TrunkResult(tipX, baseY, tipZ, 0); // placedHeight=0 indicates fallen
    }
}
