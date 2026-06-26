package com.geoforge.engine.density;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.config.GeoForgeConfig;
import org.junit.jupiter.api.Test;

class EnhancedCaveSystemTest {

    private static final double EPSILON = 1e-12;

    // ──────────────────────────────────────────────
    // Config helpers: wide envelope so caves form
    // ──────────────────────────────────────────────
    private static GeoForgeConfig baseConfig() {
        return GeoForgeConfig.builder()
                .caveCenterY(0.0)
                .caveSpread(100.0)
                .caveSurfaceCutoff(500.0)
                .caveAmplitude(8.0)
                .build();
    }

    // ──────────────────────────────────────────────
    // 1) All thresholds at extreme (disabled)
    // ──────────────────────────────────────────────

    @Test
    void allThresholdsDisabled_densityUnchanged() {
        var cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.0)
                .caveCheeseThreshold(1.0)
                .caveNoodleThreshold(0.0)
                .build();

        double result = EnhancedCaveSystem.carve(
                5.0, 0.5, 0.5, 0.5,
                0, 63, cfg);

        assertEquals(5.0, result, EPSILON, "density should pass through unchanged");
    }

    // ──────────────────────────────────────────────
    // 2) Spaghetti carving
    // ──────────────────────────────────────────────

    @Test
    void spaghettiThresholdZero_noSpaghettiCarving() {
        var cfg = baseConfig();
        cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.0)
                .caveCheeseThreshold(1.0)
                .caveNoodleThreshold(0.0)
                .caveCenterY(cfg.caveCenterY())
                .caveSpread(cfg.caveSpread())
                .caveSurfaceCutoff(cfg.caveSurfaceCutoff())
                .build();

        // |noise| = 0.25, threshold = 0 → |0.25| < 0 is false → no spaghetti
        double result = EnhancedCaveSystem.carve(
                5.0, 0.25, 0.5, 0.5,
                0, 63, cfg);

        assertEquals(5.0, result, EPSILON, "zero spaghetti threshold should disable spaghetti");
    }

    @Test
    void spaghettiThresholdActive_carvesAir() {
        var cfg = baseConfig();
        cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.5)
                .caveCheeseThreshold(1.0)
                .caveNoodleThreshold(0.0)
                .caveCenterY(cfg.caveCenterY())
                .caveSpread(cfg.caveSpread())
                .caveSurfaceCutoff(cfg.caveSurfaceCutoff())
                .build();

        // |noise| = 0.1 < 0.5 → spaghetti condition met → density should be reduced
        double result = EnhancedCaveSystem.carve(
                5.0, 0.1, 0.5, 0.5,
                0, 63, cfg);

        assertTrue(result < 0, "spaghetti carving should make density negative (air)");
    }

    @Test
    void spaghettiThresholdNoiseAbove_noCarving() {
        var cfg = baseConfig();
        cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.5)
                .caveCheeseThreshold(1.0)
                .caveNoodleThreshold(0.0)
                .caveCenterY(cfg.caveCenterY())
                .caveSpread(cfg.caveSpread())
                .caveSurfaceCutoff(cfg.caveSurfaceCutoff())
                .build();

        // |noise| = 0.6 > 0.5 → condition not met → no carving
        double result = EnhancedCaveSystem.carve(
                5.0, 0.6, 0.5, 0.5,
                0, 63, cfg);

        assertEquals(5.0, result, EPSILON,
                "spaghetti noise above threshold should not carve");
    }

    // ──────────────────────────────────────────────
    // 3) Cheese carving
    // ──────────────────────────────────────────────

    @Test
    void cheeseThresholdHigh_noCheeseCarving() {
        var cfg = baseConfig();
        cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.0)
                .caveCheeseThreshold(0.99)
                .caveNoodleThreshold(0.0)
                .caveCenterY(cfg.caveCenterY())
                .caveSpread(cfg.caveSpread())
                .caveSurfaceCutoff(cfg.caveSurfaceCutoff())
                .build();

        // noise = 0.5, threshold = 0.99 → 0.5 > 0.99 is false → no cheese
        double result = EnhancedCaveSystem.carve(
                5.0, 0.5, 0.5, 0.5,
                0, 63, cfg);

        assertEquals(5.0, result, EPSILON,
                "cheese threshold near 1 should disable cheese caves");
    }

    @Test
    void cheeseThresholdActive_carvesAir() {
        var cfg = baseConfig();
        cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.0)
                .caveCheeseThreshold(0.3)
                .caveNoodleThreshold(0.0)
                .caveCenterY(cfg.caveCenterY())
                .caveSpread(cfg.caveSpread())
                .caveSurfaceCutoff(cfg.caveSurfaceCutoff())
                .build();

        // noise = 0.5 > 0.3 → cheese condition met → density should be reduced
        double result = EnhancedCaveSystem.carve(
                5.0, 0.5, 0.5, 0.5,
                0, 63, cfg);

        assertTrue(result < 0, "cheese carving should make density negative (air)");
    }

    // ──────────────────────────────────────────────
    // 4) Noodle carving
    // ──────────────────────────────────────────────

    @Test
    void noodleThresholdZero_noNoodleCarving() {
        var cfg = baseConfig();
        cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.0)
                .caveCheeseThreshold(1.0)
                .caveNoodleThreshold(0.0)
                .caveCenterY(cfg.caveCenterY())
                .caveSpread(cfg.caveSpread())
                .caveSurfaceCutoff(cfg.caveSurfaceCutoff())
                .build();

        // |0.3| + |0.3| = 0.6 > 0 → no carving
        double result = EnhancedCaveSystem.carve(
                5.0, 0.5, 0.3, 0.3,
                0, 63, cfg);

        assertEquals(5.0, result, EPSILON,
                "zero noodle threshold should disable noodle caves");
    }

    @Test
    void noodleThresholdActive_carvesAir() {
        var cfg = baseConfig();
        cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.0)
                .caveCheeseThreshold(1.0)
                .caveNoodleThreshold(0.5)
                .caveCenterY(cfg.caveCenterY())
                .caveSpread(cfg.caveSpread())
                .caveSurfaceCutoff(cfg.caveSurfaceCutoff())
                .build();

        // |0.1| + |0.1| = 0.2 < 0.5 → noodle condition met
        double result = EnhancedCaveSystem.carve(
                5.0, 0.5, 0.1, 0.1,
                0, 63, cfg);

        assertTrue(result < 0, "noodle carving should make density negative (air)");
    }

    // ──────────────────────────────────────────────
    // 5) Deterministic output
    // ──────────────────────────────────────────────

    @Test
    void deterministicSameInputSameOutput() {
        var cfg = baseConfig();
        cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.5)
                .caveCheeseThreshold(0.5)
                .caveNoodleThreshold(0.3)
                .caveCenterY(cfg.caveCenterY())
                .caveSpread(cfg.caveSpread())
                .caveSurfaceCutoff(cfg.caveSurfaceCutoff())
                .build();

        double first = EnhancedCaveSystem.carve(
                5.0, 0.2, 0.15, 0.15,
                10, 63, cfg);

        for (int i = 0; i < 20; i++) {
            double repeated = EnhancedCaveSystem.carve(
                    5.0, 0.2, 0.15, 0.15,
                    10, 63, cfg);
            assertEquals(first, repeated, EPSILON,
                    "carve should be deterministic for the same inputs");
        }
    }

    // ──────────────────────────────────────────────
    // 6) CaveYEnvelope suppresses caves at surface
    // ──────────────────────────────────────────────

    @Test
    void cavesSuppressedAtSurface() {
        // Narrow surface cutoff so envelope → 0 near surface
        var cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.5)
                .caveCheeseThreshold(1.0)
                .caveNoodleThreshold(0.0)
                .caveCenterY(-20.0)
                .caveSpread(48.0)
                .caveSurfaceCutoff(8.0)
                .build();

        // At y == surfaceY: surfaceFactor = 0, envelope = verticalFactor * 0 = 0
        // EnhancedCaveSystem gate: envelope < 1e-6 → return original density unchanged
        double atSurface = EnhancedCaveSystem.carve(
                5.0, 0.1, 0.5, 0.5,
                63, 63, cfg);

        assertEquals(5.0, atSurface, 1e-6,
                "caves must NOT carve at the surface (envelope = 0)");

        // y = 55 (8 blocks below surface, surfaceY = 63):
        // surfaceFactor = min(1, (63-55)/8) = 1.0 → envelope = verticalFactor * 0 = 0
        // y = 55 (8 blocks below surface, surfaceY = 63):
        // At cutoff boundary: surfaceFactor = min(1, 8/8) = 1.0
        // With fixed formula: envelope = verticalFactor * 1.0 = verticalFactor
        // Caves CAN carve here since envelope > 0
        double eightBelow = EnhancedCaveSystem.carve(
                5.0, 0.1, 0.5, 0.5,
                55, 63, cfg);

        assertTrue(eightBelow < 5.0,
                "caves can carve at cutoff boundary (envelope = verticalFactor > 0)");

        // y = 62 (1 block below surface), surfaceY = 63
        // surfaceFactor = (63-62)/8 = 0.125 → envelope = verticalFactor * 0.875
        // Envelope near surface is non-zero, so carving CAN occur
        double nearSurface = EnhancedCaveSystem.carve(
                5.0, 0.1, 0.5, 0.5,
                62, 63, cfg);

        assertTrue(nearSurface < 5.0,
                "caves may be possible close to surface (within cutoff distance)");

    }

    // ──────────────────────────────────────────────
    // 7) Threshold clamping
    // ──────────────────────────────────────────────

    @Test
    void thresholdsClampedToUnitInterval() {
        // Negative thresholds should behave as 0 (disabled)
        var cfgNeg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(-0.5)
                .caveCheeseThreshold(1.5)
                .caveNoodleThreshold(-1.0)
                .build();

        double result = EnhancedCaveSystem.carve(
                5.0, 0.5, 0.5, 0.5,
                0, 63, cfgNeg);

        assertEquals(5.0, result, EPSILON,
                "negative thresholds should be clamped to 0 (disabled)");
    }

    // ──────────────────────────────────────────────
    // 8) Cheese threshold below 0 is treated as 0
    // ──────────────────────────────────────────────

    @Test
    void cheeseThresholdBelowZero_allCheeseActive() {
        var cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.0)
                .caveCheeseThreshold(-0.1)
                .caveNoodleThreshold(0.0)
                .caveCenterY(0.0)
                .caveSpread(100.0)
                .caveSurfaceCutoff(500.0)
                .build();

        // noise = -0.2, cheese threshold clamped to 0 → -0.2 > 0 is false → no cheese
        double result = EnhancedCaveSystem.carve(
                5.0, -0.2, 0.5, 0.5,
                0, 63, cfg);

        assertEquals(5.0, result, EPSILON,
                "clamped cheese threshold 0 should not trigger on negative noise");

        // noise = 0.1 > 0 (clamped threshold) → cheese active
        double resultPositive = EnhancedCaveSystem.carve(
                5.0, 0.1, 0.5, 0.5,
                0, 63, cfg);

        assertTrue(resultPositive < 0,
                "clamped cheese threshold 0 should carve on positive noise");
    }

    // ──────────────────────────────────────────────
    // 9) Already negative density stays negative
    // ──────────────────────────────────────────────

    @Test
    void alreadyNegativeDensity_notMadePositive() {
        var cfg = baseConfig();
        cfg = GeoForgeConfig.builder()
                .caveSpaghettiThreshold(0.5)
                .caveCheeseThreshold(1.0)
                .caveNoodleThreshold(0.0)
                .caveCenterY(cfg.caveCenterY())
                .caveSpread(cfg.caveSpread())
                .caveSurfaceCutoff(cfg.caveSurfaceCutoff())
                .build();

        // Density is already very negative (-10.0), spaghetti condition met
        // Should remain equally or more negative
        double result = EnhancedCaveSystem.carve(
                -10.0, 0.1, 0.5, 0.5,
                0, 63, cfg);

        assertTrue(result <= -10.0, "already negative density should stay negative or more so");
    }
}
