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
@DisplayName("Sparse canopy tests")
class SparseCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();
        @Override public void setBlock(int x, int y, int z, String m) { blocks.add(new String[]{String.valueOf(x),String.valueOf(y),String.valueOf(z),m}); }
    }

    @Test @DisplayName("Full density (1.0) places blocks")
    void fullDensity() {
        var r = new RecordingSetter();
        new SparseCanopy().place(r, 0, 50, 0, 9, "oak_leaves", 1.0, new SplittableRandom(42));
        assertThat(r.blocks).isNotEmpty();
    }

    @Test @DisplayName("Zero density (0.0) places no blocks")
    void zeroDensity() {
        var r = new RecordingSetter();
        new SparseCanopy().place(r, 0, 50, 0, 9, "oak_leaves", 0.0, new SplittableRandom(42));
        assertThat(r.blocks).isEmpty();
    }

    @Test @DisplayName("Low density places fewer blocks than full density")
    void lowDensityFewerBlocks() {
        var full = new RecordingSetter();
        new SparseCanopy().place(full, 0, 50, 0, 9, "oak_leaves", 1.0, new SplittableRandom(42));
        var sparse = new RecordingSetter();
        new SparseCanopy().place(sparse, 0, 50, 0, 9, "oak_leaves", 0.3, new SplittableRandom(42));
        assertThat(sparse.blocks.size()).isLessThan(full.blocks.size());
    }

    @Test @DisplayName("Uses correct leaf material")
    void usesCorrectMaterial() {
        var r = new RecordingSetter();
        new SparseCanopy().place(r, 0, 50, 0, 9, "oak_leaves", 0.5, new SplittableRandom(42));
        assertThat(r.blocks).allMatch(b -> b[3].equals("oak_leaves"));
    }
}
