package com.geoforge.engine.biome;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("BiomeTerrainConfig tests")
class BiomeTerrainConfigTest {

    @DisplayName("defaults() returns non-null config")
    @Test
    void defaults_returnsNonNull() {
        assertNotNull(BiomeTerrainConfig.defaults());
    }

    @DisplayName("defaults() has expected default heightOffset")
    @Test
    void defaults_heightOffset() {
        assertThat(BiomeTerrainConfig.defaults().heightOffset()).isEqualTo(0.0);
    }

    @DisplayName("defaults() has expected default amplitudeMultiplier")
    @Test
    void defaults_amplitudeMultiplier() {
        assertThat(BiomeTerrainConfig.defaults().amplitudeMultiplier()).isEqualTo(1.0);
    }

    @DisplayName("defaults() has expected default caveAmplitudeModifier")
    @Test
    void defaults_caveAmplitudeModifier() {
        assertThat(BiomeTerrainConfig.defaults().caveAmplitudeModifier()).isEqualTo(1.0);
    }

    @DisplayName("defaults() has empty treeType")
    @Test
    void defaults_treeType() {
        assertThat(BiomeTerrainConfig.defaults().treeType()).isEmpty();
    }

    @DisplayName("defaults() has empty surfaceBlock")
    @Test
    void defaults_surfaceBlock() {
        assertThat(BiomeTerrainConfig.defaults().surfaceBlock()).isEmpty();
    }

    @DisplayName("defaults() has empty subSurfaceBlock")
    @Test
    void defaults_subSurfaceBlock() {
        assertThat(BiomeTerrainConfig.defaults().subSurfaceBlock()).isEmpty();
    }

    @DisplayName("defaults() has allowFloatingPlants = false")
    @Test
    void defaults_allowFloatingPlants() {
        assertThat(BiomeTerrainConfig.defaults().allowFloatingPlants()).isFalse();
    }

    @DisplayName("defaults() has expected surfaceHardness")
    @Test
    void defaults_surfaceHardness() {
        assertThat(BiomeTerrainConfig.defaults().surfaceHardness()).isEqualTo(0.5);
    }

    @DisplayName("Records with same values are equal")
    @Test
    void equality_sameValues() {
        var a = BiomeTerrainConfig.defaults();
        var b = BiomeTerrainConfig.defaults();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @DisplayName("Records with different values are not equal")
    @Test
    void equality_differentValues() {
        var a = BiomeTerrainConfig.defaults();
        var b = new BiomeTerrainConfig(5.0, 1.0, 1.0, "", "", "", false, 0.5);
        assertNotEquals(a, b);
    }

    @DisplayName("Custom config fields are accessible via record accessors")
    @Test
    void customConfig_accessors() {
        var cfg = new BiomeTerrainConfig(3.0, 0.8, 0.5, "oak", "grass_block", "dirt", true, 0.3);
        assertThat(cfg.heightOffset()).isEqualTo(3.0);
        assertThat(cfg.amplitudeMultiplier()).isEqualTo(0.8);
        assertThat(cfg.caveAmplitudeModifier()).isEqualTo(0.5);
        assertThat(cfg.treeType()).isEqualTo("oak");
        assertThat(cfg.surfaceBlock()).isEqualTo("grass_block");
        assertThat(cfg.subSurfaceBlock()).isEqualTo("dirt");
        assertThat(cfg.allowFloatingPlants()).isTrue();
        assertThat(cfg.surfaceHardness()).isEqualTo(0.3);
    }

    @DisplayName("toString() contains field values")
    @Test
    void toString_containsFieldValues() {
        var cfg = BiomeTerrainConfig.defaults();
        String str = cfg.toString();
        assertThat(str).contains("heightOffset=0.0");
        assertThat(str).contains("amplitudeMultiplier=1.0");
    }
}
