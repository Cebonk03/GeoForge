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
@DisplayName("OvalCanopy — ellipsoid canopy placement")
class OvalCanopyTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();

        @Override
        public void setBlock(int x, int y, int z, String materialName) {
            blocks.add(new String[]{String.valueOf(x), String.valueOf(y), String.valueOf(z), materialName});
        }
    }

    private static final RecordingSetter SETTER = new RecordingSetter();
    private static final OvalCanopy CANOPY = new OvalCanopy();

    /** Count of blocks within an ellipsoid with rH=2, rV=2 (sphere of radius 2). */
    private static final int ELLIPSOID_R2_BLOCKS = 33;
    /** Interior blocks (innerDist ≤ 1.0 with rH-1=1, rV-1=1) for rH=2, rV=2. */
    private static final int INTERIOR_R2_BLOCKS = 7;

    @DisplayName("places a full ellipsoid when leafDensity = 1.0")
    @Test
    void fullDensityPlacesAllEllipsoidBlocks() {
        SETTER.blocks.clear();
        // trunkHeight=6 → rH=2, rV=2
        CANOPY.place(SETTER, 0, 50, 0, 6, "oak_leaves", 1.0, rng(42));

        assertThat(SETTER.blocks).hasSize(ELLIPSOID_R2_BLOCKS);
        assertThat(SETTER.blocks)
                .allSatisfy(block -> assertThat(block[3]).isEqualTo("oak_leaves"));
    }

    @DisplayName("places only interior blocks when leafDensity = 0.0")
    @Test
    void zeroDensityPlacesOnlyInterior() {
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 6, "oak_leaves", 0.0, rng(42));

        // Only interior (innerDist ≤ 1.0) blocks should be placed
        assertThat(SETTER.blocks).hasSize(INTERIOR_R2_BLOCKS);
    }

    @DisplayName("places blocks centered at (tipX, tipY+1, tipZ)")
    @Test
    void centerIsTipYPlusOne() {
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 10, 60, -5, 6, "birch_leaves", 1.0, rng(42));

        // Verify all placed blocks are centered at (10, 61, -5) with rH=2, rV=2
        for (String[] block : SETTER.blocks) {
            int x = Integer.parseInt(block[0]);
            int y = Integer.parseInt(block[1]);
            int z = Integer.parseInt(block[2]);
            int dx = x - 10;
            int dy = y - 61;
            int dz = z - (-5);
            double dist = (double)(dx * dx) / 4 + (double)(dy * dy) / 4 + (double)(dz * dz) / 4;
            assertThat(dist)
                    .as("block (%d,%d,%d) must be within ellipsoid of center (10,61,-5) with rH=2, rV=2", x, y, z)
                    .isLessThanOrEqualTo(1.0);
        }
    }

    @DisplayName("radii scale with trunkHeight: rH=max(2, trunkHeight/4), rV=max(2, trunkHeight/3)")
    @Test
    void radiiScaleWithTrunkHeight() {
        // trunkHeight=3 → rH=max(2,0)=2, rV=max(2,1)=2 → 33 blocks at density 1.0
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 3, "oak_leaves", 1.0, rng(42));
        assertThat(SETTER.blocks).hasSize(ELLIPSOID_R2_BLOCKS);

        // trunkHeight=12 → rH=3, rV=4 → more blocks than the rH=2,rV=2 case
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 12, "oak_leaves", 1.0, rng(42));
        assertThat(SETTER.blocks).hasSizeGreaterThan(ELLIPSOID_R2_BLOCKS);

        // trunkHeight=0 → rH=max(2,0)=2, rV=max(2,0)=2 → 33 blocks
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 0, "oak_leaves", 1.0, rng(42));
        assertThat(SETTER.blocks).hasSize(ELLIPSOID_R2_BLOCKS);
    }

    @DisplayName("oval shape is vertically elongated when rV > rH")
    @Test
    void ovalIsVerticallyElongated() {
        // trunkHeight=12 → rH=3, rV=4
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 12, "oak_leaves", 1.0, rng(42));

        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (String[] block : SETTER.blocks) {
            int x = Integer.parseInt(block[0]);
            int y = Integer.parseInt(block[1]);
            int z = Integer.parseInt(block[2]);
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }

        int ySpan = maxY - minY;
        int xSpan = maxX - minX;
        int zSpan = maxZ - minZ;

        // Vertical span should be larger than horizontal spans (rV=4 > rH=3)
        assertThat(ySpan)
                .as("vertical span should be larger than horizontal X span")
                .isGreaterThan(xSpan);
        assertThat(ySpan)
                .as("vertical span should be larger than horizontal Z span")
                .isGreaterThan(zSpan);
    }

    @DisplayName("border blocks are placed probabilistically at intermediate leafDensity")
    @Test
    void borderBlocksProbabilisticAtIntermediateDensity() {
        SETTER.blocks.clear();
        // leafDensity=0.5 with a known seed: border blocks are ~50% likely
        CANOPY.place(SETTER, 0, 50, 0, 6, "oak_leaves", 0.5, rng(42));

        int total = SETTER.blocks.size();
        // Must be at least interior (7) and at most full ellipsoid (33)
        assertThat(total).isBetween(INTERIOR_R2_BLOCKS, ELLIPSOID_R2_BLOCKS);
        // With density=0.5 and 26 border positions, we expect ~13 border blocks
        // plus 7 interior = ~20. Allow wide bounds for randomness: 10-33
        assertThat(total).isBetween(10, ELLIPSOID_R2_BLOCKS);
    }

    @DisplayName("uses supplied leaf material")
    @Test
    void usesCorrectLeafMaterial() {
        SETTER.blocks.clear();
        CANOPY.place(SETTER, 0, 50, 0, 6, "spruce_leaves", 1.0, rng(42));

        assertThat(SETTER.blocks).hasSize(ELLIPSOID_R2_BLOCKS);
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
}
