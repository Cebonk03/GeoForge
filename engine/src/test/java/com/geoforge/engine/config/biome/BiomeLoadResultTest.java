package com.geoforge.engine.config.biome;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("BiomeLoadResult — config loading result container")
class BiomeLoadResultTest {

    @Test
    @DisplayName("empty() returns result with no errors")
    void empty_hasNoErrors() {
        var result = BiomeLoadResult.empty();
        assertTrue(result.biomes().isEmpty());
        assertTrue(result.errors().isEmpty());
        assertTrue(result.warnings().isEmpty());
        assertFalse(result.hasErrors());
    }

    @Test
    @DisplayName("hasErrors() returns true when errors are present")
    void hasErrors_true_whenErrorsPresent() {
        var result = new BiomeLoadResult(Map.of(), List.of("file not found"), List.of());
        assertTrue(result.hasErrors());
    }

    @Test
    @DisplayName("hasErrors() returns false for clean result")
    void hasErrors_false_forCleanResult() {
        var result = new BiomeLoadResult(Map.of(), List.of(), List.of("deprecated field"));
        assertFalse(result.hasErrors());
    }

    @Test
    @DisplayName("merge() combines biomes from both results")
    void merge_combinesBiomes() {
        var d1 = BiomeDefinition.defaults();
        var d2 = BiomeDefinition.defaults();
        var a = new BiomeLoadResult(Map.of("plains", d1), List.of(), List.of());
        var b = new BiomeLoadResult(Map.of("desert", d2), List.of(), List.of());
        var merged = BiomeLoadResult.merge(a, b);
        assertEquals(2, merged.biomes().size());
        assertTrue(merged.biomes().containsKey("plains"));
        assertTrue(merged.biomes().containsKey("desert"));
    }

    @Test
    @DisplayName("merge() combines errors and warnings")
    void merge_combinesErrorsAndWarnings() {
        var a = new BiomeLoadResult(Map.of(), List.of("error a"), List.of("warning a"));
        var b = new BiomeLoadResult(Map.of(), List.of("error b"), List.of("warning b"));
        var merged = BiomeLoadResult.merge(a, b);
        assertEquals(2, merged.errors().size());
        assertEquals(2, merged.warnings().size());
    }

    @Test
    @DisplayName("merge() second biome wins on ID conflict")
    void merge_secondWins_onIdConflict() {
        var d1 = BiomeDefinition.defaults();
        var d2 = new BiomeDefinition(
                "plains", 5.0, 1.0, "", "", 0.5, 1.0, "", -1.0, 0, 0,
                Map.of(), List.of(), 0.3, false,
                -1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0);
        var a = new BiomeLoadResult(Map.of("plains", d1), List.of(), List.of());
        var b = new BiomeLoadResult(Map.of("plains", d2), List.of(), List.of());
        var merged = BiomeLoadResult.merge(a, b);
        assertEquals(1, merged.biomes().size());
        assertEquals(5.0, merged.biomes().get("plains").heightOffset());
    }

    @Test
    @DisplayName("merge() handles null gracefully")
    void merge_handlesNull() {
        var a = new BiomeLoadResult(Map.of("plains", BiomeDefinition.defaults()), List.of(), List.of());
        var merged = BiomeLoadResult.merge(a, null);
        assertEquals(1, merged.biomes().size());
        assertFalse(merged.hasErrors());
    }
}
