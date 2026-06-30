package com.geoforge.engine.config.biome;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("GeoForgeBiomeDefaults — hardcoded biome definition factory")
class GeoForgeBiomeDefaultsTest {

    @Test
    @DisplayName("createDefaults() returns all expected biome entries")
    void createDefaults_returnsAllBiomes() {
        var defaults = GeoForgeBiomeDefaults.createDefaults();
        // 65+ biome definitions for all vanilla MC biomes
        assertTrue(defaults.size() >= 60);
        assertNotNull(defaults.get("plains"));
        assertNotNull(defaults.get("desert"));
        assertNotNull(defaults.get("ocean"));
        assertNotNull(defaults.get("forest"));
        assertNotNull(defaults.get("jungle"));
        assertNotNull(defaults.get("taiga"));
        assertNotNull(defaults.get("the_end"));
    }

    @Test
    @DisplayName("createDefaults() returns unmodifiable map")
    void createDefaults_isUnmodifiable() {
        var defaults = GeoForgeBiomeDefaults.createDefaults();
        assertThrows(UnsupportedOperationException.class, () -> defaults.put("x", null));
    }

    @Test
    @DisplayName("custom() produces a biome definition with overridden fields")
    void custom_producesOverride() {
        var def = GeoForgeBiomeDefaults.createDefaults().get("desert");
        assertNotNull(def);
        assertEquals("desert", def.id());
        assertEquals("sand", def.surfaceBlock());
        assertEquals("sandstone", def.subSurfaceBlock());
        assertEquals(1.5, def.caveAmplitudeModifier());
    }

    @Test
    @DisplayName("neutral() produces a biome definition with only ID set")
    void neutral_preservesDefaultsExceptId() {
        var def = GeoForgeBiomeDefaults.createDefaults().get("the_void");
        assertNotNull(def);
        assertEquals("the_void", def.id());
        // All other fields should be at their defaults
        assertEquals(0.0, def.heightOffset());
        assertEquals(1.0, def.amplitudeMultiplier());
    }

    @Test
    @DisplayName("desert has surfaceDepth=5")
    void desert_hasDeepSurface() {
        var def = GeoForgeBiomeDefaults.createDefaults().get("desert");
        assertNotNull(def);
        assertEquals(5, def.surfaceDepth());
    }

    @Test
    @DisplayName("peaks and mountains have surfaceDepth=1")
    void peaks_haveShallowSurface() {
        assertEquals(1, GeoForgeBiomeDefaults.createDefaults().get("jagged_peaks").surfaceDepth());
        assertEquals(1, GeoForgeBiomeDefaults.createDefaults().get("frozen_peaks").surfaceDepth());
        assertEquals(1, GeoForgeBiomeDefaults.createDefaults().get("stony_peaks").surfaceDepth());
        assertEquals(1, GeoForgeBiomeDefaults.createDefaults().get("snowy_slopes").surfaceDepth());
    }

    @Test
    @DisplayName("beach and snowy_beach have surfaceDepth=1")
    void beaches_haveShallowSurface() {
        assertEquals(1, GeoForgeBiomeDefaults.createDefaults().get("beach").surfaceDepth());
        assertEquals(1, GeoForgeBiomeDefaults.createDefaults().get("snowy_beach").surfaceDepth());
    }

    @Test
    @DisplayName("oceans have surfaceDepth 1-2 depending on depth tier")
    void oceans_haveSurfaceDepth() {
        assertEquals(2, GeoForgeBiomeDefaults.createDefaults().get("ocean").surfaceDepth());
        assertEquals(1, GeoForgeBiomeDefaults.createDefaults().get("deep_ocean").surfaceDepth());
        assertEquals(2, GeoForgeBiomeDefaults.createDefaults().get("warm_ocean").surfaceDepth());
        assertEquals(2, GeoForgeBiomeDefaults.createDefaults().get("cold_ocean").surfaceDepth());
        assertEquals(2, GeoForgeBiomeDefaults.createDefaults().get("frozen_ocean").surfaceDepth());
    }

    @Test
    @DisplayName("forest biome has default surfaceDepth=3")
    void forest_hasDefaultSurfaceDepth() {
        assertEquals(3, GeoForgeBiomeDefaults.createDefaults().get("forest").surfaceDepth());
    }

    @Test
    @DisplayName("warm_ocean has seagrass and coral vegetation")
    void warmOcean_hasCoral() {
        var veg = GeoForgeBiomeDefaults.createDefaults().get("warm_ocean").vegetationTypes();
        assertTrue(veg.contains("seagrass"));
        assertTrue(veg.contains("coral"));
    }

    @Test
    @DisplayName("cold_ocean uses gravel surface")
    void coldOcean_hasGravel() {
        assertEquals("gravel", GeoForgeBiomeDefaults.createDefaults().get("cold_ocean").surfaceBlock());
    }

    @Test
    @DisplayName("defaults biomes have non-empty biome IDs")
    void allBiomes_haveNonEmptyIds() {
        for (var entry : GeoForgeBiomeDefaults.createDefaults().entrySet()) {
            assertFalse(entry.getValue().id().isEmpty(),
                    "Biome ID should not be empty for key: " + entry.getKey());
        }
    }

    @Test
    @DisplayName("full() builder produces correct merge with all fields")
    void full_builderSetsAllFields() {
        // Access full() indirectly via warm_ocean which uses full()
        var def = GeoForgeBiomeDefaults.createDefaults().get("warm_ocean");
        assertNotNull(def);
        assertEquals("warm_ocean", def.id());
        assertEquals(0.0, def.heightOffset());
        assertEquals(1.0, def.amplitudeMultiplier());
        assertEquals("sand", def.surfaceBlock());
        assertEquals("sand", def.subSurfaceBlock());
        assertEquals(0.8, def.surfaceHardness());
        assertEquals(2, def.surfaceDepth());
    }
}
