package com.geoforge.engine.config.biome;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("BiomeDefinition — immutable biome parameter record")
class BiomeDefinitionTest {

    @Test
    @DisplayName("defaults() produces neutral values")
    void defaults_hasNeutralValues() {
        var def = BiomeDefinition.defaults();
        assertEquals(0.0, def.heightOffset());
        assertEquals(1.0, def.amplitudeMultiplier());
        assertEquals("", def.surfaceBlock());
        assertEquals("", def.subSurfaceBlock());
        assertEquals(0.5, def.surfaceHardness());
        assertEquals(1.0, def.caveAmplitudeModifier());
        assertEquals(-1.0, def.treeDensity());
        assertEquals(0, def.minTreeHeight());
        assertEquals(0, def.maxTreeHeight());
        assertTrue(def.vegetationTypes().isEmpty());
        assertEquals(0.3, def.vegetationDensity());
        assertFalse(def.allowFloatingPlants());
        assertTrue(def.treeVariantModifiers().isEmpty());
    }

    @Test
    @DisplayName("merge() with null returns this")
    void merge_null_returnsThis() {
        var def = BiomeDefinition.defaults();
        assertSame(def, def.merge(null));
    }

    @Test
    @DisplayName("merge() applies non-default override fields")
    void merge_appliesOverrideFields() {
        var base = BiomeDefinition.defaults();
        var override = new BiomeDefinition(
                "test_biome",   // id
                5.0,            // heightOffset
                1.5,            // amplitudeMultiplier
                "stone",        // surfaceBlock
                "bedrock",       // subSurfaceBlock
                0.8,            // surfaceHardness
                0.5,            // caveAmplitudeModifier
                "OAK",          // treeType
                0.2,            // treeDensity
                4,              // minTreeHeight
                8,              // maxTreeHeight
                5,              // surfaceDepth (override from default 3)
                Map.of("tall", 2.0),  // treeVariantModifiers
                List.of("grass"),      // vegetationTypes
                0.5,            // vegetationDensity
                true,           // allowFloatingPlants
                -0.2, 0.4,      // tempMin, tempMax
                0.2, 0.7,       // humidityMin, humidityMax
                0.5, 0.7,       // continentalnessMin, continentalnessMax
                10);            // priority

        var merged = base.merge(override);

        assertEquals("test_biome", merged.id());
        assertEquals(5.0, merged.heightOffset());
        assertEquals(1.5, merged.amplitudeMultiplier());
        assertEquals("stone", merged.surfaceBlock());
        assertEquals("bedrock", merged.subSurfaceBlock());
        assertEquals(0.8, merged.surfaceHardness());
        assertEquals(0.5, merged.caveAmplitudeModifier());
        assertEquals("OAK", merged.treeType());
        assertEquals(0.2, merged.treeDensity());
        assertEquals(4, merged.minTreeHeight());
        assertEquals(8, merged.maxTreeHeight());
        assertEquals(5, merged.surfaceDepth());
        assertEquals(1, merged.treeVariantModifiers().size());
        assertTrue(merged.treeVariantModifiers().containsKey("tall"));
        assertEquals(1, merged.vegetationTypes().size());
        assertEquals("grass", merged.vegetationTypes().get(0));
        assertEquals(0.5, merged.vegetationDensity());
        assertTrue(merged.allowFloatingPlants());
        assertEquals(-0.2, merged.tempMin());
        assertEquals(0.4, merged.tempMax());
        assertEquals(0.2, merged.humidityMin());
        assertEquals(0.7, merged.humidityMax());
        assertEquals(0.5, merged.continentalnessMin());
        assertEquals(0.7, merged.continentalnessMax());
        assertEquals(10, merged.priority());
    }

    @Test
    @DisplayName("merge() preserves default fields when override has sentinels")
    void merge_preservesDefaults_withSentinels() {
        var base = BiomeDefinition.defaults();
        var override = new BiomeDefinition(
                "override", 0.0, 1.0, "", "", 0.5, 1.0, "", -1.0, 0, 0, 0,
                Map.of(), List.of(), 0.3, false,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0);
        var merged = base.merge(override);

        // Should be "override" from the id (id merges differently)
        assertEquals("override", merged.id());
        // All other fields should keep defaults from base
        assertEquals(0.0, merged.heightOffset());
        assertEquals(1.0, merged.amplitudeMultiplier());
        assertEquals("", merged.surfaceBlock());
        assertEquals(0.5, merged.surfaceHardness());
    }

    @Test
    @DisplayName("merge() preserves base id when override id is empty")
    void merge_preservesBaseId_whenOverrideEmpty() {
        var base = new BiomeDefinition(
                "base", 0.0, 1.0, "", "", 0.5, 1.0, "", -1.0, 0, 0, 3,
                Map.of(), List.of(), 0.3, false,
                -1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 5);
        var override = BiomeDefinition.defaults();

        var merged = base.merge(override);
        assertEquals("base", merged.id());
        assertEquals(5, merged.priority());
    }
}
