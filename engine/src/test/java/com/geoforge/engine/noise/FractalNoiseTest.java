package com.geoforge.engine.noise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Fractal noise tests")
class FractalNoiseTest {

    @DisplayName("Single octave matches simplex output")
    @Test
    void singleOctave_matchesSimplexOutput() {
        var noise = new SimplexNoise(42L);
        var fractal = new FractalNoise(noise, 1, 2.0, 0.5);
        assertEquals(noise.sample(0, 0), fractal.sample2D(0, 0), 1e-9);
        assertEquals(noise.sample(1.5, 2.5), fractal.sample2D(1.5, 2.5), 1e-9);
        assertEquals(noise.sample(-3.0, 4.0), fractal.sample2D(-3.0, 4.0), 1e-9);
    }

    @DisplayName("Multi-octave output differs from single-octave")
    @Test
    void multiOctave_differsFromSingleOctave() {
        var single = new FractalNoise(new SimplexNoise(42L), 1, 2.0, 0.5);
        var multi = new FractalNoise(new SimplexNoise(42L), 4, 2.0, 0.5);

        boolean anyDifferent = false;
        outer:
        for (int i = -10; i <= 10; i++) {
            for (int j = -10; j <= 10; j++) {
                double x = i + 0.3;
                double z = j + 0.7;
                if (Math.abs(single.sample2D(x, z) - multi.sample2D(x, z)) > 1e-12) {
                    anyDifferent = true;
                    break outer;
                }
            }
        }
        assertThat(anyDifferent).isTrue();
    }

    @DisplayName("All 2D samples are within [-1, 1] range")
    @Test
    void outputIsNormalized() {
        var fractal = new FractalNoise(new SimplexNoise(99L), 3, 2.0, 0.5);
        var rng = new java.util.SplittableRandom(42L);

        for (int i = 0; i < 1000; i++) {
            double x = rng.nextDouble(-100, 100);
            double z = rng.nextDouble(-100, 100);
            double value = fractal.sample2D(x, z);
            assertThat(value).isBetween(-1.0, 1.0);
        }
    }

    @DisplayName("Same seed produces same output")
    @Test
    void determinism_sameSeedProducesSameOutput() {
        var f1 = new FractalNoise(new SimplexNoise(17L), 4, 2.0, 0.5);
        var f2 = new FractalNoise(new SimplexNoise(17L), 4, 2.0, 0.5);

        for (int i = 0; i < 50; i++) {
            double x = i * 1.3;
            double z = i * 0.7;
            assertEquals(f1.sample2D(x, z), f2.sample2D(x, z), 1e-12);
        }
    }

    @DisplayName("Constructor rejects zero or negative octaves")
    @Test
    void validation_octavesMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), 0, 2.0, 0.5));
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), -1, 2.0, 0.5));
    }

    @DisplayName("Constructor rejects non-positive lacunarity")
    @Test
    void validation_lacunarityMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), 1, 0.0, 0.5));
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), 1, -1.0, 0.5));
    }

    @DisplayName("Constructor rejects non-positive persistence")
    @Test
    void validation_persistenceMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), 1, 2.0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), 1, 2.0, -0.5));
    }

    @DisplayName("Different Y coordinates produce different 3D noise")
    @Test
    void sample3D_usesYCoordinate() {
        var noise = new SimplexNoise(42L);
        var fractal = new FractalNoise(noise, 1, 2.0, 0.5);
        double v1 = fractal.sample3D(10.5, 20.3, 30.7);
        double v2 = fractal.sample3D(10.5, 99.9, 30.7);
        assertNotEquals(v1, v2, 1e-9);
    }

    @DisplayName("Single-octave 3D matches simplex output")
    @Test
    void sample3D_singleOctave_matchesSimplexOutput() {
        var noise = new SimplexNoise(42L);
        var fractal = new FractalNoise(noise, 1, 2.0, 0.5);
        assertEquals(noise.sample(1.5, 2.5, 3.5), fractal.sample3D(1.5, 2.5, 3.5), 1e-9);
        assertEquals(noise.sample(-3.0, 4.0, -5.0), fractal.sample3D(-3.0, 4.0, -5.0), 1e-9);
    }
}
