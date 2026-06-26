package com.geoforge.engine.density;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.geology.TectonicPlateMapper;
import com.geoforge.engine.noise.NoiseSource;
import com.geoforge.engine.noise.SimplexNoise;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class MultiNoiseHeightFunctionTest {

    private static final long SEED = 42L;
    private static final long EROSION_SEED = 12345L;
    private static final double RIDGE_FREQ = 0.003;
    private static final double FBM_FREQ = 0.005;
    private static final double FLAT_FREQ = 0.008;
    private static final double RIDGE_AMP = 1.0;
    private static final double SHARPNESS = 2.0;

    // -----------------------------------------------------------------------
    // Determinism
    // -----------------------------------------------------------------------

    @Test
    void deterministicOutput() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        double first = mnhf.sample(100, 0, 200);
        for (int i = 0; i < 50; i++) {
            assertEquals(first, mnhf.sample(100, 0, 200), 1e-12);
        }
    }

    @Test
    void deterministic_withDifferentY_sameColumn() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        // Height function should ignore Y — same (x,z) always returns same value
        double y0 = mnhf.sample(50, 0, 100);
        double y50 = mnhf.sample(50, 50, 100);
        double yNeg = mnhf.sample(50, -999, 100);
        assertEquals(y0, y50, 1e-12, "Y=50 should produce same as Y=0");
        assertEquals(y0, yNeg, 1e-12, "Y=-999 should produce same as Y=0");
    }

    // -----------------------------------------------------------------------
    // Different seeds
    // -----------------------------------------------------------------------

    @Test
    void differentSeeds_differentOutput() {
        var mapper1 = new TectonicPlateMapper(SEED);
        var mapper2 = new TectonicPlateMapper(SEED + 9999);
        var mnhf1 = createDefault(mapper1);
        var mnhf2 = createDefault(mapper2);
        boolean anyDiff = false;
        for (int x = 0; x < 200; x += 10) {
            if (Math.abs(mnhf1.sample(x, 0, x) - mnhf2.sample(x, 0, x)) > 1e-9) {
                anyDiff = true;
                break;
            }
        }
        assertTrue(anyDiff, "Different seeds should produce different output");
    }

    @Test
    void differentErosionSeeds_differentWeights() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf1 = new MultiNoiseHeightFunction(
                new SimplexNoise(1L), new SimplexNoise(2L), new SimplexNoise(3L),
                mapper, 999L,
                RIDGE_FREQ, FBM_FREQ, FLAT_FREQ, RIDGE_AMP, SHARPNESS);
        var mnhf2 = new MultiNoiseHeightFunction(
                new SimplexNoise(1L), new SimplexNoise(2L), new SimplexNoise(3L),
                mapper, 888L,
                RIDGE_FREQ, FBM_FREQ, FLAT_FREQ, RIDGE_AMP, SHARPNESS);
        // Different erosion seeds should produce different weights
        boolean anyDiff = false;
        for (int x = -200; x <= 200; x += 5) {
            for (int z = -200; z <= 200; z += 5) {
                double[] w1 = mnhf1.getCachedWeights(x, z);
                double[] w2 = mnhf2.getCachedWeights(x, z);
                if (Math.abs(w1[0] - w2[0]) > 1e-12
                        || Math.abs(w1[1] - w2[1]) > 1e-12
                        || Math.abs(w1[2] - w2[2]) > 1e-12) {
                    anyDiff = true;
                    break;
                }
            }
            if (anyDiff) break;
        }
        assertTrue(anyDiff, "Different erosion seeds should produce different weights");
    }

    // -----------------------------------------------------------------------
    // Continentalness-driven blend weights
    // -----------------------------------------------------------------------

    @Test
    void lowContinentalness_ridgeWeightIsZero() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        // For all coordinates with c ≤ 0.5, ridge weight should be 0
        // (ridge only appears in highlands zone c > 0.7)
        for (int x = -200; x <= 200; x += 5) {
            for (int z = -200; z <= 200; z += 5) {
                float c = mapper.getContinentalness(x, z);
                if (c <= 0.5f) {
                    double[] w = mnhf.getCachedWeights(x, z);
                    assertEquals(0.0, w[0], 1e-12,
                            "Ridge weight should be 0 at c=" + c);
                }
            }
        }
    }

    @Test
    void midContinentalness_fbmWeightDominant() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        boolean found = false;
        for (int x = -500; x <= 500; x += 3) {
            for (int z = -500; z <= 500; z += 3) {
                float c = mapper.getContinentalness(x, z);
                if (c >= 0.4f && c <= 0.6f) {
                    double[] w = mnhf.getCachedWeights(x, z);
                    if (w[1] >= w[0] && w[1] >= w[2]) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }
        assertTrue(found,
                "Should find a mid-continentalness column (c ~0.5) where FBM weight dominates");
    }

    @Test
    void highContinentalness_ridgeWeightDominant() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        boolean found = false;
        for (int x = -500; x <= 500; x += 3) {
            for (int z = -500; z <= 500; z += 3) {
                float c = mapper.getContinentalness(x, z);
                if (c >= 0.7f) {
                    double[] w = mnhf.getCachedWeights(x, z);
                    if (w[0] >= w[1] && w[0] >= w[2]) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }
        assertTrue(found,
                "Should find a high-continentalness column (c ≥ 0.7) where ridge weight dominates");
    }

    // -----------------------------------------------------------------------
    // Column weight caching
    // -----------------------------------------------------------------------

    @Test
    void columnCaching_sameColumnReturnsSameWeights() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        // Same (x,z) returns exactly the same weights
        double[] w1 = mnhf.getCachedWeights(100, 200);
        double[] w2 = mnhf.getCachedWeights(100, 200);
        assertArrayEquals(w1, w2, 1e-15, "Weights should be identical for same column");

        // Reset cache and verify weights are still consistent
        mnhf.resetCache();
        double[] w3 = mnhf.getCachedWeights(100, 200);
        assertArrayEquals(w1, w3, 1e-15, "Weights should be reproducible after cache reset");
    }

    @Test
    void columnCaching_weightsSumToUnity() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        for (int x = -100; x <= 100; x += 10) {
            for (int z = -100; z <= 100; z += 10) {
                double[] w = mnhf.getCachedWeights(x, z);
                double sum = w[0] + w[1] + w[2];
                assertEquals(1.0, sum, 1e-12,
                        "Weights should sum to 1 at (" + x + "," + z + ") "
                                + Arrays.toString(w));
            }
        }
    }

    @Test
    void columnCaching_weightsInRangeZeroToOne() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        for (int x = -100; x <= 100; x += 10) {
            for (int z = -100; z <= 100; z += 10) {
                double[] w = mnhf.getCachedWeights(x, z);
                for (int i = 0; i < 3; i++) {
                    assertTrue(w[i] >= 0.0 && w[i] <= 1.0,
                            "Weight[" + i + "]=" + w[i] + " out of [0,1] at (" + x + "," + z + ")");
                }
            }
        }
    }

    @Test
    void columnCaching_noiseSamplesReducedByCaching() {
        var mapper = new TectonicPlateMapper(SEED);
        var ridgeCounter = new CountingNoiseSource(new SimplexNoise(1L));
        var fbmCounter = new CountingNoiseSource(new SimplexNoise(2L));
        var flatCounter = new CountingNoiseSource(new SimplexNoise(3L));

        var mnhf = new MultiNoiseHeightFunction(
                ridgeCounter, fbmCounter, flatCounter, mapper, EROSION_SEED,
                RIDGE_FREQ, FBM_FREQ, FLAT_FREQ, RIDGE_AMP, SHARPNESS);

        // First sample at (100, 50, 200) — computes weights + samples noise
        mnhf.sample(100, 50, 200);
        int ridgeAfterFirst = ridgeCounter.callCount2D;
        int fbmAfterFirst = fbmCounter.callCount2D;
        int flatAfterFirst = flatCounter.callCount2D;

        // Second sample at same (x,z), different y — should use cached weights,
        // but still sample each noise source (cache is for weights, not noise)
        mnhf.sample(100, 0, 200);
        int ridgeAfterSecond = ridgeCounter.callCount2D;
        int fbmAfterSecond = fbmCounter.callCount2D;
        int flatAfterSecond = flatCounter.callCount2D;

        // Each sample() call should sample each noise source exactly once
        assertEquals(ridgeAfterFirst * 2, ridgeAfterSecond,
                "Each sample() call should sample ridge noise once");
        assertEquals(fbmAfterFirst * 2, fbmAfterSecond,
                "Each sample() call should sample FBM noise once");
        assertEquals(flatAfterFirst * 2, flatAfterSecond,
                "Each sample() call should sample flat noise once");
    }

    // -----------------------------------------------------------------------
    // Integration sanity
    // -----------------------------------------------------------------------

    @Test
    void sample_withEngineConfig_returnsReasonableBlendedValue() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        // The output is a noise blend; it should not be NaN or infinite
        for (int x = -100; x <= 100; x += 10) {
            for (int z = -100; z <= 100; z += 10) {
                double v = mnhf.sample(x, 0, z);
                assertFalse(Double.isNaN(v), "NaN at (" + x + ",0," + z + ")");
                assertFalse(Double.isInfinite(v), "Infinite at (" + x + ",0," + z + ")");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static MultiNoiseHeightFunction createDefault(TectonicPlateMapper mapper) {
        return new MultiNoiseHeightFunction(
                new SimplexNoise(1L),
                new SimplexNoise(2L),
                new SimplexNoise(3L),
                mapper,
                EROSION_SEED,
                RIDGE_FREQ, FBM_FREQ, FLAT_FREQ, RIDGE_AMP, SHARPNESS);
    }

    /**
     * A NoiseSource that counts how many times {@link #sample2D} is called.
     * Useful for verifying caching behavior.
     */
    static final class CountingNoiseSource implements NoiseSource {
        final NoiseSource delegate;
        int callCount2D;

        CountingNoiseSource(NoiseSource delegate) {
            this.delegate = delegate;
        }

        @Override
        public double sample2D(double x, double z) {
            callCount2D++;
            return delegate.sample2D(x, z);
        }

        @Override
        public double sample3D(double x, double y, double z) {
            return delegate.sample3D(x, y, z);
        }
    }
}
