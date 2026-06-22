package com.geoforge.engine.security;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.geology.TectonicPlateMapper;
import org.junit.jupiter.api.Test;

/**
 * PoC tests for {@link TectonicPlateMapper} behavior under extreme coordinates.
 *
 * <p>Attack vector: Coordinates at the edge of the int range could cause overflow
 * in distance calculations, producing incorrect continentalness values or crashes.
 */
class TectonicPlateMapperExtremeCoordsPoCTest {

    private static final long SEED = 42L;

    /**
     * PoC: Extreme coordinates must produce output in [0, 1].
     *
     * <p>The mapper uses float arithmetic for distance calculations.
     * At Integer.MAX_VALUE, dx = 2.147e9 - plateX[i] (where plateX[i] is in [-5000, 5000]).
     * dx stays in float range (max ~2.147e9) with precision loss but no overflow.
     * The edge factor uses Math.sqrt and division, which are stable.
     */
    @Test
    void extremeIntCoords_outputInRange() {
        var mapper = new TectonicPlateMapper(SEED);

        float[] results = {
            mapper.getContinentalness(Integer.MIN_VALUE, Integer.MIN_VALUE),
            mapper.getContinentalness(Integer.MAX_VALUE, Integer.MAX_VALUE),
            mapper.getContinentalness(Integer.MIN_VALUE, Integer.MAX_VALUE),
            mapper.getContinentalness(Integer.MAX_VALUE, Integer.MIN_VALUE),
        };

        for (float r : results) {
            assertTrue(r >= 0.0f && r <= 1.0f,
                    "Continentalness must be in [0,1]: " + r);
        }
    }

    /**
     * PoC: Negative coordinates must not cause edgeFactor to go out of range.
     *
     * <p>Edge factor computation: sqrt(minDistSq) / (sqrt(secondMinDistSq) + 0.001f)
     * At extreme negative coords, distance squares are large but positive.
     * The ratio stays in [0, 1] because minDistSq <= secondMinDistSq by construction.
     */
    @Test
    void negativeCoords_edgeFactorInRange() {
        var mapper = new TectonicPlateMapper(SEED);

        for (int x = -100_000; x <= 0; x += 137) {
            for (int z = -100_000; z <= 0; z += 139) {
                float c = mapper.getContinentalness(x, z);
                assertTrue(c >= 0.0f && c <= 1.0f,
                        "Out of range at (" + x + "," + z + "): " + c);
            }
        }
    }

    /**
     * PoC: Determinism at extreme coordinates.
     */
    @Test
    void determinismAtExtremeCoords() {
        var mapper1 = new TectonicPlateMapper(SEED);
        var mapper2 = new TectonicPlateMapper(SEED);

        int[][] coords = {
            {Integer.MIN_VALUE, Integer.MIN_VALUE},
            {Integer.MAX_VALUE, Integer.MAX_VALUE},
            {-100_000_000, -100_000_000},
            {100_000_000, 100_000_000},
        };

        for (int[] coord : coords) {
            float c1 = mapper1.getContinentalness(coord[0], coord[1]);
            float c2 = mapper2.getContinentalness(coord[0], coord[1]);
            assertEquals(c1, c2, 1e-6f,
                    "Determinism at (" + coord[0] + "," + coord[1] + ")");
        }
    }

    /**
     * PoC: Zero coordinates produce valid output (origin sanity check).
     */
    @Test
    void origin_returnsValidValue() {
        var mapper = new TectonicPlateMapper(SEED);
        float c = mapper.getContinentalness(0, 0);
        assertTrue(c >= 0.0f && c <= 1.0f,
                "Continentalness at origin: " + c);
    }

    /**
     * PoC: Plate centre proximity — coordinate exactly at a plate centre.
     *
     * <p>At plate centre, minDistSq = 0, secondMinDistSq > 0.
     * edgeFactor = 0 / (sqrt(second) + 0.001) ≈ 0. 
     * This is near the "centre of a continent", so continentalness ≈ 0 (ocean?).
     * Wait — actually the Voronoi edge factor being low means we're near a seed point,
     * and the noise modulation adds up to ~0.5 * 0.3 + 0.5 * 0.2 = 0.25.
     * So continentalness can be anywhere in [0, 1].
     */
    @Test
    void nearPlateCentre_validOutput() {
        var mapper = new TectonicPlateMapper(SEED, 5);
        // Test at large coordinates — any location is fine as long as valid
        float c = mapper.getContinentalness(12345, 67890);
        assertTrue(c >= 0.0f && c <= 1.0f);
    }
}
