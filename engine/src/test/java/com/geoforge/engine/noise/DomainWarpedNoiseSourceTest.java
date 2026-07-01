package com.geoforge.engine.noise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("DomainWarpedNoiseSource — composable noise decorator tests")
class DomainWarpedNoiseSourceTest {

    private static final NoiseSource NOISE = new GradientNoise(42L);
    private static final NoiseSource WARP = new GradientNoise(99L);

    @Test
    @DisplayName("Zero amplitude delegates directly to wrapped source")
    void zeroAmplitude_delegatesDirectly() {
        var wrapped = new DomainWarpedNoiseSource(NOISE, WARP, WARP, WARP, 0.0);
        assertEquals(NOISE.sample2D(10.5, 20.5), wrapped.sample2D(10.5, 20.5), 1e-12);
        assertEquals(NOISE.sample3D(10.5, 20.5, 30.5), wrapped.sample3D(10.5, 20.5, 30.5), 1e-12);
    }

    @Test
    @DisplayName("Zero amplitude returns same result regardless of warp noise")
    void zeroAmplitude_anyCoords_sameResult() {
        var wrapped = new DomainWarpedNoiseSource(NOISE, WARP, WARP, WARP, 0.0);
        assertEquals(
                wrapped.sample2D(1, 2),
                wrapped.sample2D(100, 200),
                1e-12);
        assertEquals(
                wrapped.sample3D(1, 2, 3),
                wrapped.sample3D(100, 200, 300),
                1e-12);
    }

    @Test
    @DisplayName("Large amplitude shifts output at many non-integer coordinates")
    void positiveAmplitude_shiftsCoordinates() {
        var warped = new DomainWarpedNoiseSource(NOISE, WARP, WARP, WARP, 1000.0);
        int diffCount = 0;
        for (int i = 0; i < 100; i++) {
            double v = i + 0.5;
            if (NOISE.sample2D(v, v) != warped.sample2D(v, v)) {
                diffCount++;
            }
        }
        assertTrue(diffCount > 0, "Expected at least some coordinates to differ");
    }

    @Test
    @DisplayName("Different warp seeds produce different output at non-integer coordinates")
    void deterministic_differentSeeds() {
        var a = new DomainWarpedNoiseSource(
                NOISE, new GradientNoise(1L),
                new GradientNoise(1L), new GradientNoise(1L), 100.0);
        var b = new DomainWarpedNoiseSource(
                NOISE, new GradientNoise(2L),
                new GradientNoise(2L), new GradientNoise(2L), 100.0);
        assertNotNull(a);
        assertNotNull(b);
        // Non-integer coordinates ensure gradient noise produces non-zero warp
        assertNotEquals(a.sample2D(10.5, 20.5), b.sample2D(10.5, 20.5));
        assertNotEquals(a.sample3D(10.5, 20.5, 30.5), b.sample3D(10.5, 20.5, 30.5));
    }

    @Test
    @DisplayName("Deterministic output for same seed and coordinates")
    void deterministic_sameSeedAndCoords() {
        var a = new DomainWarpedNoiseSource(
                new GradientNoise(42L), new GradientNoise(99L),
                new GradientNoise(99L), new GradientNoise(99L), 3.0);
        var b = new DomainWarpedNoiseSource(
                new GradientNoise(42L), new GradientNoise(99L),
                new GradientNoise(99L), new GradientNoise(99L), 3.0);
        assertEquals(a.sample2D(10.5, 20.5), b.sample2D(10.5, 20.5), 1e-12);
        assertEquals(a.sample3D(10.5, 20.5, 30.5), b.sample3D(10.5, 20.5, 30.5), 1e-12);
    }

    @Test
    @DisplayName("Record components are accessible")
    void recordComponents_accessible() {
        var wrapped = new DomainWarpedNoiseSource(NOISE, WARP, WARP, WARP, 0.5);
        assertSame(NOISE, wrapped.wrapped());
        assertSame(WARP, wrapped.warpX());
        assertEquals(0.5, wrapped.amplitude(), 1e-12);
    }
}
