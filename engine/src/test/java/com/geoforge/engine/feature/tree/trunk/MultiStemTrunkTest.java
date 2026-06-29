package com.geoforge.engine.feature.tree.trunk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.TrunkResult;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Multi-stem trunk placement")
class MultiStemTrunkTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();

        @Override
        public void setBlock(int x, int y, int z, String materialName) {
            blocks.add(new String[]{String.valueOf(x), String.valueOf(y), String.valueOf(z), materialName});
        }
    }

    private static final RecordingSetter SETTER = new RecordingSetter();
    private static final RandomGenerator RNG = RandomGenerator.getDefault();
    private static final MultiStemTrunk TRUNK = new MultiStemTrunk();

    @DisplayName("places 4 * height log blocks (2x2 cross-section) at consecutive Y levels starting from baseY + 1")
    @Test
    void placesCorrectNumberOfBlocksAtExpectedHeights() {
        SETTER.blocks.clear();
        TrunkResult result = TRUNK.place(SETTER, 10, 50, 20, 3, "dark_oak_log", RNG);

        // 3 layers × 4 blocks each = 12 total
        assertThat(SETTER.blocks).hasSize(12);

        // Each layer has the same 4 (x,z) positions
        String[][] expectedOffsets = {
            {"10", "20"},     // (baseX,     baseZ)
            {"11", "20"},     // (baseX + 1, baseZ)
            {"10", "21"},     // (baseX,     baseZ + 1)
            {"11", "21"},     // (baseX + 1, baseZ + 1)
        };

        for (int layer = 0; layer < 3; layer++) {
            int expectedY = 51 + layer; // baseY + 1 + layer
            for (int offset = 0; offset < 4; offset++) {
                String[] block = SETTER.blocks.get(layer * 4 + offset);
                assertThat(block[0]).isEqualTo(expectedOffsets[offset][0]); // x
                assertThat(block[1]).isEqualTo(String.valueOf(expectedY));  // y
                assertThat(block[2]).isEqualTo(expectedOffsets[offset][1]); // z
                assertThat(block[3]).isEqualTo("dark_oak_log");
            }
        }
    }

    @DisplayName("returns tip at center of 2x2 with tipY = baseY + height")
    @Test
    void returnsCorrectTipPosition() {
        SETTER.blocks.clear();
        TrunkResult result = TRUNK.place(SETTER, 0, 60, 0, 7, "birch_log", RNG);

        assertThat(result.tipX()).isEqualTo(1);
        assertThat(result.tipY()).isEqualTo(67);
        assertThat(result.tipZ()).isEqualTo(1);
        assertThat(result.placedHeight()).isEqualTo(7);
    }

    @DisplayName("tip is at (baseX+1, baseY+height, baseZ+1) — center of 2x2")
    @Test
    void tipAtCenterOf2x2() {
        SETTER.blocks.clear();
        TrunkResult result = TRUNK.place(SETTER, 100, 40, -50, 3, "spruce_log", RNG);

        assertThat(result.tipX()).isEqualTo(101);
        assertThat(result.tipZ()).isEqualTo(-49);
        assertThat(result.placedHeight()).isEqualTo(3);
    }

    @DisplayName("placedHeight equals requested height")
    @Test
    void placedHeightMatchesHeight() {
        SETTER.blocks.clear();
        TrunkResult result = TRUNK.place(SETTER, 5, 70, 5, 10, "jungle_log", RNG);
        assertThat(result.placedHeight()).isEqualTo(10);
    }

    @DisplayName("places no blocks when height is zero")
    @Test
    void placesNoBlocksWhenHeightZero() {
        SETTER.blocks.clear();
        TrunkResult result = TRUNK.place(SETTER, 0, 50, 0, 0, "oak_log", RNG);

        assertThat(SETTER.blocks).isEmpty();
        assertThat(result.tipY()).isEqualTo(50);
        assertThat(result.placedHeight()).isEqualTo(0);
    }

    @DisplayName("uses supplied log material for all blocks")
    @Test
    void usesCorrectLogMaterial() {
        SETTER.blocks.clear();
        TRUNK.place(SETTER, 0, 50, 0, 2, "dark_oak_log", RNG);

        assertThat(SETTER.blocks).hasSize(8);
        for (String[] block : SETTER.blocks) {
            assertThat(block[3]).isEqualTo("dark_oak_log");
        }
    }

    @DisplayName("deterministic — ignores random and produces identical placements")
    @Test
    void deterministicIgnoresRandom() {
        SETTER.blocks.clear();
        TrunkResult r1 = TRUNK.place(SETTER, 0, 50, 0, 4, "oak_log", RandomGenerator.of("Random"));

        SETTER.blocks.clear();
        TrunkResult r2 = TRUNK.place(SETTER, 0, 50, 0, 4, "oak_log", RandomGenerator.of("Random"));

        assertThat(r1).isEqualTo(r2);
    }

    @DisplayName("each 2x2 layer contains four distinct positions")
    @Test
    void eachLayerHasFourDistinctPositions() {
        SETTER.blocks.clear();
        TRUNK.place(SETTER, 5, 60, 5, 1, "oak_log", RNG);

        assertThat(SETTER.blocks).hasSize(4);
        // All four blocks should have the same Y
        assertThat(SETTER.blocks.get(0)[1]).isEqualTo("61");
        assertThat(SETTER.blocks.get(1)[1]).isEqualTo("61");
        assertThat(SETTER.blocks.get(2)[1]).isEqualTo("61");
        assertThat(SETTER.blocks.get(3)[1]).isEqualTo("61");

        // Collect unique (x,z) pairs — should be 4 distinct
        long distinctPositions = SETTER.blocks.stream()
                .map(b -> b[0] + "," + b[2])
                .distinct()
                .count();
        assertThat(distinctPositions).isEqualTo(4);
    }
}
