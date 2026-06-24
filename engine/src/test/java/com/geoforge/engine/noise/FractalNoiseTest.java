package com.geoforge.engine.noise;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FractalNoiseTest {

    @Test
    void singleOctave_matchesSimplexOutput() {
        var noise = new SimplexNoise(42L);
        var fractal = new FractalNoise(noise, 1, 2.0, 0.5);
        assertEquals(noise.sample(0, 0), fractal.sample2D(0, 0), 1e-9);
        assertEquals(noise.sample(1.5, 2.5), fractal.sample2D(1.5, 2.5), 1e-9);
        assertEquals(noise.sample(-3.0, 4.0), fractal.sample2D(-3.0, 4.0), 1e-9);
    }

    @Test
    void multiOctave_differsFromSingleOctave() {
        var single = new FractalNoise(new SimplexNoise(42L), 1, 2.0, 0.5);
        var multi = new FractalNoise(new SimplexNoise(42L), 4, 2.0, 0.5);

        var anyDifferent = false;
        for (int i = -10; i <= 10 && !anyDifferent; i++) {
            for (int j = -10; j <= 10 && !anyDifferent; j++) {
                double x = i + 0.3;
                double z = j + 0.7;
                if (Math.abs(single.sample2D(x, z) - multi.sample2D(x, z)) > 1e-12) {
                    anyDifferent = true;
                }
            }
        }
        assertTrue(anyDifferent,
                "multi-octave noise should differ from single-octave noise");
    }

    @Test
    void outputIsNormalized() {
        var fractal = new FractalNoise(new SimplexNoise(99L), 3, 2.0, 0.5);
        var rng = new java.util.SplittableRandom(42L);

        for (int i = 0; i < 1000; i++) {
            double x = rng.nextDouble(-100, 100);
            double z = rng.nextDouble(-100, 100);
            double value = fractal.sample2D(x, z);
            assertTrue(value >= -1.0 && value <= 1.0,
                    "Value " + value + " at (" + x + ", " + z + ") outside [-1, 1]");
        }
    }

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

    @Test
    void validation_octavesMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), 0, 2.0, 0.5));
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), -1, 2.0, 0.5));
    }

    @Test
    void validation_lacunarityMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), 1, 0.0, 0.5));
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), 1, -1.0, 0.5));
    }

    @Test
    void validation_persistenceMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), 1, 2.0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new FractalNoise(new SimplexNoise(0L), 1, 2.0, -0.5));
    }

    @Test
    void sample3D_usesYCoordinate() {
        var noise = new SimplexNoise(42L);
        var fractal = new FractalNoise(noise, 1, 2.0, 0.5);
        double v1 = fractal.sample3D(10.5, 20.3, 30.7);
        double v2 = fractal.sample3D(10.5, 99.9, 30.7);
        assertNotEquals(v1, v2, 1e-9, "Different Y should produce different noise");
    }

    @Test
    void sample3D_singleOctave_matchesSimplexOutput() {
        var noise = new SimplexNoise(42L);
        var fractal = new FractalNoise(noise, 1, 2.0, 0.5);
        assertEquals(noise.sample(1.5, 2.5, 3.5), fractal.sample3D(1.5, 2.5, 3.5), 1e-9);
        assertEquals(noise.sample(-3.0, 4.0, -5.0), fractal.sample3D(-3.0, 4.0, -5.0), 1e-9);
    }
}
