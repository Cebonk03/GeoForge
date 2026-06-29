package com.geoforge.engine.feature.tree.trunk;

import static org.assertj.core.api.Assertions.assertThat;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.TrunkResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("LeaningTrunk tests")
class LeaningTrunkTest {

    private static final String LOG = "oak_log";

    private final LeaningTrunk trunk = new LeaningTrunk();

    @DisplayName("Leans in the positive Z direction when dir == 0")
    @Test
    void leanPositiveZ() {
        var records = new ArrayList<BlockRecord>();
        BlockSetter setter = (x, y, z, m) -> records.add(new BlockRecord(x, y, z, m));
        RandomGenerator rng = new FixedRng(0, 0); // dir=0 (+Z), leanStep=3

        TrunkResult result = trunk.place(setter, 0, 50, 0, 7, LOG, rng);

        assertThat(result.tipX()).isZero();
        assertThat(result.tipZ()).isPositive();
        assertThat(result.tipY()).isEqualTo(57);
        assertThat(result.placedHeight()).isEqualTo(7);

        // Verify blocks shift in +Z as height increases
        int baseZ = 0;
        for (var rec : records) {
            assertThat(rec.material).isEqualTo(LOG);
            assertThat(rec.x).isZero();
            assertThat(rec.z).isGreaterThanOrEqualTo(baseZ);
            baseZ = rec.z;
        }
        // The top block should be at the maximum Z offset
        assertThat(records.get(records.size() - 1).z).isEqualTo(result.tipZ());
    }

    @DisplayName("Leans in the positive X direction when dir == 1")
    @Test
    void leanPositiveX() {
        var records = new ArrayList<BlockRecord>();
        BlockSetter setter = (x, y, z, m) -> records.add(new BlockRecord(x, y, z, m));
        RandomGenerator rng = new FixedRng(1, 0); // dir=1 (+X), leanStep=3

        trunk.place(setter, 0, 50, 0, 7, LOG, rng);

        for (var rec : records) {
            assertThat(rec.z).isZero();
        }
        // Some blocks should have shifted in +X
        assertThat(records.stream().anyMatch(r -> r.x > 0)).isTrue();
    }

    @DisplayName("Leans in the negative Z direction when dir == 2")
    @Test
    void leanNegativeZ() {
        var records = new ArrayList<BlockRecord>();
        BlockSetter setter = (x, y, z, m) -> records.add(new BlockRecord(x, y, z, m));
        RandomGenerator rng = new FixedRng(2, 0); // dir=2 (-Z), leanStep=3

        trunk.place(setter, 0, 50, 0, 7, LOG, rng);

        for (var rec : records) {
            assertThat(rec.x).isZero();
            assertThat(rec.z).isLessThanOrEqualTo(0);
        }
        assertThat(records.stream().anyMatch(r -> r.z < 0)).isTrue();
    }

    @DisplayName("Leans in the negative X direction when dir == 3")
    @Test
    void leanNegativeX() {
        var records = new ArrayList<BlockRecord>();
        BlockSetter setter = (x, y, z, m) -> records.add(new BlockRecord(x, y, z, m));
        RandomGenerator rng = new FixedRng(3, 0); // dir=3 (-X), leanStep=3

        trunk.place(setter, 0, 50, 0, 7, LOG, rng);

        for (var rec : records) {
            assertThat(rec.z).isZero();
            assertThat(rec.x).isLessThanOrEqualTo(0);
        }
        assertThat(records.stream().anyMatch(r -> r.x < 0)).isTrue();
    }

    @DisplayName("Gradual shift: displacement increases with height")
    @Test
    void gradualShift() {
        var records = new ArrayList<BlockRecord>();
        BlockSetter setter = (x, y, z, m) -> records.add(new BlockRecord(x, y, z, m));
        RandomGenerator rng = new FixedRng(0, 1); // dir=+Z, leanStep=4

        trunk.place(setter, 100, 50, 100, 12, LOG, rng);

        // With height=12, leanStep=4: maxShift=4, totalShift = min(12/4=3, 4)=3
        // shift=Math.min(dy/4, 4), capped by totalShift=3
        // dy=1-3: z=100, dy=4-7: z=101, dy=8-11: z=102, dy=12: z=103
        for (var rec : records) {
            int dy = rec.y - 50;
            int expectedZ = 100 + Math.min(dy / 4, 3);
            assertThat(rec.z)
                    .as("Block at y=%d should have z=%d", rec.y, expectedZ)
                    .isEqualTo(expectedZ);
        }
    }

