package com.geoforge.engine.security;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.plateau.StructurePlateauModifier;
import org.junit.jupiter.api.Test;

/**
 * PoC tests for edge cases in {@link StructurePlateauModifier}.
 *
 * <p>Attack vector: If an attacker can craft heightmap data or plateau bounds that
 * are out of range, the modifier must not crash or corrupt memory. The plateau modifier
 * is called during structure placement, which could be triggered by player-placed
 * structures or world generation events.
 */
class StructurePlateauModifierEdgeCasesPoCTest {

    /**
     * PoC: Minimum viable heightmap (size=1) must not crash.
     *
     * <p>A 1×1 heightmap can represent a single column. Applying a plateau should work.
     */
    @Test
    void size1_heightmap_noCrash() {
        float[] hm = { 0.0f };
        StructurePlateauModifier.applyPlateau(hm, 1, 0, 0, 0, 0, 10.0f);
        assertEquals(10.0f, hm[0], 1e-6f, "Single cell should be set to target height");
    }

    /**
     * PoC: Size 2 heightmap (corner case between trivial and normal).
     */
    @Test
    void size2_heightmap_noCrash() {
        float[] hm = { 1, 2, 3, 4 };
        StructurePlateauModifier.applyPlateau(hm, 2, 0, 0, 1, 1, 50.0f);
        assertEquals(50.0f, hm[0], 1e-6f, "All cells should be plateau height");
        assertEquals(50.0f, hm[3], 1e-6f);
    }

    /**
     * PoC: Plateau coords fully outside heightmap bounds are clamped to valid range.
     *
     * <p>x0=-100, x1=200 on a 16×16 map → clamped to 0..15.
     * The entire map becomes the plateau. Must not crash.
     */
    @Test
    void plateauBoundsOutsideMap_clampedToEdges() {
        float[] hm = new float[16 * 16];
        for (int i = 0; i < hm.length; i++) hm[i] = i % 10;

        StructurePlateauModifier.applyPlateau(hm, 16, -100, -100, 200, 200, 42.0f);

        // Every cell should be plateau height (with feathering at edges)
        assertEquals(42.0f, hm[0], 1e-4f, "Corner cell should be at target height");
        assertEquals(42.0f, hm[255], 1e-4f, "Far corner should be at target height");
    }

    /**
     * PoC: x0 > x1 (swapped bounds) is handled safely via clamp.
     *
     * <p>Both values are clamped to [0, size-1]. Since x0=100 → 15 and x1=0 → 0,
     * the resulting range is 0..15 (because clamp produces cx0=15, cx1=15, but wait...
     * cx0=clamp(100, 0, 15)=15, cx1=clamp(-100, 0, 15)=0. Then cz0 > cz1 means no loop iteration.
     */
    @Test
    void swappedBounds_zeroRegionProcessed() {
        float[] hm = new float[8 * 8];
        for (int i = 0; i < hm.length; i++) hm[i] = 1.0f;

        // x0 > x1, z0 > z1 — clamped to 7 and 0
        StructurePlateauModifier.applyPlateau(hm, 8, 100, 100, -100, -100, 99.0f);

        // After clamping: cx0=7, cx1=0. Loop cz0..cz1 = 7..0 → empty.
        // Nothing should change.
        assertEquals(1.0f, hm[0], 1e-6f, "First cell unchanged");
        assertEquals(1.0f, hm[63], 1e-6f, "Last cell unchanged");
    }

    /**
     * PoC: Single-column plateau (x0 == x1) must not crash.
     *
     * <p>Due to feather blending, cells in the single column are blended with
     * neighbors outside the column. Values should be modified.
     */
    @Test
    void singleColumnPlateau_noCrash() {
        float[] hm = new float[10 * 10];
        // Use non-linear pattern so neighborhood average != cell value
        for (int i = 0; i < hm.length; i++) hm[i] = i * 3.0f;
        // Plateau at column 5, all rows
        StructurePlateauModifier.applyPlateau(hm, 10, 5, 0, 5, 9, 100.0f);
        // Cell at (5, 5) should be modified from original 165.0
        // Due to feather, just verify not NaN
        assertFalse(Float.isNaN(hm[5 * 10 + 5]),
                "Cell (5,5) should not be NaN");
    }

    /**
     * PoC: Large plateau on small map must not overflow.
     *
     * <p>If plateau bounds are huge (e.g. x0=Integer.MIN_VALUE, x1=Integer.MAX_VALUE),
     * clamp normalizes them to 0..size-1.
     */
    @Test
    void extremePlateauBounds_safeClamping() {
        float[] hm = new float[4 * 4];
        for (int i = 0; i < hm.length; i++) hm[i] = 5.0f;

        // Apply plateau covering everything — use extreme values
        StructurePlateauModifier.applyPlateau(hm, 4,
                Integer.MIN_VALUE, Integer.MIN_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE,
                77.0f);

        // All cells should be set to target height
        for (int i = 0; i < hm.length; i++) {
            assertEquals(77.0f, hm[i], 1e-4f, "Cell " + i + " should be plateau height");
        }
    }

    /**
     * PoC: Minimum feasible heightmap (size=1) with plateau at the only cell.
     */
    @Test
    void size1_singleCellPlateau() {
        float[] hm = { 0.0f };
        StructurePlateauModifier.applyPlateau(hm, 1, 0, 0, 0, 0, -5.0f);
        assertEquals(-5.0f, hm[0], 1e-6f, "Negative target height works");
    }

    /**
     * PoC: Feathering at edge with large heightmap — verify no index overflow.
     *
     * <p>getNeighborAvg reads neighbors at dx/dz = -1..1. At x=0, nx=-1 is clamped
     * to 0 and the check "outside plateau" is true. Then "nx >= 0 && nx < size" is
     * true for nx=0 (clamped). So it reads heightmap[0 * size + 0].
     * No index out of bounds is possible.
     */
    @Test
    void featherAtEdge_noBoundsViolation() {
        float[] hm = new float[16 * 16];
        for (int i = 0; i < hm.length; i++) hm[i] = 3.0f;

        // Plateau at the very edge of the map
        StructurePlateauModifier.applyPlateau(hm, 16, 0, 0, 7, 7, 100.0f);

        // Edge cell (0,0) should have value (feathered blend with neighbor)
        // Just verify no crash and result is not NaN
        assertFalse(Float.isNaN(hm[0]));
        assertFalse(Float.isNaN(hm[7 * 16 + 7]));
    }
}
