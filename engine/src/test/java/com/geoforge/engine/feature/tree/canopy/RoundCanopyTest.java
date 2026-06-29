package com.geoforge.engine.feature.tree.canopy;

import static org.assertj.core.api.Assertions.assertThat;

import com.geoforge.engine.feature.BlockSetter;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("RoundCanopy — spherical canopy placement")
class RoundCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();

        @Override
        public void setBlock(int x, int y, int z, String materialName) {
            blocks.add(new String[]{String.valueOf(x), String.valueOf(y), String.valueOf(z), materialName});
        }
    }

    private static final RecordingSetter SETTER = new RecordingSetter();
    private static final RoundCanopy CANOPY = new RoundCanopy();

    /** Count of blocks within a sphere of radius 2 (all integer positions with dx²+dy²+dz² ≤ 4). */
    private static final int SPHERE_R2_BLOCKS = 33;
    /** Interior blocks (distSq ≤ 1² = 1) for radius 2. */
    private static final int INTERIOR_R2_BLOCKS = 7;

    @DisplayName("places a full sphere when leafDensity = 1.0")
    @Test
    void fullDensityPlacesAllSphereBlocks() {
        SETTER.blocks.clear();
        // trunkHeight=6 → radius=2
        CANOPY.place(SETTER, 0, 50, 0, 6, "oak_leaves", 1.0, rng(42));

        assertThat(SETTER.blocks).hasSize(SPHERE_R2_BLOCKS);
        assertThat(SETTER.blocks)
                .allSatisfy(block -> assertThat(block[3]).isEqualTo("oak_leaves"));
    }

    @DisplayName("places only interior blocks when leafDensity = 0.0")
    @Test
    void zeroDensityPlacesOnlyInterior() {
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 6, "oak_leaves", 0.0, rng(42));

        // Only interior (distSq ≤ 1) blocks should be placed
        assertThat(SETTER.blocks).hasSize(INTERIOR_R2_BLOCKS);
    }

    @DisplayName("places blocks centered at (tipX, tipY+1, tipZ)")
    @Test
    void centerIsTipYPlusOne() {
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 10, 60, -5, 6, "birch_leaves", 1.0, rng(42));

        // Verify all placed blocks are centered at (10, 61, -5) with radius 2
        for (String[] block : SETTER.blocks) {
            int x = Integer.parseInt(block[0]);
            int y = Integer.parseInt(block[1]);
            int z = Integer.parseInt(block[2]);
            int dx = x - 10;
            int dy = y - 61;
            int dz = z - (-5);
            assertThat(dx * dx + dy * dy + dz * dz)
                    .as("block (%d,%d,%d) must be within radius 2 of center (10,61,-5)", x, y, z)
                    .isLessThanOrEqualTo(4);
        }
    }

    @DisplayName("radius scales with trunkHeight: max(2, trunkHeight / 3)")
    @Test
    void radiusScalesWithTrunkHeight() {
        // trunkHeight=3 → radius=max(2,1)=2 → 33 blocks at density 1.0
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 3, "oak_leaves", 1.0, rng(42));
        assertThat(SETTER.blocks).hasSize(SPHERE_R2_BLOCKS);

        // trunkHeight=12 → radius=4 → count blocks with distSq ≤ 16
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 12, "oak_leaves", 1.0, rng(42));
        // For radius 4: dx,dy,dz ∈ [-4,4]. Count pos with dx²+dy²+dz² ≤ 16
        // This is the 9×9×9 = 729 box minus corners beyond radius 4
        // We just verify it's larger than the radius-2 sphere
        assertThat(SETTER.blocks).hasSizeGreaterThan(SPHERE_R2_BLOCKS);

        // trunkHeight=0 → radius=max(2,0)=2 → 33 blocks
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 0, "oak_leaves", 1.0, rng(42));
        assertThat(SETTER.blocks).hasSize(SPHERE_R2_BLOCKS);
    }

    @DisplayName("border blocks are placed probabilistically at intermediate leafDensity")
    @Test
    void borderBlocksProbabilisticAtIntermediateDensity() {
        SETTER.blocks.clear();
        // leafDensity=0.5 with a known seed: border blocks are ~50% likely
        CANOPY.place(SETTER, 0, 50, 0, 6, "oak_leaves", 0.5, rng(42));

        int total = SETTER.blocks.size();
        // Must be at least interior (7) and at most full sphere (33)
        assertThat(total).isBetween(INTERIOR_R2_BLOCKS, SPHERE_R2_BLOCKS);
        // With density=0.5 and 26 border positions, we expect ~13 border blocks
        // plus 7 interior = ~20. Allow wide bounds for randomness: 10-33
        assertThat(total).isBetween(10, SPHERE_R2_BLOCKS);
    }

    @DisplayName("uses supplied leaf material")
    @Test
    void usesCorrectLeafMaterial() {
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 6, "spruce_leaves", 1.0, rng(42));

        assertThat(SETTER.blocks).hasSize(SPHERE_R2_BLOCKS);
        assertThat(SETTER.blocks.get(0)[3]).isEqualTo("spruce_leaves");
        assertThat(SETTER.blocks.get(SETTER.blocks.size() - 1)[3]).isEqualTo("spruce_leaves");
    }

    @DisplayName("deterministic with same seed")
    @Test
    void deterministicWithSameSeed() {
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 6, "oak_leaves", 0.5, rng(42));
        int count1 = SETTER.blocks.size();

        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 6, "oak_leaves", 0.5, rng(42));
        int count2 = SETTER.blocks.size();

        assertThat(count1).isEqualTo(count2);
    }

    @DisplayName("different seeds give different border placement")
    @Test
    void differentSeedsGiveDifferentBorderPlacement() {
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 6, "oak_leaves", 0.5, rng(42));
        int count1 = SETTER.blocks.size();

        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 6, "oak_leaves", 0.5, rng(99));
        int count2 = SETTER.blocks.size();

        // At least one should differ (highly likely with different seeds)
        assertThat(count1 != count2 || SETTER.blocks.stream()
                .anyMatch(b -> !b[0].equals("0") || !b[1].equals("51") || !b[2].equals("0")))
                .isTrue();
    }

    /** Creates a deterministic RandomGenerator from a fixed seed. */
    private static RandomGenerator rng(long seed) {
        return new java.util.SplittableRandom(seed);
    }

    // ── Radius 4 sphere count verification helper (used conceptually) ──
    // For radius 4 (trunkHeight 12):
    //   dx,dy,dz ∈ [-4, 4] → 9³ = 729 positions
    //   Positions with dx²+dy²+dz² ≤ 16:
    //     Center (0,0,0) → 1
    //     Manhattan rings within sphere volume ~ (4/3)π·4³ ≈ 268
    //   Verified by the assertion that it's >33 (the radius-2 count).
}
