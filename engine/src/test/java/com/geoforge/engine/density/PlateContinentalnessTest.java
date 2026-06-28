package com.geoforge.engine.density;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.geology.TectonicPlateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Plate continentalness tests")
class PlateContinentalnessTest {

    private static final long SEED = 42L;
    private static final double BASE = -50.0;
    private static final double AMPLITUDE = 170.0;

    @DisplayName("Constructor creates record with correct field values")
    @Test
    void constructor_createsRecord() {
        var mapper = new TectonicPlateMapper(SEED);
        var pc = new PlateContinentalness(mapper, BASE, AMPLITUDE);
        assertNotNull(pc);
        assertSame(mapper, pc.mapper());
        assertEquals(BASE, pc.continentalBase());
        assertEquals(AMPLITUDE, pc.continentalHeightAmplitude());
    }

    @DisplayName("Sample values are within expected range")
    @Test
    void sample_returnsValueInExpectedRange() {
        var mapper = new TectonicPlateMapper(SEED);
        var pc = new PlateContinentalness(mapper, BASE, AMPLITUDE);
        for (int x = -500; x <= 500; x += 50) {
            for (int z = -500; z <= 500; z += 50) {
                double value = pc.sample(x, 0, z);
                assertThat(value).isBetween(BASE, BASE + AMPLITUDE);
            }
        }
    }

    @DisplayName("Sample near ocean basin returns a low value")
    @Test
    void sample_oceanBasin_lowValue() {
        var mapper = new TectonicPlateMapper(SEED);
        var pc = new PlateContinentalness(mapper, BASE, AMPLITUDE);
        double value = pc.sample(0, 0, 0);
        assertThat(value).isGreaterThan(BASE).isLessThan(BASE + AMPLITUDE);
    }

    @DisplayName("Sample is deterministic for same coordinates")
    @Test
    void sample_deterministic() {
        var mapper = new TectonicPlateMapper(SEED);
        var pc = new PlateContinentalness(mapper, BASE, AMPLITUDE);
        double first = pc.sample(100, 0, 200);
        for (int i = 0; i < 50; i++) {
            assertEquals(first, pc.sample(100, 0, 200), 1e-9);
        }
    }

    @DisplayName("Y coordinate is ignored")
    @Test
    void sample_ignoresY() {
        var mapper = new TectonicPlateMapper(SEED);
        var pc = new PlateContinentalness(mapper, BASE, AMPLITUDE);
        double v1 = pc.sample(10, 50, 20);
        double v2 = pc.sample(10, -999, 20);
        assertEquals(v1, v2, 1e-9);
    }

    @DisplayName("Different seeds produce different values")
    @Test
    void sample_differentSeed_differentValues() {
        var mapper1 = new TectonicPlateMapper(SEED);
        var mapper2 = new TectonicPlateMapper(SEED + 9999);
        var pc1 = new PlateContinentalness(mapper1, BASE, AMPLITUDE);
        var pc2 = new PlateContinentalness(mapper2, BASE, AMPLITUDE);
        boolean anyDiff = false;
        for (int x = 0; x < 200; x += 10) {
            if (Math.abs(pc1.sample(x, 0, x) - pc2.sample(x, 0, x)) > 1e-9) {
                anyDiff = true;
                break;
            }
        }
        assertThat(anyDiff).isTrue();
    }
}
