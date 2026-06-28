package com.geoforge.engine.geology;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Tectonic plate mapper tests")
class TectonicPlateMapperTest {

    private static final long SEED = 42L;

    @DisplayName("Continentalness at origin is in [0, 1] range")
    @Test
    void getContinentalness_atOrigin_returnsValueInRange() {
        var mapper = new TectonicPlateMapper(SEED);
        float value = mapper.getContinentalness(0, 0);
        assertThat(value).isBetween(0.0f, 1.0f);
    }

    @DisplayName("Continentalness is deterministic")
    @Test
    void getContinentalness_deterministic() {
        var mapper = new TectonicPlateMapper(SEED);
        float first = mapper.getContinentalness(100, 200);
        for (int i = 0; i < 50; i++) {
            assertEquals(first, mapper.getContinentalness(100, 200), 1e-6f);
        }
    }

    @DisplayName("All continentalness values are in [0, 1] range")
    @Test
    void getContinentalness_allValuesInRange() {
        var mapper = new TectonicPlateMapper(SEED);
        for (int x = -500; x <= 500; x += 50) {
            for (int z = -500; z <= 500; z += 50) {
                float v = mapper.getContinentalness(x, z);
                assertThat(v).isBetween(0.0f, 1.0f);
            }
        }
    }

    @DisplayName("Constructor with custom plate count works")
    @Test
    void constructor_withCustomPlateCount() {
        var mapper = new TectonicPlateMapper(SEED, 24);
        float v = mapper.getContinentalness(0, 0);
        assertThat(v).isBetween(0.0f, 1.0f);
    }

    @DisplayName("Different seeds produce different continentalness values")
    @Test
    void differentSeedDifferentValues() {
        var mapper1 = new TectonicPlateMapper(SEED);
        var mapper2 = new TectonicPlateMapper(SEED + 1000);
        boolean anyDiff = false;
        for (int x = 0; x < 200; x += 10) {
            if (Math.abs(mapper1.getContinentalness(x, x) - mapper2.getContinentalness(x, x)) > 1e-6f) {
                anyDiff = true;
                break;
            }
        }
        assertThat(anyDiff).isTrue();
    }
}
