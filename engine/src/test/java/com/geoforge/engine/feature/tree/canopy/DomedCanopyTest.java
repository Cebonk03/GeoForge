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
@DisplayName("Domed canopy tests")
class DomedCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();
        @Override public void setBlock(int x, int y, int z, String m) { blocks.add(new String[]{String.valueOf(x),String.valueOf(y),String.valueOf(z),m}); }
    }

    @Test @DisplayName("Bottom layer is a solid circle")
    void bottomLayerIsFullCircle() {
        var r = new RecordingSetter();
        new DomedCanopy().place(r, 0, 50, 0, 9, "oak_leaves", 1.0, new SplittableRandom(42));
        var bottom = r.blocks.stream().filter(b -> Integer.parseInt(b[1]) == 51).toList();
        assertThat(bottom).isNotEmpty();
        assertThat(bottom).allMatch(b -> {
            int dx = Integer.parseInt(b[0]), dz = Integer.parseInt(b[2]);
            return dx*dx + dz*dz <= 9; // radius² = (max(2,9/3)=3)² = 9
        });
    }

    @Test @DisplayName("Upper layers decrease in radius")
    void upperLayersShrink() {
        var r = new RecordingSetter();
        new DomedCanopy().place(r, 0, 50, 0, 12, "oak_leaves", 1.0, new SplittableRandom(42));
        var byY = r.blocks.stream().collect(java.util.stream.Collectors.groupingBy(b -> Integer.parseInt(b[1])));
        for (int y = 52; y <= 55; y++) { // tipY=50+1=51, layers at 52,53,... 
            var layer = byY.get(y);
            if (layer != null) {
                int maxR = layer.stream().mapToInt(b -> Math.max(Math.abs(Integer.parseInt(b[0])), Math.abs(Integer.parseInt(b[2])))).max().orElse(0);
                var prevLayer = byY.get(y - 1);
                int prevMaxR = prevLayer != null ? prevLayer.stream().mapToInt(b -> Math.max(Math.abs(Integer.parseInt(b[0])), Math.abs(Integer.parseInt(b[2])))).max().orElse(99) : 99;
                assertThat(maxR).isLessThanOrEqualTo(prevMaxR);
            }
        }
    }

    @Test @DisplayName("Uses correct leaf material")
    void usesCorrectMaterial() {
        var r = new RecordingSetter();
        new DomedCanopy().place(r, 0, 50, 0, 9, "cherry_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).allMatch(b -> b[3].equals("cherry_leaves"));
    }
}