    @DisplayName("Maximum horizontal displacement is capped at height / 3")
    @Test
    void maxDisplacementCapped() {
        var records = new ArrayList<BlockRecord>();
        BlockSetter setter = (x, y, z, m) -> records.add(new BlockRecord(x, y, z, m));
        RandomGenerator rng = new FixedRng(1, 0); // dir=+X, leanStep=3

        TrunkResult result = trunk.place(setter, 0, 50, 0, 20, LOG, rng);

        // height=20: maxShift = max(1, 20/3=6) = 6
        // totalShift = min(20/3=6, 6) = 6
        // Tip X should be at most 6
        assertThat(result.tipX()).isBetween(1, 6);
        // No block should exceed the cap
        for (var rec : records) {
            assertThat(rec.x).as("Block x offset at y=%d", rec.y)
                    .isLessThanOrEqualTo(6);
        }
    }

    @DisplayName("TrunkResult tip position matches the topmost placed block")
    @Test
    void tipMatchesTopBlock() {
        var records = new ArrayList<BlockRecord>();
        BlockSetter setter = (x, y, z, m) -> records.add(new BlockRecord(x, y, z, m));
        RandomGenerator rng = new FixedRng(0, 0); // dir=+Z, leanStep=3

        TrunkResult result = trunk.place(setter, 10, 60, 20, 8, LOG, rng);

        var topBlock = records.get(records.size() - 1);
        assertThat(result.tipX()).isEqualTo(topBlock.x);
        assertThat(result.tipY()).isEqualTo(topBlock.y);
        assertThat(result.tipZ()).isEqualTo(topBlock.z);
        assertThat(result.placedHeight()).isEqualTo(8);
    }

    @DisplayName("Short trunk (height=3) still produces a valid lean")
    @Test
    void shortTrunk() {
        var records = new ArrayList<BlockRecord>();
        BlockSetter setter = (x, y, z, m) -> records.add(new BlockRecord(x, y, z, m));
        RandomGenerator rng = new FixedRng(1, 1); // dir=+X, leanStep=4

        TrunkResult result = trunk.place(setter, 0, 50, 0, 3, LOG, rng);

        // height=3: maxShift = max(1, 3/3=1) = 1
        // totalShift = min(3/4=0, 1) = 0
        // No lean for a height-3 trunk with leanStep=4
        assertThat(result.tipX()).isZero();
        assertThat(records).hasSize(3);
        assertThat(result.placedHeight()).isEqualTo(3);
    }

    @DisplayName("All blocks are placed at Y = baseY + dy progression")
    @Test
    void yProgression() {
        var records = new ArrayList<BlockRecord>();
        BlockSetter setter = (x, y, z, m) -> records.add(new BlockRecord(x, y, z, m));
        RandomGenerator rng = new FixedRng(0, 0); // dir=+Z, leanStep=3

        trunk.place(setter, 0, 70, 0, 6, LOG, rng);

        // Y values should be 71, 72, 73, 74, 75, 76 (baseY + 1 through baseY + height)
        for (int i = 0; i < records.size(); i++) {
            assertThat(records.get(i).y).isEqualTo(71 + i);
        }
    }

    @DisplayName("All blocks use the same log material")
    @Test
    void consistentMaterial() {
        var records = new ArrayList<BlockRecord>();
        BlockSetter setter = (x, y, z, m) -> records.add(new BlockRecord(x, y, z, m));
        RandomGenerator rng = new FixedRng(2, 0); // dir=-Z, leanStep=3

        trunk.place(setter, 0, 50, 0, 10, "birch_log", rng);

        assertThat(records).allMatch(r -> r.material.equals("birch_log"));
    }

    // ----------------------------------------------------------------
    // Fixed RandomGenerator for deterministic testing
    // ----------------------------------------------------------------
    /**
     * A RandomGenerator that returns predetermined values:
     * first call to nextInt(n) -> {@code dir},
     * second call to nextInt(n) -> {@code step}.
     */
    private static final class FixedRng implements RandomGenerator {
        private final int dir;
        private final int step;
        private int callCount;

        FixedRng(int dir, int step) {
            this.dir = dir;
            this.step = step;
        }

        @Override
        public long nextLong() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int nextInt(int bound) {
            if (callCount == 0) {
                callCount++;
                return dir;
            }
            return step;
        }
    }

    private record BlockRecord(int x, int y, int z, String material) {}
}
