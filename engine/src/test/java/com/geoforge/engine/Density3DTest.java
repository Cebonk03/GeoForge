package com.geoforge.engine;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.config.GeoForgeConfig;
import org.junit.jupiter.api.Test;

/**
 * Tests for 3D density field: {@link GeoForgeEngine#getDensity} and
 * {@link GeoForgeEngine#getSurfaceHeight}.
 *
 * <p>These tests verify that the engine correctly produces a 3D density field
 * where density > 0 means solid, density < 0 means air.
 */
class Density3DTest {

    private static final long SEED = 42L;
    private static final GeoForgeConfig CFG = GeoForgeConfig.defaults();

    @Test
    void getDensity_atSurface_isNearZero() {
        var engine = new GeoForgeEngine(SEED, CFG);
        int x = 0, z = 0;
        double surfaceHeight = engine.getSurfaceHeight(x, z);
        double density = engine.getDensity(x, (int) surfaceHeight, z);
        // At the surface, density should be approximately 0 (within ±1)
        assertTrue(
                density > -1.0 && density < 1.0,
                "Density at surface should be near 0, got " + density + " at y=" + surfaceHeight);
    }

    @Test
    void getDensity_aboveSurface_isNegative() {
        var engine = new GeoForgeEngine(SEED, CFG);
        int x = 100, z = 200;
        int surfaceY = engine.getSurfaceHeight(x, z);

        // Several blocks above surface: should be air (density < 0)
        double density = engine.getDensity(x, surfaceY + 5, z);
        assertTrue(density < 0, "Above surface should be negative (air), got " + density);
    }

    @Test
    void getDensity_belowSurface_isPositive() {
        var engine = new GeoForgeEngine(SEED, CFG);
        int x = -50, z = 75;
        int surfaceY = engine.getSurfaceHeight(x, z);

        // Several blocks below surface: should be solid (density > 0)
        double density = engine.getDensity(x, surfaceY - 5, z);
        assertTrue(density > 0, "Below surface should be positive (solid), got " + density);
    }

    @Test
    void getDensity_deepUnderground_isPositive() {
        var engine = new GeoForgeEngine(SEED, CFG);
        int x = 200, z = -150;
        // Deep underground at minHeight: should be solid (density > 0)
        double density = engine.getDensity(x, CFG.minHeight() + 1, z);
        assertTrue(density > 0, "Deep underground should be positive (solid), got " + density);
    }

    @Test
    void getDensity_highInSky_isNegative() {
        var engine = new GeoForgeEngine(SEED, CFG);
        int x = -200, z = 100;
        // High in the sky: should be air (density < 0)
        double density = engine.getDensity(x, CFG.maxHeight() - 1, z);
        assertTrue(density < 0, "High in sky should be negative (air), got " + density);
    }

    @Test
    void getDensity_deterministic() {
        var engine = new GeoForgeEngine(SEED, CFG);
        double first = engine.getDensity(42, 63, 73);
        for (int i = 0; i < 20; i++) {
            assertEquals(first, engine.getDensity(42, 63, 73), 1e-9,
                    "getDensity should be deterministic");
        }
    }

    @Test
    void getSurfaceHeight_matchesGetHeightAt_whenNoCaves() {
        // When cave amplitude is 0, surfaceHeight should equal the old height function
        var cfg = GeoForgeConfig.defaults().withCaveAmplitude(0.0);
        var engine = new GeoForgeEngine(SEED, cfg);

        for (int i = 0; i < 50; i++) {
            int x = i * 53;
            int z = i * 71;
            double expectedHeight = engine.getHeightAt(x, z);
            int surfaceHeight = engine.getSurfaceHeight(x, z);
            // Surface height should be within 1 block of the height function
            int expectedInt = (int) Math.round(expectedHeight);
            assertTrue(
                    Math.abs(surfaceHeight - expectedInt) <= 1,
                    "Surface height at (" + x + "," + z + ") = " + surfaceHeight
                            + " expected ~" + expectedHeight + " (rounded " + expectedInt + ")");
        }
    }

    @Test
    void getSurfaceHeight_allColumns_surfaceInBounds() {
        var engine = new GeoForgeEngine(SEED, CFG);
        for (int x = -100; x <= 100; x += 20) {
            for (int z = -100; z <= 100; z += 20) {
                int surfaceY = engine.getSurfaceHeight(x, z);
                assertTrue(
                        surfaceY >= CFG.minHeight() && surfaceY < CFG.maxHeight(),
                        "Surface at (" + x + "," + z + ") = " + surfaceY
                                + " outside world bounds [" + CFG.minHeight()
                                + ", " + CFG.maxHeight() + ")");
            }
        }
    }

    @Test
    void caveNoise_createsSomeAirPockets() {
        // With non-zero cave amplitude, some underground positions should be air
        var engine = new GeoForgeEngine(SEED, CFG);
        int caveColumns = 0;
        int totalColumns = 0;

        for (int x = -80; x <= 80; x += 8) {
            for (int z = -80; z <= 80; z += 8) {
                int surfaceY = engine.getSurfaceHeight(x, z);
                totalColumns++;
                boolean hasAir = false;
                for (int dy = -5; dy >= -25; dy -= 5) {
                    int checkY = Math.max(surfaceY + dy, CFG.minHeight() + 1);
                    if (engine.getDensity(x, checkY, z) < 0) {
                        hasAir = true;
                        break;
                    }
                }
                if (hasAir) caveColumns++;
            }
        }

        // With cave amplitude 8.0, at least some columns should have cave air pockets
        assertTrue(
                caveColumns > 0,
                "Expected at least one column with cave air pocket, got " + caveColumns
                        + " out of " + totalColumns);
    }
}
