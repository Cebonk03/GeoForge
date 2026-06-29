package com.geoforge.engine.feature.tree.trunk;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.TrunkProfile;
import com.geoforge.engine.feature.tree.TrunkResult;
import java.util.random.RandomGenerator;

/**
 * A trunk that grows straight for most of its height, then bends in one direction.
 * The bend occurs at approximately 2/3 of the target height, shifting by 1 block
 * in a random cardinal direction for the remaining 1/3.
 */
public final class BentTrunk implements TrunkProfile {

    @Override
    public TrunkResult place(BlockSetter setter, int baseX, int baseY, int baseZ,
                             int height, String logMaterial, RandomGenerator random) {
        // Straight portion: first 2/3
        int bendY = (height * 2) / 3;
        if (bendY < 1) bendY = 1;

        // Place straight portion
        for (int dy = 1; dy <= bendY; dy++) {
            setter.setBlock(baseX, baseY + dy, baseZ, logMaterial);
        }

        // Direction of bend
        int dir = random.nextInt(4); // 0=+Z, 1=+X, 2=-Z, 3=-X
        int dx = (dir == 1) ? 1 : (dir == 3) ? -1 : 0;
        int dz = (dir == 0) ? 1 : (dir == 2) ? -1 : 0;

        // Transition block at bend point — extra log at old position on new Y level
        setter.setBlock(baseX + dx, baseY + bendY, baseZ + dz, logMaterial);

        // Remaining portion after bend
        int remaining = height - bendY;
        for (int dy = 1; dy <= remaining; dy++) {
            setter.setBlock(baseX + dx, baseY + bendY + dy, baseZ + dz, logMaterial);
        }

        int tipX = baseX + dx;
        int tipZ = baseZ + dz;
        int tipY = baseY + height;
        return new TrunkResult(tipX, tipY, tipZ, height);
    }
}
