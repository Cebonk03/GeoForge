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
@DisplayName("Flat hat canopy tests")
class FlatHatCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();
        @Override public void setBlock(int x, int y, int z, String m) { blocks.add(new String[]{String.valueOf(x),String.valueOf(y),String.valueOf(z),m}); }
    }

    @Test @DisplayName("Brim is wider than crown")
    void brimWiderThanCrown() {
        var r = new RecordingSetter();
        new FlatHatCanopy().place(r, 0, 50, 0, 8, "acacia_leaves", 1.0, new SplittableRandom(42));
        var brim = r.blocks.stream().filter(b -> Integer.parseInt(b[1]) == 51).toList();
        var crown = r.blocks.stream().filter(b -> Integer.parseInt(b[1]) == 52).toList();
        int brimR = brim.stream().mapToInt(b -> Math.max(Math.abs(Integer.parseInt(b[0])), Math.abs(Integer.parseInt(b[2])))).max().orElse(0);
        int crownR = crown.stream().mapToInt(b -> Math.max(Math.abs(Integer.parseInt(b[0])), Math.abs(Integer.parseInt(b[2])))).max().orElse(0);
        assertThat(brimR).isGreaterThan(crownR);
    }

    @Test @DisplayName("Has top center block")
    void hasTopCenter() {
        var r = new RecordingSetter();
        new FlatHatCanopy().place(r, 0, 50, 0, 8, "acacia_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).anyMatch(b -> Integer.parseInt(b[1]) == 53 && Integer.parseInt(b[0]) == 0 && Integer.parseInt(b[2]) == 0);
    }

    @Test @DisplayName("Uses correct leaf material")
    void usesCorrectMaterial() {
        var r = new RecordingSetter();
        new FlatHatCanopy().place(r, 0, 50, 0, 8, "acacia_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).allMatch(b -> b[3].equals("acacia_leaves"));
    }
}
