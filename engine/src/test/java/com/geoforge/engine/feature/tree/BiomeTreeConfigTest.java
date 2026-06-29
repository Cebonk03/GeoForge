package com.geoforge.engine.feature.tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("BiomeTreeConfig record tests")
class BiomeTreeConfigTest {

    @DisplayName("defaults() returns sentinel values: -1.0 density, 0 heights, empty map")
    @Test
    void defaults_returnsSentinelValues() {
        var config = BiomeTreeConfig.defaults();

        assertThat(config.treeDensity()).isEqualTo(-1.0);
        assertThat(config.minTreeHeight()).isZero();
        assertThat(config.maxTreeHeight()).isZero();
        assertThat(config.variantModifiers()).isEmpty();
    }

    @DisplayName("Custom values accessible via accessors")
    @Test
    void customValuesAreAccessible() {
        var modifiers = Map.of("savanna_oak", 0.5, "jungle_big", 1.5);
        var config = new BiomeTreeConfig(0.75, 6, 12, modifiers);

        assertThat(config.treeDensity()).isEqualTo(0.75);
        assertThat(config.minTreeHeight()).isEqualTo(6);
        assertThat(config.maxTreeHeight()).isEqualTo(12);
        assertThat(config.variantModifiers())
                .containsEntry("savanna_oak", 0.5)
                .containsEntry("jungle_big", 1.5)
                .hasSize(2);
    }

    @DisplayName("Null variantModifiers is normalized to empty map")
    @Test
    void nullModifiersNormalizedToEmptyMap() {
        var config = new BiomeTreeConfig(0.5, 4, 8, null);

        assertThat(config.variantModifiers()).isEmpty();
    }

    @DisplayName("Validation rejects density outside [0, 1] range")
    @Test
    void validation_rejectsInvalidDensity() {
        assertThatThrownBy(() -> new BiomeTreeConfig(1.5, 4, 8, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("treeDensity");

        assertThatThrownBy(() -> new BiomeTreeConfig(-0.5, 4, 8, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("treeDensity");
    }

    @DisplayName("Validation accepts sentinel density -1.0")
    @Test
    void validation_acceptsSentinelDensity() {
        var config = new BiomeTreeConfig(-1.0, 4, 8, Map.of());

        assertThat(config.treeDensity()).isEqualTo(-1.0);
    }

    @DisplayName("Validation rejects negative minTreeHeight")
    @Test
    void validation_rejectsNegativeMinHeight() {
        assertThatThrownBy(() -> new BiomeTreeConfig(0.5, -1, 8, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minTreeHeight");
    }

    @DisplayName("Validation rejects negative maxTreeHeight")
    @Test
    void validation_rejectsNegativeMaxHeight() {
        assertThatThrownBy(() -> new BiomeTreeConfig(0.5, 4, -1, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxTreeHeight");
    }

    @DisplayName("Validation rejects minTreeHeight > maxTreeHeight when both positive")
    @Test
    void validation_rejectsMinGreaterThanMax() {
        assertThatThrownBy(() -> new BiomeTreeConfig(0.5, 10, 5, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minTreeHeight")
                .hasMessageContaining("maxTreeHeight");
    }

    @DisplayName("Record equals/hashCode works for equal and non-equal instances")
    @Test
    void equalsAndHashCode() {
        var config1 = new BiomeTreeConfig(0.5, 4, 8, Map.of("oak", 1.0));
        var config2 = new BiomeTreeConfig(0.5, 4, 8, Map.of("oak", 1.0));
        var config3 = new BiomeTreeConfig(0.6, 4, 8, Map.of("oak", 1.0));

        assertThat(config1).isEqualTo(config2);
        assertThat(config1).hasSameHashCodeAs(config2);
        assertThat(config1).isNotEqualTo(config3);
        assertThat(config1.hashCode()).isNotEqualTo(config3.hashCode());
    }
}
