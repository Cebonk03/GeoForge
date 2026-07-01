package com.geoforge.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.config.GeoForgeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("ColumnContext — per-column immutable context tests")
class ColumnContextTest {

    @Test
    @DisplayName("Record components are accessible")
    void recordComponents_accessible() {
        var ctx = new ColumnContext(50.0, 0.5, "plains", 1.0, 0.0, 1.0);
        assertEquals(50.0, ctx.targetHeight(), 1e-12);
        assertEquals(0.5, ctx.valleyFactor(), 1e-12);
        assertEquals("plains", ctx.biomeId());
        assertEquals(1.0, ctx.caveModifier(), 1e-12);
        assertEquals(0.0, ctx.heightOffset(), 1e-12);
        assertEquals(1.0, ctx.amplitudeMultiplier(), 1e-12);
    }

    @Test
    @DisplayName("Equals and hashCode work correctly")
    void equals_hashCode() {
        var a = new ColumnContext(50.0, 0.5, "plains", 1.0, 0.0, 1.0);
        var b = new ColumnContext(50.0, 0.5, "plains", 1.0, 0.0, 1.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Different values produce unequal records")
    void equals_differentValues() {
        var a = new ColumnContext(50.0, 0.5, "plains", 1.0, 0.0, 1.0);
        var b = new ColumnContext(60.0, 0.5, "plains", 1.0, 0.0, 1.0);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("compute() produces consistent results for the same engine, x, z")
    void compute_deterministic() {
        var config = GeoForgeConfig.defaults();
        var engine = new GeoForgeEngine(42L, config);
        var ctx1 = ColumnContext.compute(engine, 0, 0);
        var ctx2 = ColumnContext.compute(engine, 0, 0);
        assertEquals(ctx1.targetHeight(), ctx2.targetHeight(), 1e-9);
        assertEquals(ctx1.valleyFactor(), ctx2.valleyFactor(), 1e-9);
        assertEquals(ctx1.biomeId(), ctx2.biomeId());
    }

    @Test
    @DisplayName("compute() produces different context for different columns")
    void compute_differentColumns() {
        var config = GeoForgeConfig.defaults();
        var engine = new GeoForgeEngine(42L, config);
        var ctx1 = ColumnContext.compute(engine, 0, 0);
        var ctx2 = ColumnContext.compute(engine, 100, 100);
        // Different positions should produce different heights
        assertNotEquals(ctx1.targetHeight(), ctx2.targetHeight(), 1.0);
    }

    @Test
    @DisplayName("valleyFactor is in [0, 1] range")
    void valleyFactor_inRange() {
        var config = GeoForgeConfig.defaults();
        var engine = new GeoForgeEngine(42L, config);
        for (int x = 0; x < 50; x += 10) {
            for (int z = 0; z < 50; z += 10) {
                var ctx = ColumnContext.compute(engine, x, z);
                assertTrue(ctx.valleyFactor() >= 0.0,
                        "valleyFactor should be >= 0 at (" + x + "," + z + "): " + ctx.valleyFactor());
                assertTrue(ctx.valleyFactor() <= 1.0,
                        "valleyFactor should be <= 1 at (" + x + "," + z + "): " + ctx.valleyFactor());
            }
        }
    }

    @Test
    @DisplayName("compute() returns non-null biomeId")
    void compute_hasBiomeId() {
        var config = GeoForgeConfig.defaults();
        var engine = new GeoForgeEngine(42L, config);
        var ctx = ColumnContext.compute(engine, 0, 0);
        assertNotNull(ctx.biomeId());
        assertFalse(ctx.biomeId().isEmpty());
    }
}
