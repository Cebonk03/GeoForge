package com.geoforge.engine.feature.tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TreeVariant record tests")
class TreeVariantTest {

    private static final TrunkProfile trunk =
            (s, x, y, z, h, m, r) -> new TrunkResult(x, y + h, z, h);
    private static final CanopyProfile canopy = (s, tx, ty, tz, th, m, d, r) -> {};

    @DisplayName("Valid variant constructed successfully")
    @Test
    void validVariant() {
        var variant = new TreeVariant("tall_oak", trunk, canopy, 4, 10, 0.8, 1.0);

        assertThat(variant.name()).isEqualTo("tall_oak");
        assertThat(variant.trunk()).isSameAs(trunk);
        assertThat(variant.canopy()).isSameAs(canopy);
        assertThat(variant.minHeight()).isEqualTo(4);
        assertThat(variant.maxHeight()).isEqualTo(10);
        assertThat(variant.leafDensity()).isEqualTo(0.8);
        assertThat(variant.weight()).isEqualTo(1.0);
    }

    @DisplayName("Null name throws NullPointerException")
    @Test
    void nullName() {
        assertThatThrownBy(() -> new TreeVariant(null, trunk, canopy, 4, 8, 0.8, 1.0))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("name must not be null");
    }

    @DisplayName("Blank name throws IllegalArgumentException")
    @Test
    void blankName() {
        assertThatThrownBy(() -> new TreeVariant("  ", trunk, canopy, 4, 8, 0.8, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be blank");
    }

    @DisplayName("Null trunk throws NullPointerException")
    @Test
    void nullTrunk() {
        assertThatThrownBy(() -> new TreeVariant("oak", null, canopy, 4, 8, 0.8, 1.0))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("trunk must not be null");
    }

    @DisplayName("Null canopy throws NullPointerException")
    @Test
    void nullCanopy() {
        assertThatThrownBy(() -> new TreeVariant("oak", trunk, null, 4, 8, 0.8, 1.0))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("canopy must not be null");
    }

    @DisplayName("minHeight < 3 throws IllegalArgumentException")
    @Test
    void minHeightTooLow() {
        assertThatThrownBy(() -> new TreeVariant("oak", trunk, canopy, 2, 8, 0.8, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minHeight must be >= 3, got 2");

        assertThatThrownBy(() -> new TreeVariant("oak", trunk, canopy, 1, 8, 0.8, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minHeight");
    }

    @DisplayName("maxHeight < minHeight throws IllegalArgumentException")
    @Test
    void maxHeightLessThanMinHeight() {
        assertThatThrownBy(() -> new TreeVariant("oak", trunk, canopy, 6, 4, 0.8, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxHeight")
                .hasMessageContaining("minHeight");
    }

    @DisplayName("leafDensity > 1 throws IllegalArgumentException")
    @Test
    void leafDensityTooHigh() {
        assertThatThrownBy(() -> new TreeVariant("oak", trunk, canopy, 4, 8, 1.1, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("leafDensity");
    }

    @DisplayName("leafDensity < 0 throws IllegalArgumentException")
    @Test
    void leafDensityTooLow() {
        assertThatThrownBy(() -> new TreeVariant("oak", trunk, canopy, 4, 8, -0.1, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("leafDensity");
    }

    @DisplayName("weight < 0 throws IllegalArgumentException")
    @Test
    void weightTooLow() {
        assertThatThrownBy(() -> new TreeVariant("oak", trunk, canopy, 4, 8, 0.8, -1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("weight");
    }

    @DisplayName("withWeight creates new variant with different weight")
    @Test
    void withWeight() {
        var original = new TreeVariant("oak", trunk, canopy, 4, 8, 0.8, 1.0);
        var modified = original.withWeight(2.5);

        assertThat(modified.name()).isEqualTo("oak");
        assertThat(modified.trunk()).isSameAs(trunk);
        assertThat(modified.canopy()).isSameAs(canopy);
        assertThat(modified.minHeight()).isEqualTo(4);
        assertThat(modified.maxHeight()).isEqualTo(8);
        assertThat(modified.leafDensity()).isEqualTo(0.8);
        assertThat(modified.weight()).isEqualTo(2.5);
        assertThat(original.weight()).isEqualTo(1.0);
    }

    @DisplayName("Record equals/hashCode works for equal and non-equal instances")
    @Test
    void equalsAndHashCode() {
        var v1 = new TreeVariant("oak", trunk, canopy, 4, 8, 0.8, 1.0);
        var v2 = new TreeVariant("oak", trunk, canopy, 4, 8, 0.8, 1.0);
        var v3 = new TreeVariant("birch", trunk, canopy, 4, 8, 0.8, 1.0);

        assertThat(v1).isEqualTo(v2);
        assertThat(v1).hasSameHashCodeAs(v2);
        assertThat(v1).isNotEqualTo(v3);
        assertThat(v1.hashCode()).isNotEqualTo(v3.hashCode());
    }

    @DisplayName("Boundary value: minHeight == 3 is accepted")
    @Test
    void minHeightAtBoundary() {
        var variant = new TreeVariant("short", trunk, canopy, 3, 5, 0.5, 0.0);
        assertThat(variant.minHeight()).isEqualTo(3);
    }

    @DisplayName("Boundary value: leafDensity == 0.0 and 1.0 are accepted")
    @Test
    void leafDensityAtBoundaries() {
        var sparse = new TreeVariant("sparse", trunk, canopy, 4, 8, 0.0, 1.0);
        var dense = new TreeVariant("dense", trunk, canopy, 4, 8, 1.0, 1.0);

        assertThat(sparse.leafDensity()).isZero();
        assertThat(dense.leafDensity()).isEqualTo(1.0);
    }

    @DisplayName("Boundary value: weight == 0.0 is accepted")
    @Test
    void weightAtBoundary() {
        var variant = new TreeVariant("rare", trunk, canopy, 4, 8, 0.5, 0.0);
        assertThat(variant.weight()).isZero();
    }
}
