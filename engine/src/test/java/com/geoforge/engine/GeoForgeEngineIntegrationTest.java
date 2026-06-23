package com.geoforge.engine;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.config.GeoForgeConfig;
import org.junit.jupiter.api.Test;

/**
 * Integration test for the full GeoForge engine generation chain.
 *
 * <p>Exercises the complete pipeline: tectonic continentalness → fractal noise →
 * height sampling → biome assignment → erosion. These tests verify that the
 * components work together correctly, not just in isolation.
 */
class GeoForgeEngineIntegrationTest {

    private static final long SEED = 42L;
    private static final GeoForgeConfig CFG = GeoForgeConfig.defaults();

    @Test
    void fullChain_deterministicAcrossInvocations() {
        var engine1 = new GeoForgeEngine(SEED, CFG);
        var engine2 = new GeoForgeEngine(SEED, CFG);

        for (int x = -20; x <= 20; x += 7) {
            for (int z = -20; z <= 20; z += 7) {
                double h1 = engine1.getHeightAt(x, z);
                double h2 = engine2.getHeightAt(x, z);
                assertEquals(h1, h2, 1e-9,
                        "Height determinism failed at (" + x + "," + z + ")");

                String b1 = engine1.getBiomeId(x, (int) Math.round(h1), z);
                String b2 = engine2.getBiomeId(x, (int) Math.round(h2), z);
                assertEquals(b1, b2,
                        "Biome determinism failed at (" + x + "," + z + ")");
            }
        }
    }

    @Test
    void fullChain_heightsWithinConfigBounds() {
        var engine = new GeoForgeEngine(SEED, CFG);
        for (int x = -100; x <= 100; x += 10) {
            for (int z = -100; z <= 100; z += 10) {
                double h = engine.getHeightAt(x, z);
                assertTrue(h >= CFG.minHeight() && h <= CFG.maxHeight(),
                        "Height " + h + " at (" + x + "," + z + ") out of bounds");
            }
        }
    }

    @Test
    void fullChain_biomeIsValid() {
        var engine = new GeoForgeEngine(SEED, CFG);
        for (int x = -50; x <= 50; x += 25) {
            for (int z = -50; z <= 50; z += 25) {
                int y = (int) Math.round(engine.getHeightAt(x, z));
                String biome = engine.getBiomeId(x, y, z);
                assertNotNull(biome);
                assertTrue(engine.getAllBiomeIds().contains(biome),
                        "Unknown biome '" + biome + "' at (" + x + "," + y + "," + z + ")");
            }
        }
    }

    @Test
    void fullChain_biomeVariesByAltitude() {
        var engine = new GeoForgeEngine(SEED, CFG);
        boolean anyDiff = false;
        for (int x = -10; x <= 10 && !anyDiff; x += 5) {
            for (int z = -10; z <= 10 && !anyDiff; z += 5) {
                String surface = engine.getBiomeId(x, (int) Math.round(engine.getHeightAt(x, z)), z);
                String deep = engine.getBiomeId(x, CFG.minHeight(), z);
                if (!surface.equals(deep)) {
                    anyDiff = true;
                }
            }
        }
        assertTrue(anyDiff,
                "Biome should differ between surface and minHeight depth at some position");
    }

    @Test
    void fullChain_erosionModifiesHeightmap() {
        var engine = new GeoForgeEngine(SEED, CFG);
        int size = 16;
        float[] hm = new float[size * size];
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                hm[z * size + x] = (float) engine.getHeightAt(x, z);
            }
        }
        float sumBefore = 0;
        for (float v : hm) sumBefore += Math.abs(v);
        float before = sumBefore;

        engine.erode(hm, size, SEED);

        float sumAfter = 0;
        for (float v : hm) sumAfter += Math.abs(v);
        float after = sumAfter;
        assertNotEquals(before, after, 1e-6f,
                "Erosion should modify the heightmap");
    }

    @Test
    void fullChain_seaLevelAccessor() {
        var engine = new GeoForgeEngine(SEED, CFG);
        assertEquals(CFG.seaLevel(), engine.seaLevel());
    }
}
