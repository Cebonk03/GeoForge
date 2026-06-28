package com.geoforge.engine.density;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.noise.NoiseSource;
import com.geoforge.engine.noise.SimplexNoise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Domain warp density tests")
class DomainWarpDensityTest {

    private static final NoiseSource NOISE = new SimplexNoise(42L);
    private static final DensityFunctionTree CONSTANT = (x, y, z) -> 42.0;

    @DisplayName("Zero amplitude delegates directly to inner function")
    @Test
    void zeroAmplitude_delegatesDirectly() {
        var warp = new DomainWarpDensity(NOISE, NOISE, CONSTANT, 0.0);
        assertEquals(42.0, warp.sample(10, 20, 30), 1e-12);
    }

    @DisplayName("Zero amplitude returns same result for any coordinates")
    @Test
    void zeroAmplitude_anyCoords_sameResult() {
        var warp = new DomainWarpDensity(NOISE, NOISE, CONSTANT, 0.0);
        assertEquals(warp.sample(1, 2, 3), warp.sample(100, 200, 300), 1e-12);
    }

    @DisplayName("Positive amplitude shifts coordinates by noise * amplitude")
    @Test
    void positiveAmplitude_shiftsCoordinates() {
        DensityFunctionTree extractX = (x, y, z) -> x;
        var warp = new DomainWarpDensity(NOISE, NOISE, extractX, 5.0);
        double expectedX = 10.0 + NOISE.sample2D(10, 30) * 5.0;
        assertEquals(expectedX, warp.sample(10, 20, 30), 1e-12);
    }

    @DisplayName("Same seed produces same output")
    @Test
    void deterministic_sameSeedSameOutput() {
        var warp1 = new DomainWarpDensity(NOISE, NOISE, CONSTANT, 5.0);
        var warp2 = new DomainWarpDensity(NOISE, NOISE, CONSTANT, 5.0);
        assertEquals(warp1.sample(10, 20, 30), warp2.sample(10, 20, 30), 1e-12);
    }

    @DisplayName("Record components are accessible")
    @Test
    void recordComponents_accessible() {
        var warp = new DomainWarpDensity(NOISE, NOISE, CONSTANT, 3.0);
        assertEquals(3.0, warp.amplitude(), 1e-12);
        assertSame(CONSTANT, warp.wrapped());
    }
}
