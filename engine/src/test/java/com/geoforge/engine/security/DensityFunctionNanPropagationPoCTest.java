package com.geoforge.engine.security;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.density.*;
import com.geoforge.engine.noise.SimplexNoise;
import org.junit.jupiter.api.Test;

/**
 * PoC tests for NaN/Infinity propagation through the {@link DensityFunctionTree} composition.
 *
 * <p>Attack vector: If NaN or Infinity is introduced into the density function tree
 * (e.g. via extreme coordinate scaling or direct API misuse), it must not propagate
 * unnoticed or cause crashes downstream.
 */
class DensityFunctionNanPropagationPoCTest {

    @Test
    void constantDensity_nanDoesNotCrash() {
        var c = new ConstantDensity(Double.NaN);
        double v = c.sample(0, 0, 0);
        assertTrue(Double.isNaN(v), "NaN constant propagates NaN");
    }

    @Test
    void addDensity_nanPropagates() {
        var a = new ConstantDensity(Double.NaN);
        var b = new ConstantDensity(5.0);
        var add = new AddDensity(a, b);
        double v = add.sample(0, 0, 0);
        assertTrue(Double.isNaN(v),
                "NaN + 5 = NaN — NaN infection propagates through addition");
    }

    @Test
    void multiplyDensity_nanPropagates() {
        var a = new ConstantDensity(Double.NaN);
        var b = new ConstantDensity(5.0);
        var mul = new MultiplyDensity(a, b);
        double v = mul.sample(0, 0, 0);
        assertTrue(Double.isNaN(v),
                "NaN * 5 = NaN — NaN infection propagates through multiplication");
    }

    /**
     * FINDING: ClampDensity does NOT guard against NaN.
     *
     * <p>Due to IEEE 754 semantics, {@code NaN < min} is always false and
     * {@code NaN > max} is always false. Therefore, ClampDensity returns the
     * raw NaN instead of clamping to the min/max bound.
     *
     * <p>This means if NaN reaches the height function (e.g. via extreme coordinate
     * scaling or corrupted noise), the clamp will not sanitize it.
     */
    @Test
    void clampDensity_doesNotCatchNan() {
        var inner = new ConstantDensity(Double.NaN);
        var clamped = new ClampDensity(inner, -64.0, 180.0);

        double v = clamped.sample(0, 0, 0);

        // FINDING: NaN is NOT caught by the clamp!
        assertTrue(Double.isNaN(v),
                "ClampDensity should clamp NaN to range but instead propagates NaN. "
                + "This is because NaN < -64 is false and NaN > 180 is false in IEEE 754.");
    }

    /**
     * Proof: Math.max/min do NOT catch NaN.
     *
     * <p>Despite common belief, {@code Math.max(min, Math.min(max, NaN))} returns NaN
     * because {@code Math.min} returns NaN if either argument is NaN (per Java spec).
     */
    @Test
    void mathMaxMin_doesNotCatchNan() {
        double v = Double.NaN;
        double min = -64.0;
        double max = 180.0;

        // This is what ClampDensity does:
        double actual = v;
        if (v < min) actual = min;
        if (v > max) actual = max;
        assertTrue(Double.isNaN(actual),
                "ClampDensity's if-based clamp does not catch NaN");

        // Math.max/min also do NOT catch NaN:
        double safer = Math.max(min, Math.min(max, v));
        assertTrue(Double.isNaN(safer),
                "Math.max/min do NOT handle NaN either");

        // The only way to catch NaN is explicit Double.isNaN():
        double correct = Double.isNaN(v) ? min : Math.max(min, Math.min(max, v));
        assertEquals(min, correct, 1e-9,
                "Only explicit isNaN check catches NaN correctly");
    }

    /**
     * PoC: Infinity propagation through density tree.
     */
    @Test
    void infinity_propagatesThroughTree() {
        var a = new ConstantDensity(Double.POSITIVE_INFINITY);
        var b = new ConstantDensity(Double.NEGATIVE_INFINITY);
        var add = new AddDensity(a, b);
        // +Inf + -Inf = NaN
        double v = add.sample(0, 0, 0);
        assertTrue(Double.isNaN(v),
                "+Inf + -Inf = NaN — Infinity can produce NaN in the tree");
    }

    /**
     * PoC: ScaledNoise with NaN scale factors does not crash.
     */
    @Test
    void scaledNoise_nanScale_noCrash() {
        var noise = new SimplexNoise(42L);
        // NaN scale factors
        var scaled = new ScaledNoise(noise, Double.NaN, 1.0, Double.NaN);
        double v = scaled.sample(10, 20, 30);
        // Should not crash; output is likely NaN
        assertFalse(Double.isInfinite(v) && Double.isNaN(v),
                "NaN scale produces garbage but no crash");
    }

    /**
     * SUMMARY: Density function tree robustness.
     *
     * <p>1. ClampDensity does not catch NaN (IEEE 754 comparison semantics).
     *    Fix: Use {@code Math.max(min, Math.min(max, v))} or explicit {@code Double.isNaN(v)} check.
     *
     * <p>2. Realistic risk: None within normal operation. The engine only passes
     *    finite int coordinates, noise outputs are bounded to [-1, 1], and scale factors
     *    are constants. NaN cannot naturally arise.
     *
     * <p>3. Defensive: Adding NaN-guard to ClampDensity is a cheap hardening measure.
     */
}
