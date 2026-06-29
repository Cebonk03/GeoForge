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
@DisplayName("Spreading canopy tests")
class SpreadingCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();
        @Override public void setBlock(int x, int y, int z, String m) { blocks.add(new String[]{String.valueOf(x),String.valueOf(y),String.valueOf(z),m}); }
    }

    @Test @DisplayName("Bottom layer is widest, radii decrease upward")
    void radiiDecreaseUpward() {
        var r = new RecordingSetter();
        new SpreadingCanopy().place(r, 0, 50, 0, 10, "dark_oak_leaves", 1.0, new SplittableRandom(42));
        var byY = r.blocks.stream().collect(java.util.stream.Collectors.groupingBy(b -> Integer.parseInt(b[1])));
        int prevR = Integer.MAX_VALUE;
        for (int y = 51; y <= 54; y++) {
            var layer = byY.get(y);
            if (layer != null) {
                int maxR = layer.stream().mapToInt(b -> Math.max(Math.abs(Integer.parseInt(b[0])), Math.abs(Integer.parseInt(b[2])))).max().orElse(0);
                assertThat(maxR).isLessThanOrEqualTo(prevR);
                prevR = maxR;
            }
        }
    }

    @Test @DisplayName("Has top center block")
    void hasTopCenter() {
        var r = new RecordingSetter();
        new SpreadingCanopy().place(r, 0, 50, 0, 10, "dark_oak_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).anyMatch(b -> Integer.parseInt(b[1]) > 53 && b[3].equals("dark_oak_leaves"));
    }

    @Test @DisplayName("Uses correct leaf material")
    void usesCorrectMaterial() {
        var r = new RecordingSetter();
        new SpreadingCanopy().place(r, 0, 50, 0, 8, "acacia_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).allMatch(b -> b[3].equals("acacia_leaves"));
    }
}
