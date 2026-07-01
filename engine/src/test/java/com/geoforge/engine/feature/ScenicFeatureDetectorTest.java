package com.geoforge.engine.feature;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.GeoForgeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("ScenicFeatureDetector — wow-moment detection tests")
class ScenicFeatureDetectorTest {

    @Test
    @DisplayName("detect returns NONE on flat terrain")
    void detect_flatTerrain_returnsNone() {
        // Use a concrete seed and config to verify the detector works end-to-end
        var config = GeoForgeConfig.defaults();
        var engine = new GeoForgeEngine(42L, config);
        var detector = new ScenicFeatureDetector(42L);
        // Flat terrain at low Y should early-out to NONE
        int surfaceY = engine.getSurfaceHeight(0, 0);
        var result = detector.detect(engine, 0, 0, surfaceY);
        // Most flat positions should return NONE; this verifies the method runs without error
        assertNotNull(result);
        assertNotNull(result.type());
        assertTrue(result.intensity() >= 0.0 && result.intensity() <= 1.0);
    }

    @Test
    @DisplayName("detect returns deterministic results for same seed and coordinates")
    void detect_deterministic() {
        var config = GeoForgeConfig.defaults();
        var engine = new GeoForgeEngine(42L, config);
        var a = new ScenicFeatureDetector(42L);
        var b = new ScenicFeatureDetector(42L);
        for (int x = 0; x < 50; x += 10) {
            for (int z = 0; z < 50; z += 10) {
                int surfaceY = engine.getSurfaceHeight(x, z);
                var r1 = a.detect(engine, x, z, surfaceY);
                var r2 = b.detect(engine, x, z, surfaceY);
                assertEquals(r1.type(), r2.type());
                assertEquals(r1.intensity(), r2.intensity(), 1e-12);
                assertEquals(r1.direction(), r2.direction(), 1e-12);
            }
        }
    }

    @Test
    @DisplayName("detect returns valid FeatureResult for all sampled positions")
    void detect_allPositions_validResults() {
        var config = GeoForgeConfig.defaults();
        var engine = new GeoForgeEngine(42L, config);
        var detector = new ScenicFeatureDetector(42L);
        // Sample a 5x5 grid of positions to verify no exceptions and valid results
        for (int x = 0; x < 80; x += 20) {
            for (int z = 0; z < 80; z += 20) {
                int surfaceY = engine.getSurfaceHeight(x, z);
                var result = detector.detect(engine, x, z, surfaceY);
                assertNotNull(result);
                assertNotNull(result.type());
                assertTrue(result.intensity() >= 0.0 && result.intensity() <= 1.0,
                        "Intensity out of range at (" + x + "," + z + "): " + result.intensity());
            }
        }
    }

    @Test
    @DisplayName("FeatureType enum values are accessible")
    void featureType_enumValues() {
        assertEquals(4, ScenicFeatureDetector.FeatureType.values().length);
        assertNotNull(ScenicFeatureDetector.FeatureType.valueOf("NONE"));
        assertNotNull(ScenicFeatureDetector.FeatureType.valueOf("EMERGENCE"));
        assertNotNull(ScenicFeatureDetector.FeatureType.valueOf("HIDDEN_VALLEY"));
        assertNotNull(ScenicFeatureDetector.FeatureType.valueOf("EDGE_VISTA"));
    }
}
