package com.geoforge.engine.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DensityGuardTest {

    @Test
    void finiteValue_passesThrough() {
        double result = DensityGuard.clamp(5.0, -64, 320);
        assertEquals(5.0, result, 1e-12);
    }

    @Test
    void nan_becomesNegativeInfinity() {
        double result = DensityGuard.clamp(Double.NaN, -64, 320);
        assertTrue(Double.isInfinite(result) && result < 0);
    }

    @Test
    void positiveInfinity_becomesNegativeInfinity() {
        double result = DensityGuard.clamp(Double.POSITIVE_INFINITY, -64, 320);
        assertTrue(Double.isInfinite(result) && result < 0);
    }

    @Test
    void negativeInfinity_passesThrough() {
        double result = DensityGuard.clamp(Double.NEGATIVE_INFINITY, -64, 320);
        assertTrue(Double.isInfinite(result) && result < 0);
    }

    @Test
    void extremePositive_clamped() {
        double result = DensityGuard.clamp(1e10, -64, 320);
        // margin = (320 - (-64)) * 2 = 768, max = 320 + 768 = 1088
        assertTrue(result <= 1088, "Expected <= 1088 but got " + result);
    }

    @Test
    void extremeNegative_clamped() {
        double result = DensityGuard.clamp(-1e10, -64, 320);
        // margin = (320 - (-64)) * 2 = 768, min = -64 - 768 = -832
        assertTrue(result >= -832, "Expected >= -832 but got " + result);
    }

    @Test
    void zero_passesThrough() {
        assertEquals(0.0, DensityGuard.clamp(0.0, -64, 320), 1e-12);
    }

    @Test
    void negativeOne_passesThrough() {
        assertEquals(-1.0, DensityGuard.clamp(-1.0, -64, 320), 1e-12);
    }
}
