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
@DisplayName("Straight trunk placement")
class StraightTrunkTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();

        @Override
        public void setBlock(int x, int y, int z, String materialName) {
            blocks.add(new String[]{String.valueOf(x), String.valueOf(y), String.valueOf(z), materialName});
        }
    }

    private static final RecordingSetter SETTER = new RecordingSetter();
    private static final RandomGenerator RNG = RandomGenerator.getDefault();
    private static final StraightTrunk TRUNK = new StraightTrunk();

    @DisplayName("places height log blocks at consecutive Y levels starting from baseY + 1")
    @Test
    void placesCorrectNumberOfBlocksAtExpectedHeights() {
        SETTER.blocks.clear();
        TrunkResult result = TRUNK.place(SETTER, 10, 50, 20, 5, "oak_log", RNG);

        assertThat(SETTER.blocks).hasSize(5);
        for (int i = 0; i < 5; i++) {
            String[] block = SETTER.blocks.get(i);
            assertThat(block[0]).isEqualTo("10");  // x
            assertThat(block[1]).isEqualTo(String.valueOf(51 + i)); // y = baseY + 1 + i
            assertThat(block[2]).isEqualTo("20");  // z
            assertThat(block[3]).isEqualTo("oak_log");
        }
    }

    @DisplayName("returns tipY = baseY + height")
    @Test
    void returnsCorrectTipY() {
        SETTER.blocks.clear();
        TrunkResult result = TRUNK.place(SETTER, 0, 60, 0, 7, "birch_log", RNG);

        assertThat(result.tipY()).isEqualTo(67);
        assertThat(result.tipX()).isEqualTo(0);
        assertThat(result.tipZ()).isEqualTo(0);
        assertThat(result.placedHeight()).isEqualTo(7);
    }

    @DisplayName("no horizontal displacement — tipX = baseX, tipZ = baseZ")
    @Test
    void noHorizontalDisplacement() {
        SETTER.blocks.clear();
        TrunkResult result = TRUNK.place(SETTER, 100, 40, -50, 3, "spruce_log", RNG);

        assertThat(result.tipX()).isEqualTo(100);
        assertThat(result.tipZ()).isEqualTo(-50);
        assertThat(result.placedHeight()).isEqualTo(3);

        for (String[] block : SETTER.blocks) {
            assertThat(block[0]).isEqualTo("100");   // x unchanged
            assertThat(block[2]).isEqualTo("-50");   // z unchanged
        }
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

    @DisplayName("uses supplied log material")
    @Test
    void usesCorrectLogMaterial() {
        SETTER.blocks.clear();
        TRUNK.place(SETTER, 0, 50, 0, 2, "dark_oak_log", RNG);

        assertThat(SETTER.blocks).hasSize(2);
        assertThat(SETTER.blocks.get(0)[3]).isEqualTo("dark_oak_log");
        assertThat(SETTER.blocks.get(1)[3]).isEqualTo("dark_oak_log");
    }

    @DisplayName("deterministic — ignores random")
    @Test
    void deterministicIgnoresRandom() {
        SETTER.blocks.clear();
        TrunkResult r1 = TRUNK.place(SETTER, 0, 50, 0, 4, "oak_log", RandomGenerator.of("Random"));

        SETTER.blocks.clear();
        TrunkResult r2 = TRUNK.place(SETTER, 0, 50, 0, 4, "oak_log", RandomGenerator.of("Random"));

        assertThat(r1).isEqualTo(r2);
    }
}
