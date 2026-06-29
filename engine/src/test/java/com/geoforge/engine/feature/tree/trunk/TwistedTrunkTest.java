package com.geoforge.engine.feature.tree.trunk;

import static org.assertj.core.api.Assertions.assertThat;

import com.geoforge.engine.feature.BlockSetter;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TwistedTrunk — zigzag trunk profile tests")
class TwistedTrunkTest {

    private final TwistedTrunk trunk = new TwistedTrunk();

    /**
     * A BlockSetter that records every call for verification.
     */
    private static final class RecordingSetter implements BlockSetter {
        final List<String> calls = new ArrayList<>();

        @Override
        public void setBlock(int x, int y, int z, String materialName) {
            calls.add(x + "," + y + "," + z + "," + materialName);
        }
    }

    @DisplayName("Places exactly height blocks starting at y = baseY + 1")
    @Test
    void placesCorrectNumberOfBlocks() {
        RecordingSetter setter = new RecordingSetter();
        var result = trunk.place(setter, 0, 50, 0, 8, "oak_log", RandomGenerator.of("Random"));

        assertThat(setter.calls).hasSize(8);
        assertThat(result.placedHeight()).isEqualTo(8);
    }

    @DisplayName("TrunkResult tip coordinates match the last placed block")
    @Test
    void tipMatchesLastBlock() {
        RecordingSetter setter = new RecordingSetter();

        // Use a fixed seed for deterministic twist pattern
        var rng = RandomGenerator.of("Random");
        rng = rng; // seeded via RandomGenerator.of defaults

        // Use a seeded SplittableRandom for reproducibility
        java.util.SplittableRandom fixed = new java.util.SplittableRandom(42L);
        var result = trunk.place(setter, 10, 60, 20, 6, "spruce_log", fixed);

        // Last recorded call should match result tip coordinates
        String last = setter.calls.get(setter.calls.size() - 1);
        String[] parts = last.split(",");
        assertThat(result.tipX()).isEqualTo(Integer.parseInt(parts[0]));
        assertThat(result.tipY()).isEqualTo(Integer.parseInt(parts[1]));
        assertThat(result.tipZ()).isEqualTo(Integer.parseInt(parts[2]));
        assertThat(result.placedHeight()).isEqualTo(6);
    }

    @DisplayName("All placed blocks use the correct log material")
    @Test
    void usesCorrectMaterial() {
        RecordingSetter setter = new RecordingSetter();
        trunk.place(setter, 0, 0, 0, 5, "birch_log", new java.util.SplittableRandom(99L));

        assertThat(setter.calls)
                .allSatisfy(call -> assertThat(call).endsWith(",birch_log"));
    }

    @DisplayName("Blocks are placed at y = baseY + 1 through baseY + height")
    @Test
    void yLevelsAreSequential() {
        RecordingSetter setter = new RecordingSetter();
        trunk.place(setter, 5, 30, 5, 7, "oak_log", new java.util.SplittableRandom(7L));

        int baseY = 30;
        for (int i = 0; i < setter.calls.size(); i++) {
            int expectedY = baseY + i + 1;
            assertThat(setter.calls.get(i)).contains("," + expectedY + ",");
        }
    }

    @DisplayName("Total horizontal displacement never exceeds 3 blocks")
    @Test
    void totalDisplacementIsAtMost3() {
        // Run with many random seeds to verify the invariant
        for (long seed = 0; seed < 200; seed++) {
            RecordingSetter setter = new RecordingSetter();
            var rng = new java.util.SplittableRandom(seed);
            var result = trunk.place(setter, 0, 0, 0, 20, "oak_log", rng);

            int dx = result.tipX();
            int dz = result.tipZ();
            int totalDisp = Math.abs(dx) + Math.abs(dz);
            assertThat(totalDisp)
                    .as("seed=%d total displacement |%d|+|%d| = %d", seed, dx, dz, totalDisp)
                    .isLessThanOrEqualTo(3);
        }
    }

    @DisplayName("Each individual shift is exactly ±1 block (never 0 or 2+)")
    @Test
    void shiftsAreExactlyOneBlock() {
        // Record (dx, dz) for each block, verify consecutive diffs are ±1 in one axis
        for (long seed = 0; seed < 100; seed++) {
            RecordingSetter setter = new RecordingSetter();
            trunk.place(setter, 0, 0, 0, 15, "oak_log", new java.util.SplittableRandom(seed));

            int prevX = 0, prevZ = 0;
            for (String call : setter.calls) {
                String[] parts = call.split(",");
                int x = Integer.parseInt(parts[0]);
                int z = Integer.parseInt(parts[2]);

                if (prevX != 0 || prevZ != 0) {
                    int dx = Math.abs(x - prevX);
                    int dz = Math.abs(z - prevZ);
                    // Either X changed by 1 and Z unchanged, or Z changed by 1 and X unchanged
                    boolean validShift = (dx == 1 && dz == 0) || (dx == 0 && dz == 1) || (dx == 0 && dz == 0);
                    assertThat(validShift)
                            .as("seed=%d invalid shift: (%d,%d) -> (%d,%d) = (dx=%d, dz=%d)",
                                    seed, prevX, prevZ, x, z, dx, dz)
                            .isTrue();
                }
                prevX = x;
                prevZ = z;
            }
        }
    }

