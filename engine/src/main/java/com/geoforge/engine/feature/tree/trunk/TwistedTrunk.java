package com.geoforge.engine.feature.tree.trunk;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.TrunkProfile;
import com.geoforge.engine.feature.tree.TrunkResult;
import java.util.random.RandomGenerator;

/**
 * A trunk with a twisted / zigzag growth pattern — shifts by ±1 block
 * in alternating directions every 2-3 layers. Creates a twisted appearance
 * without exceeding 3 blocks of total horizontal displacement.
 */
public final class TwistedTrunk implements TrunkProfile {

    @Override
    public TrunkResult place(BlockSetter setter, int baseX, int baseY, int baseZ,
                             int height, String logMaterial, RandomGenerator random) {
        int curDx = 0, curDz = 0;
        int segmentCount = 0;
        boolean useX = true;
        int twistInterval = 2 + random.nextInt(2); // 2 or 3

        for (int dy = 1; dy <= height; dy++) {
            if (dy > 1 && (dy - 1) % twistInterval == 0 && segmentCount < 3) {
                // Change direction at each twist point
                if (useX) {
                    curDx += (random.nextBoolean() ? 1 : -1);
                } else {
                    curDz += (random.nextBoolean() ? 1 : -1);
                }
                useX = !useX; // alternate axis
                segmentCount++;
            }
            setter.setBlock(baseX + curDx, baseY + dy, baseZ + curDz, logMaterial);
        }

        return new TrunkResult(baseX + curDx, baseY + height, baseZ + curDz, height);
    }
}
