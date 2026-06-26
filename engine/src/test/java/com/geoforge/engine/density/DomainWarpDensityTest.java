package com.geoforge.engine.density;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.noise.NoiseSource;
import com.geoforge.engine.noise.SimplexNoise;
import org.junit.jupiter.api.Test;

class DomainWarpDensityTest {

    private static final NoiseSource NOISE = new SimplexNoise(42L);
    private static final DensityFunctionTree CONSTANT = (x, y, z) -> 42.0;

    @Test
    void zeroAmplitude_delegatesDirectly() {
        var warp = new DomainWarpDensity(NOISE, NOISE, CONSTANT, 0.0);
        assertEquals(42.0, warp.sample(10, 20, 30), 1e-12);
    }

    @Test
    void zeroAmplitude_anyCoords_sameResult() {
        var warp = new DomainWarpDensity(NOISE, NOISE, CONSTANT, 0.0);
        assertEquals(warp.sample(1, 2, 3), warp.sample(100, 200, 300), 1e-12);
    }

    void positiveAmplitude_shiftsCoordinates() {
        // Use positional function to verify coordinate shifting
        DensityFunctionTree positional = (x, y, z) -> x + y + z;
        var warp = new DomainWarpDensity(NOISE, NOISE, positional, 5.0);
        double direct = positional.sample(10, 20, 30);
        double warped = warp.sample(10, 20, 30);
        assertNotEquals(direct, warped, 1e-6);
    }

    @Test
    void deterministic_sameSeedSameOutput() {
        var warp1 = new DomainWarpDensity(NOISE, NOISE, CONSTANT, 5.0);
        var warp2 = new DomainWarpDensity(NOISE, NOISE, CONSTANT, 5.0);
        assertEquals(warp1.sample(10, 20, 30), warp2.sample(10, 20, 30), 1e-12);
    }

    @Test
    void recordComponents_accessible() {
        var warp = new DomainWarpDensity(NOISE, NOISE, CONSTANT, 3.0);
        assertEquals(3.0, warp.amplitude(), 1e-12);
        assertSame(CONSTANT, warp.wrapped());
    }
}
