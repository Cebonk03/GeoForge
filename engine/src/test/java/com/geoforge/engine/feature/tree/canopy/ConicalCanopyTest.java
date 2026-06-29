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
@DisplayName("Conical canopy tests")
class ConicalCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();
        @Override public void setBlock(int x, int y, int z, String m) { blocks.add(new String[]{String.valueOf(x),String.valueOf(y),String.valueOf(z),m}); }
    }

    @Test @DisplayName("Layers decrease in radius upward")
    void layersDecreaseUpward() {
        var r = new RecordingSetter();
        new ConicalCanopy().place(r, 0, 50, 0, 9, "spruce_leaves", 1.0, new SplittableRandom(42));
        var byY = r.blocks.stream().collect(java.util.stream.Collectors.groupingBy(b -> Integer.parseInt(b[1])));
        int prevR = Integer.MAX_VALUE;
        for (int y = 49; y <= 55; y++) {
            var layer = byY.get(y);
            if (layer != null) {
                int maxR = layer.stream().mapToInt(b -> Math.max(Math.abs(Integer.parseInt(b[0])), Math.abs(Integer.parseInt(b[2])))).max().orElse(0);
                assertThat(maxR).isLessThanOrEqualTo(prevR);
                prevR = maxR;
            }
        }
    }

    @Test @DisplayName("Has a top cap block")
    void hasTopCap() {
        var r = new RecordingSetter();
        new ConicalCanopy().place(r, 0, 50, 0, 9, "spruce_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).anyMatch(b -> Integer.parseInt(b[1]) >= 55 && b[3].equals("spruce_leaves"));
    }

    @Test @DisplayName("Uses correct leaf material")
    void usesCorrectMaterial() {
        var r = new RecordingSetter();
        new ConicalCanopy().place(r, 0, 50, 0, 9, "spruce_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).allMatch(b -> b[3].equals("spruce_leaves"));
    }
}
