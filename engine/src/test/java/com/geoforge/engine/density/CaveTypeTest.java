package com.geoforge.engine.density;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.geoforge.engine.config.GeoForgeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("CaveType enum tests")
class CaveTypeTest {

    private static final double EPSILON = 1e-12;
    private static final GeoForgeConfig DEFAULT_CFG = GeoForgeConfig.defaults();

    @DisplayName("SPAGHETTI threshold reads the correct config field")
    @Test
    void spaghettiThreshold_readsConfigField() {
        GeoForgeConfig cfg = GeoForgeConfig.builder().caveSpaghettiThreshold(0.42).build();
        assertEquals(0.42, CaveType.SPAGHETTI.threshold(cfg), EPSILON);
    }

    @DisplayName("CHEESE threshold reads the correct config field")
    @Test
    void cheeseThreshold_readsConfigField() {
        GeoForgeConfig cfg = GeoForgeConfig.builder().caveCheeseThreshold(0.73).build();
        assertEquals(0.73, CaveType.CHEESE.threshold(cfg), EPSILON);
    }

    @DisplayName("NOODLE threshold reads the correct config field")
    @Test
    void noodleThreshold_readsConfigField() {
        GeoForgeConfig cfg = GeoForgeConfig.builder().caveNoodleThreshold(0.19).build();
        assertEquals(0.19, CaveType.NOODLE.threshold(cfg), EPSILON);
    }

    @DisplayName("SPAGHETTI is disabled when threshold is at or near zero")
    @Test
    void spaghetti_isDisabled_whenThresholdZero() {
        assertTrue(CaveType.SPAGHETTI.isDisabled(0.0));
        assertTrue(CaveType.SPAGHETTI.isDisabled(-0.1));
        assertTrue(CaveType.SPAGHETTI.isDisabled(EPSILON / 2));
        assertTrue(CaveType.SPAGHETTI.isDisabled(EPSILON));
        assertFalse(CaveType.SPAGHETTI.isDisabled(0.3));
    }

    @DisplayName("CHEESE is disabled when threshold is at or near one")
    @Test
    void cheese_isDisabled_whenThresholdOne() {
        assertTrue(CaveType.CHEESE.isDisabled(1.0));
        assertTrue(CaveType.CHEESE.isDisabled(1.1));
        assertTrue(CaveType.CHEESE.isDisabled(1.0 - EPSILON / 2));
        assertTrue(CaveType.CHEESE.isDisabled(1.0 - EPSILON));
        assertFalse(CaveType.CHEESE.isDisabled(0.5));
    }

    @DisplayName("NOODLE is disabled when threshold is at or near zero")
    @Test
    void noodle_isDisabled_whenThresholdZero() {
        assertTrue(CaveType.NOODLE.isDisabled(0.0));
        assertTrue(CaveType.NOODLE.isDisabled(-0.1));
        assertTrue(CaveType.NOODLE.isDisabled(EPSILON / 2));
        assertTrue(CaveType.NOODLE.isDisabled(EPSILON));
        assertFalse(CaveType.NOODLE.isDisabled(0.15));
    }

    @DisplayName("SPAGHETTI test returns true when |noise3D| < threshold")
    @Test
    void spaghetti_test_absNoiseBelowThreshold() {
        assertTrue(CaveType.SPAGHETTI.test(0.1, 0, 0, 0.5));
        assertTrue(CaveType.SPAGHETTI.test(-0.1, 0, 0, 0.5));
        assertTrue(CaveType.SPAGHETTI.test(0.0, 0, 0, 0.3));
    }

    @DisplayName("SPAGHETTI test returns false when |noise3D| >= threshold")
    @Test
    void spaghetti_test_absNoiseAboveThreshold() {
        assertFalse(CaveType.SPAGHETTI.test(0.6, 0, 0, 0.5));
        assertFalse(CaveType.SPAGHETTI.test(-0.6, 0, 0, 0.5));
        assertFalse(CaveType.SPAGHETTI.test(0.3, 0, 0, 0.3));
    }

    @DisplayName("CHEESE test returns true when noise3D > threshold")
    @Test
    void cheese_test_noiseAboveThreshold() {
        assertTrue(CaveType.CHEESE.test(0.7, 0, 0, 0.3));
        assertTrue(CaveType.CHEESE.test(0.31, 0, 0, 0.3));
    }

    @DisplayName("CHEESE test returns false when noise3D <= threshold")
    @Test
    void cheese_test_noiseAtOrBelowThreshold() {
        assertFalse(CaveType.CHEESE.test(0.3, 0, 0, 0.3));
        assertFalse(CaveType.CHEESE.test(0.1, 0, 0, 0.3));
        assertFalse(CaveType.CHEESE.test(-0.5, 0, 0, 0.3));
    }

    @DisplayName("NOODLE test returns true when |noiseA| + |noiseB| < threshold")
    @Test
    void noodle_test_sumBelowThreshold() {
        assertTrue(CaveType.NOODLE.test(0, 0.1, 0.1, 0.5));
        assertTrue(CaveType.NOODLE.test(0, -0.1, 0.2, 0.5));
    }

    @DisplayName("NOODLE test returns false when |noiseA| + |noiseB| >= threshold")
    @Test
    void noodle_test_sumAtOrAboveThreshold() {
        assertFalse(CaveType.NOODLE.test(0, 0.3, 0.3, 0.5));
        assertFalse(CaveType.NOODLE.test(0, 0.5, 0.0, 0.5));
        assertFalse(CaveType.NOODLE.test(0, 0.1, 0.1, 0.2));
    }

    @DisplayName("NoiseA and noiseB are ignored by spaghetti and cheese")
    @Test
    void spaghettiAndCheese_ignoreNoiseAAndNoiseB() {
        assertTrue(CaveType.SPAGHETTI.test(0.1, 100.0, 100.0, 0.5));
        assertFalse(CaveType.SPAGHETTI.test(0.6, -100.0, -100.0, 0.5));
        assertTrue(CaveType.CHEESE.test(0.7, 100.0, 100.0, 0.3));
        assertFalse(CaveType.CHEESE.test(0.1, -100.0, -100.0, 0.3));
    }

    @DisplayName("Enum values are ordered: SPAGHETTI, CHEESE, NOODLE")
    @Test
    void valuesOrderIsSpaghettiCheeseNoodle() {
        CaveType[] values = CaveType.values();
        assertThat(values).hasSize(3);
        assertEquals(CaveType.SPAGHETTI, values[0]);
        assertEquals(CaveType.CHEESE, values[1]);
        assertEquals(CaveType.NOODLE, values[2]);
    }
}
