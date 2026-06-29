package com.geoforge.engine.feature.tree.trunk;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.TrunkProfile;
import com.geoforge.engine.feature.tree.TrunkResult;
import java.util.random.RandomGenerator;

/**
 * A trunk that gradually leans in one direction — shifts approximately 1 block
 * per 3-4 blocks of height, creating a subtle angled appearance.
 * The maximum horizontal displacement is capped at height / 3.
 */
public final class LeaningTrunk implements TrunkProfile {

    @Override
    public TrunkResult place(BlockSetter setter, int baseX, int baseY, int baseZ,
                             int height, String logMaterial, RandomGenerator random) {
        int dir = random.nextInt(4);
        int dx = (dir == 1) ? 1 : (dir == 3) ? -1 : 0;
        int dz = (dir == 0) ? 1 : (dir == 2) ? -1 : 0;

        int leanStep = 3 + random.nextInt(2); // 3 or 4
        int maxShift = Math.max(1, height / 3);
        int totalShift = Math.min(height / leanStep, maxShift);

        for (int dy = 1; dy <= height; dy++) {
            int shift = Math.min(dy / leanStep, maxShift);
            int x = baseX + dx * Math.min(shift, totalShift);
            int z = baseZ + dz * Math.min(shift, totalShift);
            setter.setBlock(x, baseY + dy, z, logMaterial);
        }

        int tipX = baseX + dx * totalShift;
        int tipZ = baseZ + dz * totalShift;
        return new TrunkResult(tipX, baseY + height, tipZ, height);
    }
}
