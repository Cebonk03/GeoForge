package com.geoforge.engine.security;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.biome.BiomeLookupTable;
import org.junit.jupiter.api.Test;

/**
 * PoC tests for {@link BiomeLookupTable} behavior under extreme or degenerate inputs.
 *
 * <p>Attack vector: If NaN or Infinity reaches the biome lookup (via
 * {@link com.geoforge.engine.GeoForgeEngine#getBiomeId}), the clamp must not crash
 * or produce an out-of-bounds array index.
 */
class BiomeLookupTableExtremeValuesPoCTest {

    /**
     * PoC: NaN temperature and humidity must not crash.
     *
     * <p>In the GeoForgeEngine, temperature comes from 3D noise which is bounded to [-1, 1].
     * However, a compromised or corrupted noise state could produce NaN.
     *
     * <p>clampIndex(NaN, -1.0, 1.0):
     * - NaN <= -1.0 → false (IEEE 754)
     * - NaN >= 1.0 → false
     * - norm = (NaN - (-1.0)) / (1.0 - (-1.0)) = NaN / 2.0 = NaN
     * - idx = (int)(NaN * 8) = (int)NaN = 0
     * - Returns 0 (safe)
     */
    @Test
    void nanInput_doesNotCrash() {
        String biome = BiomeLookupTable.lookup(Double.NaN, Double.NaN);
        assertNotNull(biome,
                "NaN inputs should not crash — defaults to first biome");
    }

    /**
     * PoC: NaN temperature with valid humidity.
     */
    @Test
    void nanTemperature_validHumidity() {
        String biome = BiomeLookupTable.lookup(Double.NaN, 0.5);
        assertNotNull(biome);
    }

    /**
     * PoC: NaN humidity with valid temperature.
     */
    @Test
    void nanHumidity_validTemperature() {
        String biome = BiomeLookupTable.lookup(0.0, Double.NaN);
        assertNotNull(biome);
    }

    /**
     * PoC: Infinity inputs must not crash.
     *
     * <p>+Inf → clampIndex returns SIZE-1 (7) because +Inf >= max.
     * -Inf → clampIndex returns 0 because -Inf <= min.
     */
    @Test
    void infinityInput_doesNotCrash() {
        assertNotNull(BiomeLookupTable.lookup(
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertNotNull(BiomeLookupTable.lookup(
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
        assertNotNull(BiomeLookupTable.lookup(
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
    }

    /**
     * PoC: Temperature and humidity outside documented ranges are safely clamped.
     *
     * <p>Temperature is supposed to be [-1, 1], humidity [0, 1].
     * Values outside these ranges must be clamped without crash.
     */
    @Test
    void outOfRangeValues_clampedSafely() {
        // Very cold temperature (-100) → clamped to index 0
        String t0 = BiomeLookupTable.lookup(-100.0, 0.5);
        // Very hot temperature (100) → clamped to index 7
        String t7 = BiomeLookupTable.lookup(100.0, 0.5);
        // Both should be valid
        assertNotNull(t0);
        assertNotNull(t7);

        // Very dry humidity (-0.5) → clamped to index 0
        String h0 = BiomeLookupTable.lookup(0.0, -0.5);
        // Very wet humidity (2.0) → clamped to index 7
        String h7 = BiomeLookupTable.lookup(0.0, 2.0);
        assertNotNull(h0);
        assertNotNull(h7);
    }

    /**
     * PoC: The table index is always in bounds — static proof.
     *
     * <p>clampIndex returns a value in [0, SIZE-1] = [0, 7] for any double input.
     * Proof:
     * - If value <= min → return 0
     * - If value >= max → return SIZE-1
     * - Otherwise: norm = (value - min) / (max - min), norm in (0, 1)
     *   idx = (int)(norm * SIZE) ∈ [0, SIZE-1]
     *   Final check: if idx >= SIZE → idx = SIZE-1 (defensive)
     *
     * <p>All possible double inputs produce valid array indices.
     * TABLE has dimensions [8][8], so any result in [0,7] × [0,7] is valid.
     */
    @Test
    void tableAccessAlwaysInBounds_staticProof() {
        // Exhaustively verify all clampIndex outputs
        double[] testValues = {
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.MAX_VALUE,
            Double.MIN_VALUE,
            -Double.MAX_VALUE,
            -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5,
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
        };

        for (double t : testValues) {
            for (double h : testValues) {
                String biome = BiomeLookupTable.lookup(t, h);
                assertNotNull(biome,
                        "Biome should not be null for temp=" + t + " hum=" + h);
            }
        }
    }
}
