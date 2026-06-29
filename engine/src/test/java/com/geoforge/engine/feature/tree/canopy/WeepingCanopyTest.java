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
@DisplayName("Weeping canopy tests")
class WeepingCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();
        @Override public void setBlock(int x, int y, int z, String m) { blocks.add(new String[]{String.valueOf(x),String.valueOf(y),String.valueOf(z),m}); }
    }

    @Test @DisplayName("Places blocks at Y levels below tip")
    void placesBelowTip() {
        var r = new RecordingSetter();
        new WeepingCanopy().place(r, 0, 50, 0, 9, "mangrove_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).isNotEmpty();
        assertThat(r.blocks).anyMatch(b -> Integer.parseInt(b[1]) <= 51); // at or below tip+1
    }

    @Test @DisplayName("Has top cluster (blocks above tip+1)")
    void hasTopCluster() {
        var r = new RecordingSetter();
        new WeepingCanopy().place(r, 0, 50, 0, 9, "mangrove_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).anyMatch(b -> Integer.parseInt(b[1]) > 51);
    }

    @Test @DisplayName("Uses correct leaf material")
    void usesCorrectMaterial() {
        var r = new RecordingSetter();
        new WeepingCanopy().place(r, 0, 50, 0, 9, "mangrove_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).allMatch(b -> b[3].equals("mangrove_leaves"));
    }
}
