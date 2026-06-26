package com.geoforge.engine.density;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CaveYEnvelopeTest {

    @Test
    void envelope_atCenter_deepUnderground_returnsNearOne() {
        // y=caveCenterY=-20, surfaceY far above so surfaceFactor=1.0
        // Need y < surfaceY AND (surfaceY - y) >= cutoff for surfaceFactor=1.0
        double result = CaveYEnvelope.envelope(-20, -12, -20, 48, 8);
        assertEquals(1.0, result, 0.02);
    }

    @Test
    void envelope_atSurface_suppressed() {
        // At y == surfaceY: surfaceFactor=0, envelope = 0
        double result = CaveYEnvelope.envelope(63, 63, -20, 48, 8);
        assertEquals(0.0, result, 1e-12);
    }

    @Test
    void envelope_aboveSurface_suppressed() {
        // Above surface (y > surfaceY): surfaceFactor=0, envelope = 0
        double result = CaveYEnvelope.envelope(70, 63, -20, 48, 8);
        assertEquals(0.0, result, 1e-12);
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
    void envelope_zeroSurfaceCutoff_allowsCaves() {
        // When surfaceCutoff <= 0, surfaceFactor=1.0 (no surface suppression ramp)
        // Envelope = verticalFactor (Gaussian only)
        double result = CaveYEnvelope.envelope(50, 63, -20, 48, 0);
        assertTrue(result > 0, "Without cutoff, envelope should allow caves");
        assertTrue(result < 1.0);
    }

    @Test
    void envelope_belowSurface_partialSuppression() {
        // y=59, surfaceY=63 => (63-59)/8 = 0.5, surfaceFactor=0.5
        // envelope = verticalFactor * 0.5
        double withSuppression = CaveYEnvelope.envelope(59, 63, -20, 48, 8);
        // Same y but surfaceY far enough for surfaceFactor=1.0 => envelope = verticalFactor
        double withoutSuppression = CaveYEnvelope.envelope(59, 67, -20, 48, 8);
        assertTrue(withSuppression > 0 && withSuppression < 1.0);
        assertTrue(withSuppression < withoutSuppression,
                "surface suppression should reduce envelope below unsuppressed value");
    }

    @Test
    void envelope_deepBelowSurface_fullSuppression() {
        // When (surfaceY - y) >= caveSurfaceCutoff, surfaceFactor = 1.0
        // Envelope = verticalFactor * 1.0 = Gaussian
        double result = CaveYEnvelope.envelope(-50, 63, -20, 48, 8);
        assertTrue(result > 0, "Deep below surface should have full envelope");
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
        // Envelope = verticalFactor (not forced to 0)
        double result = CaveYEnvelope.envelope(50, 63, -20, 48, -1);
        assertTrue(result > 0, "Negative cutoff should not suppress all caves");
        assertTrue(result < 1.0);
    }

    @Test
    void envelope_symmetryAroundCenter() {
        // When surfaceFactor=1.0 (deep below surface), Gaussian is symmetric
        // Both positions need surfaceFactor=1.0: surfaceY >= both y + cutoff
        double pos = CaveYEnvelope.envelope(-20 + 30, 20, -20, 48, 8);
        double neg = CaveYEnvelope.envelope(-20 - 30, 20, -20, 48, 8);
        assertEquals(pos, neg, 1e-12);
    }

    @Test
    void envelope_increasingSpread_smootherFalloff() {
        // Same offset from center, wider spread = larger value when surfaceFactor=1.0
        double narrow = CaveYEnvelope.envelope(-60, -52, -20, 30, 8);
        double wide = CaveYEnvelope.envelope(-60, -52, -20, 60, 8);
        assertTrue(wide > narrow, "wider spread should be larger at same offset");
    }
}
