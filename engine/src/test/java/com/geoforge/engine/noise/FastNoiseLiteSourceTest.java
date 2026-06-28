package com.geoforge.engine.noise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("FastNoiseLite source tests")
class FastNoiseLiteSourceTest {

    private static final long SEED = 12345L;

    @DisplayName("Same seed and coordinates produce same 3D value")
    @Test
    void determinism_3d_sameSeedSameCoordsProducesSameValue() {
        var noise = new FastNoiseLiteSource(SEED);
        double first = noise.sample3D(100.5, 200.3, 300.7);
        for (int i = 0; i < 100; i++) {
            assertEquals(first, noise.sample3D(100.5, 200.3, 300.7), 1e-12);
        }
    }

    @DisplayName("Same seed and coordinates produce same 2D value")
    @Test
    void determinism_2d_sameSeedSameCoordsProducesSameValue() {
        var noise = new FastNoiseLiteSource(SEED);
        double first = noise.sample2D(100.5, 200.3);
        for (int i = 0; i < 100; i++) {
            assertEquals(first, noise.sample2D(100.5, 200.3), 1e-12);
        }
    }

    @DisplayName("All 3D samples are in [-1, 1] range")
    @Test
    void bounds_3d_allSamplesInRange() {
        var noise = new FastNoiseLiteSource(SEED);
        for (int i = 0; i < 1000; i++) {
            double x = (i * 17.3) % 1000;
            double y = (i * 31.7) % 1000;
            double z = (i * 7.1) % 1000;
            double v = noise.sample3D(x, y, z);
            assertThat(v).isBetween(-1.0, 1.0);
        }
    }

    @DisplayName("All 2D samples are in [-1, 1] range")
    @Test
    void bounds_2d_allSamplesInRange() {
        var noise = new FastNoiseLiteSource(SEED);
        for (int i = 0; i < 1000; i++) {
            double x = (i * 17.3) % 1000;
            double z = (i * 31.7) % 1000;
            double v = noise.sample2D(x, z);
            assertThat(v).isBetween(-1.0, 1.0);
        }
    }

    @DisplayName("Different seeds produce different values")
    @Test
    void differentSeedsProduceDifferentValues() {
        var noise1 = new FastNoiseLiteSource(SEED);
        var noise2 = new FastNoiseLiteSource(SEED + 1);
        boolean anyDifferent = false;
        for (int i = 0; i < 100; i++) {
            double v1 = noise1.sample3D(i * 10.3 + 0.5, i * 10.7 + 1.3, i * 10.1 + 2.7);
            double v2 = noise2.sample3D(i * 10.3 + 0.5, i * 10.7 + 1.3, i * 10.1 + 2.7);
            if (Math.abs(v1 - v2) > 1e-9) {
                anyDifferent = true;
                break;
            }
        }
        assertThat(anyDifferent).isTrue();
    }
}