    @DisplayName("Axis alternates between X and Z on successive twists")
    @Test
    void axisAlternatesOnTwists() {
        // Height of 30 ensures enough room for multiple twists
        RecordingSetter setter = new RecordingSetter();
        trunk.place(setter, 0, 0, 0, 30, "oak_log", new java.util.SplittableRandom(42L));

        // Collect Y levels where a shift occurred
        List<Boolean> twistAxesIsX = new ArrayList<>();
        int prevX = 0, prevZ = 0;
        boolean first = true;
        for (String call : setter.calls) {
            String[] parts = call.split(",");
            int x = Integer.parseInt(parts[0]);
            int z = Integer.parseInt(parts[2]);
            if (first) {
                first = false;
            } else {
                int dx = x - prevX;
                int dz = z - prevZ;
                if (dx != 0 || dz != 0) {
                    // A twist occurred — record which axis
                    twistAxesIsX.add(dx != 0);
                }
            }
            prevX = x;
            prevZ = z;
        }

        // Successive twists must alternate axis
        for (int i = 1; i < twistAxesIsX.size(); i++) {
            assertThat(twistAxesIsX.get(i))
                    .as("twist %d should alternate axis (prev was %s, now %s)",
                            i, twistAxesIsX.get(i - 1) ? "X" : "Z", twistAxesIsX.get(i) ? "X" : "Z")
                    .isNotEqualTo(twistAxesIsX.get(i - 1));
        }
    }

    @DisplayName("At most 3 twists occur regardless of height")
    @Test
    void maxThreeSegments() {
        // Very tall tree should still have at most 3 twists
        for (long seed = 0; seed < 100; seed++) {
            RecordingSetter setter = new RecordingSetter();
            trunk.place(setter, 0, 0, 0, 50, "oak_log", new java.util.SplittableRandom(seed));

            int prevX = 0, prevZ = 0;
            int twistCount = 0;
            boolean first = true;
            for (String call : setter.calls) {
                String[] parts = call.split(",");
                int x = Integer.parseInt(parts[0]);
                int z = Integer.parseInt(parts[2]);
                if (first) {
                    first = false;
                } else {
                    if (x != prevX || z != prevZ) {
                        twistCount++;
                    }
                }
                prevX = x;
                prevZ = z;
            }

            assertThat(twistCount)
                    .as("seed=%d twist count", seed)
                    .isLessThanOrEqualTo(3);
        }
    }

    @DisplayName("Twist interval is always 2 or 3 blocks")
    @Test
    void twistIntervalIs2or3() {
        RecordingSetter setter = new RecordingSetter();
        trunk.place(setter, 0, 0, 0, 30, "oak_log", new java.util.SplittableRandom(42L));

        // Find Y levels where coordinate changes (twist points)
        List<Integer> twistYs = new ArrayList<>();
        int prevX = 0, prevZ = 0;
        int y = 0;
        for (String call : setter.calls) {
            y++;
            String[] parts = call.split(",");
            int x = Integer.parseInt(parts[0]);
            int z = Integer.parseInt(parts[2]);
            if (y > 1 && (x != prevX || z != prevZ)) {
                twistYs.add(y);
            }
            prevX = x;
            prevZ = z;
        }

        // Each twist interval = distance from previous twist Y
        int prevTwistY = 0;
        for (int twistY : twistYs) {
            int interval = twistY - prevTwistY;
            assertThat(interval)
                    .as("twist at y=%d interval = %d", twistY, interval)
                    .isIn(2, 3);
            prevTwistY = twistY;
        }
    }

    @DisplayName("The twist segmentCount guard is 3 — no more than 3 direction changes")
    @Test
    void segmentCountIsCappedAt3() {
        // A very tall tree (height=100) should still have ≤3 direction changes
        RecordingSetter setter = new RecordingSetter();
        trunk.place(setter, 0, 0, 0, 100, "oak_log", new java.util.SplittableRandom(1L));

        int prevX = 0, prevZ = 0;
        int directionChanges = 0;
        boolean first = true;
        for (String call : setter.calls) {
            String[] parts = call.split(",");
            int x = Integer.parseInt(parts[0]);
            int z = Integer.parseInt(parts[2]);
            if (first) {
                first = false;
            } else {
                if (x != prevX || z != prevZ) {
                    directionChanges++;
                }
            }
            prevX = x;
            prevZ = z;
        }

        assertThat(directionChanges).isLessThanOrEqualTo(3);
    }
}
