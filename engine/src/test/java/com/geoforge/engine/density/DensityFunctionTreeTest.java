package com.geoforge.engine.density;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.noise.SimplexNoise;
import org.junit.jupiter.api.Test;

class DensityFunctionTreeTest {

    @Test
    void constant_alwaysReturnsValue() {
        var c = new ConstantDensity(0.3);
        assertEquals(0.3, c.sample(1, 2, 3), 1e-12);
        assertEquals(0.3, c.sample(-100, 200, 300), 1e-12);
    }

    @Test
    void add_returnsSum() {
        var a = new ConstantDensity(0.3);
        var b = new ConstantDensity(0.5);
        var add = new AddDensity(a, b);
        assertEquals(0.8, add.sample(0, 0, 0), 1e-9);
    }

    @Test
    void clamp_limitsOutput() {
        var inner = new ConstantDensity(2.0);
        var clamped = new ClampDensity(inner, -1.0, 1.0);
        assertEquals(1.0, clamped.sample(0, 0, 0), 1e-12);
    }

    @Test
    void clamp_lowerBound() {
        var inner = new ConstantDensity(-5.0);
        var clamped = new ClampDensity(inner, -1.0, 1.0);
        assertEquals(-1.0, clamped.sample(0, 0, 0), 1e-12);
    }

    @Test
    void clamp_passesThroughWithinRange() {
        var inner = new ConstantDensity(0.3);
        var clamped = new ClampDensity(inner, -1.0, 1.0);
        assertEquals(0.3, clamped.sample(0, 0, 0), 1e-12);
    }

    @Test
    void multiply_returnsProduct() {
        var a = new ConstantDensity(3.0);
        var b = new ConstantDensity(4.0);
        var mul = new MultiplyDensity(a, b);
        assertEquals(12.0, mul.sample(0, 0, 0), 1e-12);
    }

    @Test
    void scaledNoise_deterministic() {
        var noise = new SimplexNoise(42L);
        var scaled = new ScaledNoise(noise, 0.5, 0.5, 0.5);
        double first = scaled.sample(10, 20, 30);
        for (int i = 0; i < 50; i++) {
            assertEquals(first, scaled.sample(10, 20, 30), 1e-12);
        }
    }

    @Test
    void nestedComposition() {
        // (Add(Constant(2), Constant(3))) * Constant(4) = 20
        var innerAdd = new AddDensity(new ConstantDensity(2), new ConstantDensity(3));
        var mul = new MultiplyDensity(innerAdd, new ConstantDensity(4));
        assertEquals(20.0, mul.sample(0, 0, 0), 1e-12);
    }
}
