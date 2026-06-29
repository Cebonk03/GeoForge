package com.geoforge.engine.feature.tree.trunk;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.TrunkProfile;
import com.geoforge.engine.feature.tree.TrunkResult;
import java.util.random.RandomGenerator;

/**
 * A multi-stemmed trunk with a 2×2 cross-section — four logs placed at each layer.
 * Used for large trees like dark oaks and mega jungle/spruce trees.
 */
public final class MultiStemTrunk implements TrunkProfile {

    @Override
    public TrunkResult place(BlockSetter setter, int baseX, int baseY, int baseZ,
                             int height, String logMaterial, RandomGenerator random) {
        for (int dy = 1; dy <= height; dy++) {
            setter.setBlock(baseX,     baseY + dy, baseZ,     logMaterial);
            setter.setBlock(baseX + 1, baseY + dy, baseZ,     logMaterial);
            setter.setBlock(baseX,     baseY + dy, baseZ + 1, logMaterial);
            setter.setBlock(baseX + 1, baseY + dy, baseZ + 1, logMaterial);
        }
        // Return center of 2×2 trunk for correct canopy positioning
        return new TrunkResult(baseX + 1, baseY + height, baseZ + 1, height);
    }
}
