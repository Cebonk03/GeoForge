package com.geoforge.engine.density;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CaveYEnvelopeTest {

    @Test
    void envelope_atCenter_returnsNearOne() {
        // At caveCenterY with surfaceY == y (no surface suppression), Gaussian peaks at ~1.0
        double result = CaveYEnvelope.envelope(-20, -20, -20, 48, 8);
        assertEquals(1.0, result, 0.02);
    }

    @Test
    void envelope_farBelow_small() {
        double result = CaveYEnvelope.envelope(-200, 63, -20, 48, 8);
        assertTrue(result < 0.2);
    }

    @Test
    void envelope_zeroSpread_returnsZero() {
        double result = CaveYEnvelope.envelope(0, 63, -20, 0, 8);
        assertEquals(0.0, result, 1e-12);
    }

    @Test
    void envelope_negativeSpread_returnsZero() {
        double result = CaveYEnvelope.envelope(0, 63, -20, -1, 8);
        assertEquals(0.0, result, 1e-12);
    }

    @Test
    void envelope_zeroSurfaceCutoff_fullSuppressionBelowSurface() {
        // When surfaceCutoff <= 0, surfaceFactor=1.0 for all y below surface
        double result = CaveYEnvelope.envelope(50, 63, -20, 48, 0);
        assertEquals(0.0, result, 1e-12);
    }

    @Test
    void envelope_belowSurface_partialSuppression() {
        // y=59, surfaceY=63 => (63-59)/8 = 0.5, so surfaceFactor=0.5
        // envelope = verticalFactor * (1.0 - 0.5) = verticalFactor * 0.5
        double withSuppression = CaveYEnvelope.envelope(59, 63, -20, 48, 8);
        // Without surface suppression (y >= surfaceY): envelope = verticalFactor alone
        double withoutSuppression = CaveYEnvelope.envelope(59, 58, -20, 48, 8);
        assertTrue(withSuppression > 0 && withSuppression < 1.0);
        assertTrue(withSuppression < withoutSuppression,
                "surface suppression should reduce envelope value");
    }

    @Test
    void envelope_atSurface_noSurfaceSuppression() {
        // At y == surfaceY, surfaceFactor = 0 (y < surfaceY is false)
        // Result is the raw Gaussian value (> 0, < 1 for this config)
        double result = CaveYEnvelope.envelope(63, 63, -20, 48, 8);
        assertTrue(result > 0);
        assertTrue(result < 1.0);
    }

    @Test
    void envelope_aboveSurface_noSurfaceSuppression() {
        // Above surface, surfaceFactor = 0 — only Gaussian contributes
        double result = CaveYEnvelope.envelope(70, 63, -20, 48, 8);
        assertTrue(result > 0);
        assertTrue(result < 1.0);
    }

    @Test
    void envelope_deepBelowSurface_notSuppressed() {
        // When surfaceY is below y, surfaceFactor=0 (y >= surfaceY)
        // Both positions are far from center but have same envelope value
        double atDepth = CaveYEnvelope.envelope(-150, -160, -20, 48, 8);
        double deeper = CaveYEnvelope.envelope(-200, -210, -20, 48, 8);
        // Both have surfaceFactor=0, so results are pure Gaussian values
        assertTrue(atDepth > 0);
        assertTrue(deeper > 0);
    }

    @Test
    void envelope_resultInZeroOneRange() {
        for (double y = -100; y <= 100; y += 10) {
            double result = CaveYEnvelope.envelope(y, 63, -20, 48, 8);
            assertTrue(result >= 0.0 && result <= 1.0,
                    "y=" + y + " gave " + result);
        }
    }

    @Test
    void envelope_guardsAgainstNaN() {
        double result = CaveYEnvelope.envelope(-1_000_000, 63, -20, 48, 8);
        assertTrue(Double.isFinite(result));
    }

    @Test
    void envelope_veryNarrowSpread() {
        double result = CaveYEnvelope.envelope(-20, 63, -20, 0.1, 8);
        assertTrue(result >= 0.0 && result <= 1.0);
        assertTrue(Double.isFinite(result));
    }

    @Test
    void envelope_negativeSurfaceCutoff_treatedAsZero() {
        // SurfaceCutoff <= 0 means surfaceFactor=1.0 for all y below surface
        double result = CaveYEnvelope.envelope(50, 63, -20, 48, -1);
        assertEquals(0.0, result, 1e-12);
    }

    @Test
    void envelope_symmetryAroundCenter() {
        // When surfaceFactor=0 (y >= surfaceY), Gaussian should be symmetric
        double pos = CaveYEnvelope.envelope(-20 + 30, -60, -20, 48, 8);
        double neg = CaveYEnvelope.envelope(-20 - 30, -60, -20, 48, 8);
        assertEquals(pos, neg, 1e-12);
    }

    @Test
    void envelope_increasingSpread_smootherFalloff() {
        // At same offset from center, wider spread = larger value
        // Use y >= surfaceY to avoid surface suppression
        double narrow = CaveYEnvelope.envelope(-60, -60, -20, 30, 8);
        double wide = CaveYEnvelope.envelope(-60, -60, -20, 60, 8);
        assertTrue(wide > narrow, "wider spread should be larger at same offset");
    }
}
