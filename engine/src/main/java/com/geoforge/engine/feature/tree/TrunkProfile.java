package com.geoforge.engine.feature.tree;

import com.geoforge.engine.feature.BlockSetter;
import java.util.random.RandomGenerator;

/**
 * Strategy for placing tree trunk blocks.
 * Each implementation defines a distinct trunk growth pattern.
 */
@FunctionalInterface
public interface TrunkProfile {

    /**
     * Places trunk blocks and returns the resulting tip position and placed height.
     *
     * @param setter      block placement abstraction
     * @param baseX       base X of the trunk (column center)
     * @param baseY       base Y (ground level, trunk starts at baseY + 1)
     * @param baseZ       base Z of the trunk
     * @param height      target trunk height in blocks
     * @param logMaterial material name for log blocks
     * @param random      shared random for stochastic variation
     * @return the tip coordinates and actual placed height
     */
    TrunkResult place(BlockSetter setter, int baseX, int baseY, int baseZ,
                      int height, String logMaterial, RandomGenerator random);
}
