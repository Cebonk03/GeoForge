package com.geoforge.engine.noise;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SimplexNoiseTest {

    private static final long SEED = 12345L;

    @Test
    void determinism_sameSeedSameCoordsProducesSameValue() {
        var noise = new SimplexNoise(SEED);
        double first = noise.sample(100.5, 200.3, 300.7);
        for (int i = 0; i < 100; i++) {
            double next = noise.sample(100.5, 200.3, 300.7);
            assertEquals(first, next, 1e-12, "Determinism violated at iteration " + i);
        }
    }

    @Test
    void determinism_2d_sameSeedSameCoordsProducesSameValue() {
        var noise = new SimplexNoise(SEED);
        double first = noise.sample(100.5, 200.3);
        for (int i = 0; i < 100; i++) {
            double next = noise.sample(100.5, 200.3);
            assertEquals(first, next, 1e-12, "2D determinism violated at iteration " + i);
        }
    }

    @Test
    void bounds_3d_allSamplesInRange() {
        var noise = new SimplexNoise(SEED);
        for (int i = 0; i < 1000; i++) {
            double x = (i * 17.3) % 1000;
            double y = (i * 31.7) % 1000;
            double z = (i * 7.1) % 1000;
            double v = noise.sample(x, y, z);
            assertTrue(
                    v >= -1.0 && v <= 1.0,
                    "3D sample out of [-1,1] range at i=" + i + ": " + v);
        }
    }

    @Test
    void bounds_2d_allSamplesInRange() {
        var noise = new SimplexNoise(SEED);
        for (int i = 0; i < 1000; i++) {
            double x = (i * 17.3) % 1000;
            double z = (i * 31.7) % 1000;
            double v = noise.sample(x, z);
            assertTrue(
                    v >= -1.0 && v <= 1.0,
                    "2D sample out of [-1,1] range at i=" + i + ": " + v);
        }
    }

    @Test
    void continuity_adjacentSamplesDifferByLessThanThreshold() {
        var noise = new SimplexNoise(SEED);
        double delta = 0.001;
        for (int i = 0; i < 500; i++) {
            double baseX = i * 10.0 + 0.3;
            double baseZ = i * 10.0 + 0.7;
            double v1 = noise.sample(baseX, 0, baseZ);
            double v2 = noise.sample(baseX + delta, 0, baseZ + delta);
            double diff = Math.abs(v2 - v1);
            assertTrue(
                    diff < 0.5,
                    "Continuity violation at i=" + i + ": diff=" + diff + " >= 0.5");
        }
    }

    @Test
    void differentSeedsProduceDifferentValues() {
        var noise1 = new SimplexNoise(SEED);
        var noise2 = new SimplexNoise(SEED + 1);
        boolean anyDifferent = false;
        for (int i = 0; i < 100; i++) {
            double v1 = noise1.sample(i * 10.3 + 0.5, i * 10.7 + 1.3, i * 10.1 + 2.7);
            double v2 = noise2.sample(i * 10.3 + 0.5, i * 10.7 + 1.3, i * 10.1 + 2.7);
            if (Math.abs(v1 - v2) > 1e-9) {
                anyDifferent = true;
                break;
            }
        }
        assertTrue(anyDifferent, "Different seeds produced identical noise values");
    }
}
