package com.geoforge.engine;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.config.GeoForgeConfig;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GeoForgeEngineTest {

    private static final long SEED = 42L;
    private static final GeoForgeConfig CFG = GeoForgeConfig.defaults();

    @Test
    void getHeightAt_returnsReasonableValue() {
        var engine = new GeoForgeEngine(SEED, CFG);
        double height = engine.getHeightAt(0, 0);
        assertTrue(
                height >= CFG.minHeight() && height <= CFG.maxHeight(),
                "Height out of reasonable range: " + height);
    }

    @Test
    void getHeightAt_allCoordsInRange() {
        var engine = new GeoForgeEngine(SEED, CFG);
        for (int x = -100; x <= 100; x += 10) {
            for (int z = -100; z <= 100; z += 10) {
                double h = engine.getHeightAt(x, z);
                assertTrue(
                        h >= CFG.minHeight() && h <= CFG.maxHeight(),
                        "Height at (" + x + "," + z + ") = " + h + " out of range");
            }
        }
    }

    @Test
    void determinism_sameSeedProducesSameHeight() {
        var engine1 = new GeoForgeEngine(SEED, CFG);
        var engine2 = new GeoForgeEngine(SEED, CFG);
        for (int i = 0; i < 50; i++) {
            int x = i * 13;
            int z = i * 17;
            double h1 = engine1.getHeightAt(x, z);
            double h2 = engine2.getHeightAt(x, z);
            assertEquals(h1, h2, 1e-9, "Determinism failed at (" + x + "," + z + ")");
        }
    }

    @Test
    void getBiomeId_returnsValidBiome() {
        var engine = new GeoForgeEngine(SEED, CFG);
        String biome = engine.getBiomeId(0, 63, 0);
        assertNotNull(biome);
        assertTrue(engine.getAllBiomeIds().contains(biome), "Unknown biome: " + biome);
    }

    @Test
    void getBiomeId_variousPositions_allValid() {
        var engine = new GeoForgeEngine(SEED, CFG);
        for (int x = -50; x <= 50; x += 25) {
            for (int z = -50; z <= 50; z += 25) {
                String biome = engine.getBiomeId(x, 63, z);
                assertTrue(
                        engine.getAllBiomeIds().contains(biome),
                        "Invalid biome '" + biome + "' at (" + x + ",63," + z + ")");
            }
        }
    }

    @Test
    void getAllBiomeIds_returnsNonEmpty() {
        var engine = new GeoForgeEngine(SEED, CFG);
        Set<String> ids = engine.getAllBiomeIds();
        assertNotNull(ids);
        assertFalse(ids.isEmpty(), "Empty biome ID set");
    }

    @Test
    void getAllBiomeIds_isCached() {
        var engine = new GeoForgeEngine(SEED, CFG);
        assertSame(
                engine.getAllBiomeIds(),
                engine.getAllBiomeIds(),
                "getAllBiomeIds() should return the same cached set");
    }

    @Test
    void differentSeedDifferentHeight() {
        var engine1 = new GeoForgeEngine(SEED, CFG);
        var engine2 = new GeoForgeEngine(SEED + 9999, CFG);
        boolean anyDiff = false;
        for (int x = 0; x < 100; x += 10) {
            if (Math.abs(engine1.getHeightAt(x, x) - engine2.getHeightAt(x, x)) > 1e-6) {
                anyDiff = true;
                break;
            }
        }
        assertTrue(anyDiff, "Different seeds should produce different terrain");
    }
}
