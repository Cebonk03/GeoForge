package com.geoforge.engine.feature.tree.trunk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.feature.BlockSetter;
import com.geoforge.engine.feature.tree.TrunkResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Fallen trunk placement")
class FallenTrunkTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();

        @Override
        public void setBlock(int x, int y, int z, String materialName) {
            blocks.add(new String[]{String.valueOf(x), String.valueOf(y), String.valueOf(z), materialName});
        }
    }

    private static final RecordingSetter SETTER = new RecordingSetter();
    private static final FallenTrunk TRUNK = new FallenTrunk();

    /** Returns a RandomGenerator whose nextBoolean always returns the given value. */
    private static RandomGenerator fixedRng(boolean value) {
        return new RandomGenerator() {
            @Override public boolean nextBoolean() { return value; }
            @Override public long nextLong() { return 0L; }
            @Override public int nextInt() { return 0; }
            @Override public float nextFloat() { return 0f; }
            @Override public double nextDouble() { return 0d; }
            @Override public void nextBytes(byte[] bytes) { Arrays.fill(bytes, (byte) 0); }
        };
    }

    @DisplayName("places (height) blocks along X axis including stump when alongX=true")
    @Test
    void placesBlocksAlongXAxis() {
        SETTER.blocks.clear();
        TRUNK.place(SETTER, 10, 50, 20, 5, "oak_log", fixedRng(true));

        assertThat(SETTER.blocks).hasSize(5); // stump + 4 horizontal = length=5
        // Stump at start
        assertThat(SETTER.blocks.get(0)).containsExactly("10", "50", "20", "oak_log");
        // Horizontal blocks along +X
        for (int i = 1; i < 5; i++) {
            String[] block = SETTER.blocks.get(i);
            assertThat(block[0]).isEqualTo(String.valueOf(10 + i));
            assertThat(block[1]).isEqualTo("50");
            assertThat(block[2]).isEqualTo("20");
            assertThat(block[3]).isEqualTo("oak_log");
        }
    }

    @DisplayName("places blocks along Z axis when alongX=false")
    @Test
    void placesBlocksAlongZAxis() {
        SETTER.blocks.clear();
        TRUNK.place(SETTER, 10, 50, 20, 4, "spruce_log", fixedRng(false));

        assertThat(SETTER.blocks).hasSize(4);
        // Stump at start
        assertThat(SETTER.blocks.get(0)).containsExactly("10", "50", "20", "spruce_log");
        // Horizontal blocks along +Z
        for (int i = 1; i < 4; i++) {
            String[] block = SETTER.blocks.get(i);
            assertThat(block[0]).isEqualTo("10");
            assertThat(block[1]).isEqualTo("50");
            assertThat(block[2]).isEqualTo(String.valueOf(20 + i));
            assertThat(block[3]).isEqualTo("spruce_log");
        }
    }

    @DisplayName("returns tip at far end with placedHeight=0")
    @Test
    void returnsCorrectTipAlongX() {
        SETTER.blocks.clear();
        TrunkResult result = TRUNK.place(SETTER, 0, 60, 0, 6, "birch_log", fixedRng(true));

        assertThat(result.tipX()).isEqualTo(5);  // 0 + 6 - 1
        assertThat(result.tipY()).isEqualTo(60); // ground level
        assertThat(result.tipZ()).isEqualTo(0);
        assertThat(result.placedHeight()).isEqualTo(0);
    }

    @DisplayName("returns correct tip along Z axis")
    @Test
    void returnsCorrectTipAlongZ() {
        SETTER.blocks.clear();
        TrunkResult result = TRUNK.place(SETTER, 5, 70, 10, 4, "jungle_log", fixedRng(false));

        assertThat(result.tipX()).isEqualTo(5);
        assertThat(result.tipY()).isEqualTo(70);
        assertThat(result.tipZ()).isEqualTo(13); // 10 + 4 - 1
        assertThat(result.placedHeight()).isEqualTo(0);
    }

    @DisplayName("clamps length to [3, 6] for height below 3")
    @Test
    void clampsLengthToMinimum() {
        SETTER.blocks.clear();
        TRUNK.place(SETTER, 0, 50, 0, 1, "oak_log", fixedRng(true));

        // length clamped to 3 (minimum)
        assertThat(SETTER.blocks).hasSize(3);
        assertThat(SETTER.blocks.get(0)[0]).isEqualTo("0");
        assertThat(SETTER.blocks.get(1)[0]).isEqualTo("1");
        assertThat(SETTER.blocks.get(2)[0]).isEqualTo("2");
    }

    @DisplayName("clamps length to [3, 6] for height above 6")
    @Test
    void clampsLengthToMaximum() {
        SETTER.blocks.clear();
        TRUNK.place(SETTER, 10, 50, 10, 10, "oak_log", fixedRng(true));

        // length clamped to 6 (maximum)
        assertThat(SETTER.blocks).hasSize(6);
        for (int i = 0; i < 6; i++) {
            assertThat(SETTER.blocks.get(i)[0]).isEqualTo(String.valueOf(10 + i));
        }
    }

    @DisplayName("uses supplied log material for all blocks")
    @Test
    void usesCorrectLogMaterial() {
        SETTER.blocks.clear();
        TRUNK.place(SETTER, 0, 50, 0, 4, "dark_oak_log", fixedRng(true));

        assertThat(SETTER.blocks).hasSize(4);
        for (String[] block : SETTER.blocks) {
            assertThat(block[3]).isEqualTo("dark_oak_log");
        }
    }

    @DisplayName("deterministic with same seed — same random orientation gives same result")
    @Test
    void deterministicWithSeed() {
        SETTER.blocks.clear();
        RandomGenerator rng = java.util.random.RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
        TrunkResult r1 = TRUNK.place(SETTER, 0, 50, 0, 5, "oak_log", rng);

        SETTER.blocks.clear();
        rng = java.util.random.RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
        TrunkResult r2 = TRUNK.place(SETTER, 0, 50, 0, 5, "oak_log", rng);

        assertThat(r1).isEqualTo(r2);
    }

    @DisplayName("stump is always at base position")
    @Test
    void stumpAtBasePosition() {
        SETTER.blocks.clear();
        TRUNK.place(SETTER, -3, 45, 7, 4, "oak_log", fixedRng(false));

        // First block is the stump at base position
        assertThat(SETTER.blocks.get(0)[0]).isEqualTo("-3");
        assertThat(SETTER.blocks.get(0)[1]).isEqualTo("45");
        assertThat(SETTER.blocks.get(0)[2]).isEqualTo("7");
        assertThat(SETTER.blocks.get(0)[3]).isEqualTo("oak_log");
    }
}
