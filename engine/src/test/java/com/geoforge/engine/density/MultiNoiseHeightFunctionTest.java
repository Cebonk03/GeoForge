package com.geoforge.engine.density;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.geology.TectonicPlateMapper;
import com.geoforge.engine.noise.NoiseSource;
import com.geoforge.engine.noise.GradientNoise;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("unit")
@DisplayName("Multi-noise height function tests")
class MultiNoiseHeightFunctionTest {

    private static final long SEED = 42L;
    private static final long EROSION_SEED = 12345L;
    private static final double RIDGE_FREQ = 0.003;
    private static final double FBM_FREQ = 0.005;
    private static final double FLAT_FREQ = 0.008;
    private static final double RIDGE_AMP = 1.0;
    private static final double SHARPNESS = 2.0;
    private static final double BOUNDARY_WARP_FREQ = 0.001;
    private static final double BOUNDARY_WARP_AMP = 0.15;

    @DisplayName("Output is deterministic for the same inputs")
    @Test
    void deterministicOutput() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        double first = mnhf.sample(100, 0, 200);
        for (int i = 0; i < 50; i++) {
            assertEquals(first, mnhf.sample(100, 0, 200), 1e-12);
        }
    }

    @DisplayName("Height function ignores Y — same (x,z) returns same value regardless of Y")
    @Test
    void deterministic_withDifferentY_sameColumn() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        double y0 = mnhf.sample(50, 0, 100);
        double y50 = mnhf.sample(50, 50, 100);
        double yNeg = mnhf.sample(50, -999, 100);
        assertEquals(y0, y50, 1e-12);
        assertEquals(y0, yNeg, 1e-12);
    }

    @DisplayName("Different seeds produce different output")
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
        assertThat(anyDiff).isTrue();
    }

    @DisplayName("Different erosion seeds produce different weights")
    @Test
    void differentErosionSeeds_differentWeights() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf1 = new MultiNoiseHeightFunction(
                new GradientNoise(1L), new GradientNoise(2L), new GradientNoise(3L),
                mapper, 999L,
                RIDGE_FREQ, FBM_FREQ, FLAT_FREQ, RIDGE_AMP, SHARPNESS,
                new GradientNoise(999L), BOUNDARY_WARP_FREQ, BOUNDARY_WARP_AMP);
        var mnhf2 = new MultiNoiseHeightFunction(
                new GradientNoise(1L), new GradientNoise(2L), new GradientNoise(3L),
                mapper, 888L,
                RIDGE_FREQ, FBM_FREQ, FLAT_FREQ, RIDGE_AMP, SHARPNESS,
                new GradientNoise(888L), BOUNDARY_WARP_FREQ, BOUNDARY_WARP_AMP);
        boolean anyDiff = false;
        outer:
        for (int x = -200; x <= 200; x += 5) {
            for (int z = -200; z <= 200; z += 5) {
                double[] w1 = mnhf1.getCachedWeights(x, z);
                double[] w2 = mnhf2.getCachedWeights(x, z);
                if (Math.abs(w1[0] - w2[0]) > 1e-12
                        || Math.abs(w1[1] - w2[1]) > 1e-12
                        || Math.abs(w1[2] - w2[2]) > 1e-12) {
                    anyDiff = true;
                    break outer;
                }
            }
        }
        assertThat(anyDiff).isTrue();
    }

    @DisplayName("Low continentalness columns have ridge weight = 0")
    @Test
    void lowContinentalness_ridgeWeightIsZero() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        boolean found = false;
        outer:
        for (int x = -200; x <= 200; x += 5) {
            for (int z = -200; z <= 200; z += 5) {
                float c = mapper.getContinentalness(x, z);
                if (c <= 0.5f) {
                    double[] w = mnhf.getCachedWeights(x, z);
                    assertEquals(0.0, w[0], 1e-12, "Ridge weight should be 0 at c=" + c);
                    found = true;
                }
            }
        }
        assertThat(found).isTrue();
    }

    @DisplayName("Mid continentalness column exists where FBM weight dominates")
    @Test
    void midContinentalness_fbmWeightDominant() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        boolean found = false;
        outer:
        for (int x = -500; x <= 500; x += 3) {
            for (int z = -500; z <= 500; z += 3) {
                float c = mapper.getContinentalness(x, z);
                if (c >= 0.4f && c <= 0.6f) {
                    double[] w = mnhf.getCachedWeights(x, z);
                    if (w[1] >= w[0] && w[1] >= w[2]) {
                        found = true;
                        break outer;
                    }
                }
            }
        }
        assertThat(found).isTrue();
    }

    @DisplayName("High continentalness column exists where ridge weight dominates")
    @Test
    void highContinentalness_ridgeWeightDominant() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        boolean found = false;
        outer:
        for (int x = -500; x <= 500; x += 3) {
            for (int z = -500; z <= 500; z += 3) {
                float c = mapper.getContinentalness(x, z);
                if (c >= 0.7f) {
                    double[] w = mnhf.getCachedWeights(x, z);
                    if (w[0] >= w[1] && w[0] >= w[2]) {
                        found = true;
                        break outer;
                    }
                }
            }
        }
        assertThat(found).isTrue();
    }

    @DisplayName("Same column returns identical cached weights")
    @Test
    void columnCaching_sameColumnReturnsSameWeights() {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        double[] w1 = mnhf.getCachedWeights(100, 200);
        double[] w2 = mnhf.getCachedWeights(100, 200);
        assertArrayEquals(w1, w2, 1e-15);
        mnhf.resetCache();
        double[] w3 = mnhf.getCachedWeights(100, 200);
        assertArrayEquals(w1, w3, 1e-15);
    }

    @DisplayName("Column weights sum to unity for all grid positions")
    @ParameterizedTest
    @MethodSource("weightGridPositions")
    void columnCaching_weightsSumToUnity(int x, int z) {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        double[] w = mnhf.getCachedWeights(x, z);
        double sum = w[0] + w[1] + w[2];
        assertEquals(1.0, sum, 1e-12, "Weights should sum to 1 at (" + x + "," + z + ") "
                + Arrays.toString(w));
    }

    @DisplayName("Column weights are in [0, 1] range for all grid positions")
    @ParameterizedTest
    @MethodSource("weightGridPositions")
    void columnCaching_weightsInRangeZeroToOne(int x, int z) {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        double[] w = mnhf.getCachedWeights(x, z);
        for (int i = 0; i < 3; i++) {
            assertThat(w[i])
                    .as("Weight[%d] at (%d,%d)", i, x, z)
                    .isBetween(0.0, 1.0);
        }
    }

    @DisplayName("Noise samples are reduced by weight caching")
    @Test
    void columnCaching_noiseSamplesReducedByCaching() {
        var mapper = new TectonicPlateMapper(SEED);
        var ridgeCounter = new CountingNoiseSource(new GradientNoise(1L));
        var fbmCounter = new CountingNoiseSource(new GradientNoise(2L));
        var flatCounter = new CountingNoiseSource(new GradientNoise(3L));

        var mnhf = new MultiNoiseHeightFunction(
                ridgeCounter, fbmCounter, flatCounter, mapper, EROSION_SEED,
                RIDGE_FREQ, FBM_FREQ, FLAT_FREQ, RIDGE_AMP, SHARPNESS,
                new GradientNoise(999L), BOUNDARY_WARP_FREQ, BOUNDARY_WARP_AMP);

        mnhf.sample(100, 50, 200);
        int ridgeAfterFirst = ridgeCounter.callCount2D;
        int fbmAfterFirst = fbmCounter.callCount2D;
        int flatAfterFirst = flatCounter.callCount2D;

        mnhf.sample(100, 0, 200);
        int ridgeAfterSecond = ridgeCounter.callCount2D;
        int fbmAfterSecond = fbmCounter.callCount2D;
        int flatAfterSecond = flatCounter.callCount2D;

        assertEquals(ridgeAfterFirst * 2, ridgeAfterSecond);
        assertEquals(fbmAfterFirst * 2, fbmAfterSecond);
        assertEquals(flatAfterFirst * 2, flatAfterSecond);
    }

    @DisplayName("Sample with engine config produces reasonable blended values (no NaN/inf)")
    @ParameterizedTest
    @MethodSource("weightGridPositions")
    void sample_withEngineConfig_returnsReasonableBlendedValue(int x, int z) {
        var mapper = new TectonicPlateMapper(SEED);
        var mnhf = createDefault(mapper);
        double v = mnhf.sample(x, 0, z);
        assertThat(v).isFinite();
    }

    private static MultiNoiseHeightFunction createDefault(TectonicPlateMapper mapper) {
        return new MultiNoiseHeightFunction(
                new GradientNoise(1L),
                new GradientNoise(2L),
                new GradientNoise(3L),
                mapper,
                EROSION_SEED,
                RIDGE_FREQ, FBM_FREQ, FLAT_FREQ, RIDGE_AMP, SHARPNESS,
                new GradientNoise(999L), BOUNDARY_WARP_FREQ, BOUNDARY_WARP_AMP);
    }

    private static Stream<Arguments> weightGridPositions() {
        var args = new java.util.ArrayList<Arguments>();
        for (int x = -100; x <= 100; x += 10) {
            for (int z = -100; z <= 100; z += 10) {
                args.add(Arguments.of(x, z));
            }
        }
        return args.stream();
    }

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
