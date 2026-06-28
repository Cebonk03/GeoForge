package com.geoforge.engine.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Density guard tests")
class DensityGuardTest {

    @DisplayName("Finite value passes through unchanged")
    @Test
    void finiteValue_passesThrough() {
        double result = DensityGuard.clamp(5.0, -64, 320);
        assertEquals(5.0, result, 1e-12);
    }

    @DisplayName("NaN becomes a finite sentinel value")
    @Test
    void nan_becomesFiniteSentinel() {
        double result = DensityGuard.clamp(Double.NaN, -64, 320);
        assertThat(result).isFinite();
        assertEquals(-832.0, result, 1e-12);
    }

    @DisplayName("Positive infinity becomes finite sentinel")
    @Test
    void positiveInfinity_becomesFiniteSentinel() {
        double result = DensityGuard.clamp(Double.POSITIVE_INFINITY, -64, 320);
        assertThat(result).isFinite();
        assertEquals(-832.0, result, 1e-12);
    }

    @DisplayName("Negative infinity becomes finite sentinel")
    @Test
    void negativeInfinity_becomesFiniteSentinel() {
        double result = DensityGuard.clamp(Double.NEGATIVE_INFINITY, -64, 320);
        assertThat(result).isFinite();
        assertEquals(-832.0, result, 1e-12);
    }

    @DisplayName("Extreme positive values are clamped to max + margin")
    @Test
    void extremePositive_clamped() {
        double result = DensityGuard.clamp(1e10, -64, 320);
        assertThat(result).isLessThanOrEqualTo(1088);
    }

    @DisplayName("Extreme negative values are clamped to min - margin")
    @Test
    void extremeNegative_clamped() {
        double result = DensityGuard.clamp(-1e10, -64, 320);
        assertThat(result).isGreaterThanOrEqualTo(-832);
    }

    @DisplayName("Zero passes through")
    @Test
    void zero_passesThrough() {
        assertEquals(0.0, DensityGuard.clamp(0.0, -64, 320), 1e-12);
    }

    @DisplayName("Negative one passes through")
    @Test
    void negativeOne_passesThrough() {
        assertEquals(-1.0, DensityGuard.clamp(-1.0, -64, 320), 1e-12);
    }
}
