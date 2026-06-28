package com.geoforge.engine.density;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("unit")
@DisplayName("Cave Y-envelope tests")
class CaveYEnvelopeTest {

    @DisplayName("Envelope at center deep underground returns near one")
    @Test
    void envelope_atCenter_deepUnderground_returnsNearOne() {
        double result = CaveYEnvelope.envelope(-20, -12, -20, 48, 8);
        assertEquals(1.0, result, 0.02);
    }

    @DisplayName("Envelope at surface is fully suppressed")
    @Test
    void envelope_atSurface_suppressed() {
        double result = CaveYEnvelope.envelope(63, 63, -20, 48, 8);
        assertEquals(0.0, result, 1e-12);
    }

    @DisplayName("Envelope above surface is fully suppressed")
    @Test
    void envelope_aboveSurface_suppressed() {
        double result = CaveYEnvelope.envelope(70, 63, -20, 48, 8);
        assertEquals(0.0, result, 1e-12);
    }

    @DisplayName("Envelope far below center is small")
    @Test
    void envelope_farBelow_small() {
        double result = CaveYEnvelope.envelope(-200, 63, -20, 48, 8);
        assertThat(result).isLessThan(0.2);
    }

    @DisplayName("Envelope with zero spread returns zero")
    @Test
    void envelope_zeroSpread_returnsZero() {
        double result = CaveYEnvelope.envelope(0, 63, -20, 0, 8);
        assertEquals(0.0, result, 1e-12);
    }

    @DisplayName("Envelope with negative spread returns zero")
    @Test
    void envelope_negativeSpread_returnsZero() {
        double result = CaveYEnvelope.envelope(0, 63, -20, -1, 8);
        assertEquals(0.0, result, 1e-12);
    }

    @DisplayName("Envelope with zero surface cutoff allows caves anywhere below surface")
    @Test
    void envelope_zeroSurfaceCutoff_allowsCaves() {
        double result = CaveYEnvelope.envelope(50, 63, -20, 48, 0);
        assertThat(result).isPositive().isLessThan(1.0);
    }

    @DisplayName("Envelope below surface has partial suppression")
    @Test
    void envelope_belowSurface_partialSuppression() {
        double withSuppression = CaveYEnvelope.envelope(59, 63, -20, 48, 8);
        double withoutSuppression = CaveYEnvelope.envelope(59, 67, -20, 48, 8);
        assertThat(withSuppression).isBetween(0.0, 1.0);
        assertThat(withSuppression).isLessThan(withoutSuppression);
    }

    @DisplayName("Envelope deep below surface has full (un-suppressed) value")
    @Test
    void envelope_deepBelowSurface_fullSuppression() {
        double result = CaveYEnvelope.envelope(-50, 63, -20, 48, 8);
        assertThat(result).isPositive();
    }

    @DisplayName("Envelope result is always in [0, 1] range")
    @ParameterizedTest
    @MethodSource("envelopeYPositions")
    void envelope_resultInZeroOneRange(double y) {
        double result = CaveYEnvelope.envelope(y, 63, -20, 48, 8);
        assertThat(result).isBetween(0.0, 1.0);
    }

    @DisplayName("Envelope guards against NaN for extreme inputs")
    @Test
    void envelope_guardsAgainstNaN() {
        double result = CaveYEnvelope.envelope(-1_000_000, 63, -20, 48, 8);
        assertThat(result).isFinite();
    }

    @DisplayName("Envelope with very narrow spread still produces finite result")
    @Test
    void envelope_veryNarrowSpread() {
        double result = CaveYEnvelope.envelope(-20, 63, -20, 0.1, 8);
        assertThat(result).isBetween(0.0, 1.0);
        assertThat(result).isFinite();
    }

    @DisplayName("Envelope with negative surface cutoff treated as zero (no suppression)")
    @Test
    void envelope_negativeSurfaceCutoff_treatedAsZero() {
        double result = CaveYEnvelope.envelope(50, 63, -20, 48, -1);
        assertThat(result).isPositive().isLessThan(1.0);
    }

    @DisplayName("Envelope is symmetric around center when surface suppression is uniform")
    @Test
    void envelope_symmetryAroundCenter() {
        double pos = CaveYEnvelope.envelope(-20 + 30, 20, -20, 48, 8);
        double neg = CaveYEnvelope.envelope(-20 - 30, 20, -20, 48, 8);
        assertEquals(pos, neg, 1e-12);
    }

    @DisplayName("Wider spread produces larger envelope value at same offset from center")
    @Test
    void envelope_increasingSpread_smootherFalloff() {
        double narrow = CaveYEnvelope.envelope(-60, -52, -20, 30, 8);
        double wide = CaveYEnvelope.envelope(-60, -52, -20, 60, 8);
        assertThat(wide).isGreaterThan(narrow);
    }

    private static Stream<Arguments> envelopeYPositions() {
        var args = new java.util.ArrayList<Arguments>();
        for (double y = -100; y <= 100; y += 10) {
            args.add(Arguments.of(y));
        }
        return args.stream();
    }
}
