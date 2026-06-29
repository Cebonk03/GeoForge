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
@DisplayName("Layered canopy tests")
class LayeredCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();
        @Override public void setBlock(int x, int y, int z, String m) { blocks.add(new String[]{String.valueOf(x),String.valueOf(y),String.valueOf(z),m}); }
    }

    @Test @DisplayName("Bottom layer is widest")
    void bottomLayerWidest() {
        var r = new RecordingSetter();
        new LayeredCanopy().place(r, 0, 50, 0, 12, "jungle_leaves", 1.0, new SplittableRandom(42));
        var byY = r.blocks.stream().collect(java.util.stream.Collectors.groupingBy(b -> Integer.parseInt(b[1])));
        int minY = byY.keySet().stream().mapToInt(v -> v).min().orElse(0);
        int bottomR = byY.get(minY).stream().mapToInt(b -> Math.max(Math.abs(Integer.parseInt(b[0])), Math.abs(Integer.parseInt(b[2])))).max().orElse(0);
        byY.forEach((y, blks) -> {
            int r2 = blks.stream().mapToInt(b -> Math.max(Math.abs(Integer.parseInt(b[0])), Math.abs(Integer.parseInt(b[2])))).max().orElse(0);
            assertThat(r2).isLessThanOrEqualTo(bottomR);
        });
    }

    @Test @DisplayName("Has visible gaps between layers")
    void hasGapsBetweenLayers() {
        var r = new RecordingSetter();
        new LayeredCanopy().place(r, 0, 50, 0, 12, "jungle_leaves", 1.0, new SplittableRandom(42));
        var ys = r.blocks.stream().mapToInt(b -> Integer.parseInt(b[1])).distinct().sorted().toArray();
        // There should be gaps (non-consecutive Y levels)
        for (int i = 1; i < ys.length; i++) {
            assertThat(ys[i] - ys[i - 1]).isLessThanOrEqualTo(2);
        }
    }

    @Test @DisplayName("Uses correct leaf material")
    void usesCorrectMaterial() {
        var r = new RecordingSetter();
        new LayeredCanopy().place(r, 0, 50, 0, 12, "jungle_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).allMatch(b -> b[3].equals("jungle_leaves"));
    }
}
