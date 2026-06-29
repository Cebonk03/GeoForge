package com.geoforge.engine.feature.tree.trunk;

import static org.assertj.core.api.Assertions.assertThat;

import com.geoforge.engine.feature.BlockSetter;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("unit")
@DisplayName("BentTrunk tests")
class BentTrunkTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();

        @Override
        public void setBlock(int x, int y, int z, String materialName) {
            blocks.add(new String[]{String.valueOf(x), String.valueOf(y), String.valueOf(z), materialName});
        }

        void reset() { blocks.clear(); }
    }

    @DisplayName("Total placed blocks equals height + 1 (includes transition block)")
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 4, 9, 10, 15})
    void totalPlacedBlocksIsHeightPlusOne(int height) {
        var recorder = new RecordingSetter();
        var trunk = new BentTrunk();
        trunk.place(recorder, 0, 0, 0, height, "oak_log", new SplittableRandom(42));
        assertThat(recorder.blocks).hasSize(height + 1);
    }

    @DisplayName("Bend occurs at ~2/3 height and shifts by 1 block in a cardinal direction")
    @ParameterizedTest
    @ValueSource(ints = {3, 4, 9, 10})
    void bendShiftsByOneBlock(int height) {
        var recorder = new RecordingSetter();
        var trunk = new BentTrunk();
        trunk.place(recorder, 5, 10, 7, height, "oak_log", new SplittableRandom(42));

        int bendY = Math.max((height * 2) / 3, 1);

        // Straight portion remains at base position
        for (int i = 0; i < bendY; i++) {
            var b = recorder.blocks.get(i);
            assertThat(Integer.parseInt(b[0])).as("straight block %d x", i).isEqualTo(5);
            assertThat(Integer.parseInt(b[2])).as("straight block %d z", i).isEqualTo(7);
            assertThat(Integer.parseInt(b[1])).as("straight block %d y", i).isEqualTo(10 + i + 1);
            assertThat(b[3]).as("straight block %d material", i).isEqualTo("oak_log");
        }

        // Transition block at bend: offset by exactly 1 in a cardinal direction
        var transition = recorder.blocks.get(bendY);
        int dx = Integer.parseInt(transition[0]) - 5;
        int dz = Integer.parseInt(transition[2]) - 7;
        assertThat(Math.abs(dx) + Math.abs(dz)).as("bend must be exactly 1 block in X or Z").isEqualTo(1);
        assertThat(Integer.parseInt(transition[1])).as("transition block Y").isEqualTo(10 + bendY);
        assertThat(transition[3]).as("transition material").isEqualTo("oak_log");

        // Remaining portion at offset position
        int remaining = height - bendY;
        for (int i = 0; i < remaining; i++) {
            var b = recorder.blocks.get(bendY + 1 + i);
            assertThat(Integer.parseInt(b[0])).as("bent block %d x", i).isEqualTo(5 + dx);
            assertThat(Integer.parseInt(b[2])).as("bent block %d z", i).isEqualTo(7 + dz);
            assertThat(Integer.parseInt(b[1])).as("bent block %d y", i).isEqualTo(10 + bendY + i + 1);
            assertThat(b[3]).as("bent block %d material", i).isEqualTo("oak_log");
        }
    }

    @DisplayName("Returned tip position matches topmost placed block")
    @ParameterizedTest
    @ValueSource(ints = {3, 4, 9, 10})
    void tipMatchesLastBlock(int height) {
        var recorder = new RecordingSetter();
        var trunk = new BentTrunk();
        var result = trunk.place(recorder, 5, 10, 7, height, "oak_log", new SplittableRandom(42));

        var last = recorder.blocks.get(recorder.blocks.size() - 1);
        assertThat(result.tipX()).isEqualTo(Integer.parseInt(last[0]));
        assertThat(result.tipY()).isEqualTo(Integer.parseInt(last[1]));
        assertThat(result.tipZ()).isEqualTo(Integer.parseInt(last[2]));
        assertThat(result.placedHeight()).isEqualTo(height);
    }

    @DisplayName("All placed blocks use the specified log material")
    @Test
    void allBlocksUseCorrectMaterial() {
        var recorder = new RecordingSetter();
        var trunk = new BentTrunk();
        trunk.place(recorder, 0, 0, 0, 9, "oak_log", new SplittableRandom(42));
        assertThat(recorder.blocks)
                .allSatisfy(b -> assertThat(b[3]).isEqualTo("oak_log"));
    }
}
