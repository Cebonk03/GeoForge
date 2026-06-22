package com.geoforge.engine.geology;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TectonicPlateMapperTest {

    private static final long SEED = 42L;

    @Test
    void getContinentalness_atOrigin_returnsValueInRange() {
        var mapper = new TectonicPlateMapper(SEED);
        float value = mapper.getContinentalness(0, 0);
        assertTrue(
                value >= 0.0f && value <= 1.0f,
                "Continentalness out of [0,1] range: " + value);
    }

    @Test
    void getContinentalness_deterministic() {
        var mapper = new TectonicPlateMapper(SEED);
        float first = mapper.getContinentalness(100, 200);
        for (int i = 0; i < 50; i++) {
            assertEquals(first, mapper.getContinentalness(100, 200), 1e-6f);
        }
    }

    @Test
    void getContinentalness_allValuesInRange() {
        var mapper = new TectonicPlateMapper(SEED);
        for (int x = -500; x <= 500; x += 50) {
            for (int z = -500; z <= 500; z += 50) {
                float v = mapper.getContinentalness(x, z);
                assertTrue(
                        v >= 0.0f && v <= 1.0f,
                        "Value at (" + x + "," + z + ") = " + v + " out of range");
            }
        }
    }

    @Test
    void constructor_withCustomPlateCount() {
        var mapper = new TectonicPlateMapper(SEED, 24);
        float v = mapper.getContinentalness(0, 0);
        assertTrue(v >= 0.0f && v <= 1.0f);
    }

    @Test
    void differentSeedDifferentValues() {
        var mapper1 = new TectonicPlateMapper(SEED);
        var mapper2 = new TectonicPlateMapper(SEED + 1000);
        boolean anyDiff = false;
        for (int x = 0; x < 200; x += 10) {
            if (Math.abs(
                            mapper1.getContinentalness(x, x)
                                    - mapper2.getContinentalness(x, x))
                    > 1e-6f) {
                anyDiff = true;
                break;
            }
        }
        assertTrue(anyDiff, "Different seeds should produce different continentalness");
    }
}
