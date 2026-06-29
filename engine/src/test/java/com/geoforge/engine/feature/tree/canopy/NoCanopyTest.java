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
@DisplayName("NoCanopy (dead tree) tests")
class NoCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();
        @Override public void setBlock(int x, int y, int z, String m) { blocks.add(new String[]{String.valueOf(x),String.valueOf(y),String.valueOf(z),m}); }
    }

    @Test @DisplayName("Places zero leaf blocks")
    void placesZeroBlocks() {
        var r = new RecordingSetter();
        new NoCanopy().place(r, 0, 50, 0, 10, "oak_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).isEmpty();
    }
}
