package com.geoforge.engine.density;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.geoforge.engine.noise.GradientNoise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Density function tree tests")
class DensityFunctionTreeTest {

    @DisplayName("Constant always returns its configured value")
    @Test
    void constant_alwaysReturnsValue() {
        var c = new ConstantDensity(0.3);
        assertEquals(0.3, c.sample(1, 2, 3), 1e-12);
        assertEquals(0.3, c.sample(-100, 200, 300), 1e-12);
    }

    @DisplayName("Add returns the sum of two constants")
    @Test
    void add_returnsSum() {
        var a = new ConstantDensity(0.3);
        var b = new ConstantDensity(0.5);
        var add = new AddDensity(a, b);
        assertEquals(0.8, add.sample(0, 0, 0), 1e-9);
    }

    @DisplayName("Clamp limits output to configured range (upper)")
    @Test
    void clamp_limitsOutput() {
        var inner = new ConstantDensity(2.0);
        var clamped = new ClampDensity(inner, -1.0, 1.0);
        assertEquals(1.0, clamped.sample(0, 0, 0), 1e-12);
    }

    @DisplayName("Clamp limits output to configured range (lower)")
    @Test
    void clamp_lowerBound() {
        var inner = new ConstantDensity(-5.0);
        var clamped = new ClampDensity(inner, -1.0, 1.0);
        assertEquals(-1.0, clamped.sample(0, 0, 0), 1e-12);
    }

    @DisplayName("Clamp passes through values within range")
    @Test
    void clamp_passesThroughWithinRange() {
        var inner = new ConstantDensity(0.3);
        var clamped = new ClampDensity(inner, -1.0, 1.0);
        assertEquals(0.3, clamped.sample(0, 0, 0), 1e-12);
    }

    @DisplayName("Multiply returns the product of two constants")
    @Test
    void multiply_returnsProduct() {
        var a = new ConstantDensity(3.0);
        var b = new ConstantDensity(4.0);
        var mul = new MultiplyDensity(a, b);
        assertEquals(12.0, mul.sample(0, 0, 0), 1e-12);
    }

    @DisplayName("Scaled noise is deterministic")
    @Test
    void scaledNoise_deterministic() {
        var noise = new GradientNoise(42L);
        var scaled = new ScaledNoise(noise, 0.5, 0.5, 0.5);
        double first = scaled.sample(10, 20, 30);
        for (int i = 0; i < 50; i++) {
            assertEquals(first, scaled.sample(10, 20, 30), 1e-12);
        }
    }

    @DisplayName("Nested composition (Add * Constant) produces correct result")
    @Test
    void nestedComposition() {
        var innerAdd = new AddDensity(new ConstantDensity(2), new ConstantDensity(3));
        var mul = new MultiplyDensity(innerAdd, new ConstantDensity(4));
        assertEquals(20.0, mul.sample(0, 0, 0), 1e-12);
    }

    @DisplayName("ScaledNoise2D ignores Y coordinate")
    @Test
    void scaledNoise2D_ignoresY() {
        var noise = new GradientNoise(42L);
        var scaled = new ScaledNoise2D(noise, 0.1, 0.1);
        double v1 = scaled.sample(10, 50, 10);
        double v2 = scaled.sample(10, 999, 10);
        assertEquals(v1, v2, 1e-12);
    }

    @DisplayName("ScaledNoise2D is deterministic")
    @Test
    void scaledNoise2D_deterministic() {
        var noise = new GradientNoise(42L);
        var scaled = new ScaledNoise2D(noise, 0.5, 0.5);
        double first = scaled.sample(10, 20, 30);
        for (int i = 0; i < 50; i++) {
            assertEquals(first, scaled.sample(10, 20, 30), 1e-12);
        }
    }

    @DisplayName("ScaledNoise2D with identity scaling matches simplex")
    @Test
    void scaledNoise2D_zeroY_matchesSimplex() {
        var noise = new GradientNoise(42L);
        var scaled = new ScaledNoise2D(noise, 1.0, 1.0);
        double v1 = scaled.sample(10, 0, 20);
        double v2 = noise.sample(10, 20);
        assertEquals(v1, v2, 1e-12);
    }

    @DisplayName("ScaledNoise2D respects scale factors")
    @Test
    void scaledNoise2D_respectsScales() {
        var noise = new GradientNoise(42L);
        var scaled = new ScaledNoise2D(noise, 0.5, 0.5);
        double v1 = scaled.sample(10, 0, 20);
        double v2 = noise.sample(5.0, 10.0);
        assertEquals(v1, v2, 1e-12);
    }
}
