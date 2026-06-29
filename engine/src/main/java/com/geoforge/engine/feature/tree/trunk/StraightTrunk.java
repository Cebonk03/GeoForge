package com.geoforge.engine.feature.tree.trunk;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.TrunkProfile;
import com.geoforge.engine.feature.tree.TrunkResult;
import java.util.random.RandomGenerator;

/**
 * A straight vertical trunk — logs placed in a column from {@code baseY + 1} to
 * {@code baseY + height}. This is the most common trunk pattern, used for oaks,
 * birches, and most standard trees.
 */
public final class StraightTrunk implements TrunkProfile {

    @Override
    public TrunkResult place(BlockSetter setter, int baseX, int baseY, int baseZ,
                             int height, String logMaterial, RandomGenerator random) {
        for (int dy = 1; dy <= height; dy++) {
            setter.setBlock(baseX, baseY + dy, baseZ, logMaterial);
        }
        return new TrunkResult(baseX, baseY + height, baseZ, height);
    }
}
