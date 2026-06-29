package com.geoforge.engine.feature.tree.canopy;

import static org.assertj.core.api.Assertions.assertThat;

import com.geoforge.engine.feature.BlockSetter;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Plume canopy tests")
class PlumeCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();
        @Override public void setBlock(int x, int y, int z, String m) { blocks.add(new String[]{String.valueOf(x),String.valueOf(y),String.valueOf(z),m}); }
    }

    @Test @DisplayName("Places blocks in a compact cluster near tip")
    void compactCluster() {
        var r = new RecordingSetter();
        new PlumeCanopy().place(r, 0, 50, 0, 12, "oak_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).isNotEmpty();
        // All blocks should be within 2 blocks of tip
        assertThat(r.blocks).allMatch(b -> {
            int dx = Math.abs(Integer.parseInt(b[0])), dz = Math.abs(Integer.parseInt(b[2]));
            int dy = Integer.parseInt(b[1]) - 51;
            return dx <= 2 && dz <= 2 && dy >= 0 && dy <= 3;
        });
    }

    @Test @DisplayName("Total height does not exceed 4 blocks")
    void totalHeightWithinBounds() {
        var r = new RecordingSetter();
        new PlumeCanopy().place(r, 0, 50, 0, 12, "oak_leaves", 1.0, new SplittableRandom(42));
        var byY = r.blocks.stream().mapToInt(b -> Integer.parseInt(b[1])).max().orElse(51);
        assertThat(byY - 51).isLessThanOrEqualTo(4);
    }

    @Test @DisplayName("Uses correct leaf material")
    void usesCorrectMaterial() {
        var r = new RecordingSetter();
        new PlumeCanopy().place(r, 0, 50, 0, 12, "jungle_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).allMatch(b -> b[3].equals("jungle_leaves"));
    }
}
