package com.geoforge.engine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ConfigMigrator}.
 */
@Tag("unit")
@DisplayName("ConfigMigrator tests")
class ConfigMigratorTest {

    /**
     * Builds a synthetic v1 config (configVersion = 1) with non-default values for
     * the original 22 fields and intentionally different values for the 27 new fields
     * so we can verify migration overwrites them with defaults.
     */
    private static GeoForgeConfig v1Config() {
        return GeoForgeConfig.builder()
.minHeight(-60)
.maxHeight(200)
.seaLevel(70)
.continentalBase(40.0)
.continentalHeightAmplitude(100.0)
.temperatureFrequency(0.002)
.temperatureYFrequency(0.01)
.humidityFrequency(0.002)
.caveFrequency(0.04)
.caveAmplitude(10.0)
.caveOctaves(3)
.caveLacunarity(3.0)
.cavePersistence(0.6)
.riverFrequency(0.02)
.riverDepth(6)
.riverWidth(4)
.erosionMaxDropletSteps(15)
.erosionIterations(128)
.caveCenterY(-10.0)
.caveSpread(40.0)
.caveSurfaceCutoff(5.0)
.caveSpaghettiThreshold(0.4)
.caveCheeseThreshold(0.6)
.caveNoodleThreshold(0.2)
.caveNoodleFrequency(0.06)
.riverCanyonDepth(1)
.riverCanyonWidth(3)
.riverValleyProfile(RiverProfile.FLOODPLAIN)
.riverFloodplainWidth(10)
.riverTableResponse(0.5)
.ridgeFrequency(0.004)
.ridgeOctaves(4)
.ridgeAmplitude(2.0)
.fbmFrequency(0.006)
.fbmOctaves(5)
.flatFrequency(0.01)
.continentalnessBlendSharpness(3.0)
.noiseBackend("fastnoise")
.featureSeedOffset(0xBEEFL)
.erosionDropletCount(2048)
.erosionGravity(0.3f)
.plateauSize(5)
.plateauTargetHeight(120)
.domainWarpAmplitude(1.0)
.treeDensityFrequency(0.04)
.configVersion(1)
.build();
    }

    /**
     * Builds a synthetic v2 config (configVersion = 2) with non-default values for
     * the 2 v3 fields to verify migration overwrites them with builder defaults.
     */
    private static GeoForgeConfig v2Config() {
        return GeoForgeConfig.builder()
                .minHeight(-60)
                .maxHeight(200)
                .seaLevel(70)
                .continentalBase(40.0)
                .continentalHeightAmplitude(100.0)
                .temperatureFrequency(0.002)
                .temperatureYFrequency(0.01)
                .humidityFrequency(0.002)
                .caveFrequency(0.04)
                .caveAmplitude(10.0)
                .caveOctaves(3)
                .caveLacunarity(3.0)
                .cavePersistence(0.6)
                .riverFrequency(0.02)
                .riverDepth(6)
                .riverWidth(4)
                .erosionMaxDropletSteps(15)
                .erosionIterations(128)
                .caveCenterY(-10.0)
                .caveSpread(40.0)
                .caveSurfaceCutoff(5.0)
                .caveSpaghettiThreshold(0.4)
                .caveCheeseThreshold(0.6)
                .caveNoodleThreshold(0.2)
                .caveNoodleFrequency(0.06)
                .riverCanyonDepth(1)
                .riverCanyonWidth(3)
                .riverValleyProfile(RiverProfile.FLOODPLAIN)
                .riverFloodplainWidth(10)
                .riverTableResponse(0.5)
                .ridgeFrequency(0.004)
                .ridgeOctaves(4)
                .ridgeAmplitude(2.0)
                .fbmFrequency(0.006)
                .fbmOctaves(5)
                .flatFrequency(0.01)
                .continentalnessBlendSharpness(3.0)
                .noiseBackend("fastnoise")
                .featureSeedOffset(0xBEEFL)
                .erosionDropletCount(2048)
                .erosionGravity(0.3f)
                .plateauSize(5)
                .plateauTargetHeight(120)
                .domainWarpAmplitude(1.0)
                // v3 fields — non-default to verify overwrite
                .treeDensityFrequency(0.04)
                .configVersion(2)
                .build();
    }

    @Test
    void migrateV1PreservesAllOldFields() {
        GeoForgeConfig v1 = v1Config();
        GeoForgeConfig result = ConfigMigrator.migrate(v1);

        // All 22 v1 fields must be preserved exactly
        assertEquals(-60, result.minHeight());
        assertEquals(200, result.maxHeight());
        assertEquals(70, result.seaLevel());
        assertEquals(40.0, result.continentalBase());
        assertEquals(100.0, result.continentalHeightAmplitude());
        assertEquals(0.002, result.temperatureFrequency());
        assertEquals(0.01, result.temperatureYFrequency());
        assertEquals(0.002, result.humidityFrequency());
        assertEquals(0.04, result.caveFrequency());
        assertEquals(10.0, result.caveAmplitude());
        assertEquals(3, result.caveOctaves());
        assertEquals(3.0, result.caveLacunarity());
        assertEquals(0.6, result.cavePersistence());
        assertEquals(0.02, result.riverFrequency());
        assertEquals(6, result.riverDepth());
        assertEquals(4, result.riverWidth());
        assertEquals(15, result.erosionMaxDropletSteps());
        assertEquals(128, result.erosionIterations());

        // configVersion must be upgraded to 4 (bumped for caveOctaves 3 + biome border widening)
        assertEquals(4, result.configVersion());
    }

    @Test
    void migrateV1FillsNewFieldsWithDefaults() {
        GeoForgeConfig v1 = v1Config();
        GeoForgeConfig result = ConfigMigrator.migrate(v1);
        GeoForgeConfig defaults = GeoForgeConfig.defaults();

        // Cave Y-envelope defaults
        assertEquals(defaults.caveCenterY(), result.caveCenterY());
        assertEquals(defaults.caveSpread(), result.caveSpread());
        assertEquals(defaults.caveSurfaceCutoff(), result.caveSurfaceCutoff());
        assertEquals(defaults.caveSpaghettiThreshold(), result.caveSpaghettiThreshold());
        assertEquals(defaults.caveCheeseThreshold(), result.caveCheeseThreshold());
        assertEquals(defaults.caveNoodleThreshold(), result.caveNoodleThreshold());
        assertEquals(defaults.caveNoodleFrequency(), result.caveNoodleFrequency());

        // River v2 defaults
        assertEquals(defaults.riverCanyonDepth(), result.riverCanyonDepth());
        assertEquals(defaults.riverCanyonWidth(), result.riverCanyonWidth());
        assertEquals(defaults.riverValleyProfile(), result.riverValleyProfile());
        assertEquals(defaults.riverFloodplainWidth(), result.riverFloodplainWidth());
        assertEquals(defaults.riverTableResponse(), result.riverTableResponse());

        // Multi-noise terrain defaults
        assertEquals(defaults.ridgeFrequency(), result.ridgeFrequency());
        assertEquals(defaults.ridgeOctaves(), result.ridgeOctaves());
        assertEquals(defaults.ridgeAmplitude(), result.ridgeAmplitude());
        assertEquals(defaults.fbmFrequency(), result.fbmFrequency());
        assertEquals(defaults.fbmOctaves(), result.fbmOctaves());
        assertEquals(defaults.flatFrequency(), result.flatFrequency());
        assertEquals(defaults.continentalnessBlendSharpness(), result.continentalnessBlendSharpness());

        // Decorations defaults
        assertEquals(defaults.featureSeedOffset(), result.featureSeedOffset());

        // Erosion defaults
        assertEquals(1024, result.erosionDropletCount());
        assertEquals(defaults.erosionGravity(), result.erosionGravity());

        // Domain warping defaults
        assertEquals(defaults.domainWarpAmplitude(), result.domainWarpAmplitude());

        // New v3 fields must get defaults
        assertEquals(defaults.treeDensityFrequency(), result.treeDensityFrequency());
    }

    @Test
    void migrateIsIdempotent() {
        GeoForgeConfig v1 = v1Config();
        GeoForgeConfig once = ConfigMigrator.migrate(v1);
        GeoForgeConfig twice = ConfigMigrator.migrate(once);

        // Second migration must produce identical result (record equals compares all fields)
        assertEquals(once, twice);
    }
    @Test
    void migrateV2PreservesAllFields() {
        GeoForgeConfig v2 = v2Config();
        GeoForgeConfig result = ConfigMigrator.migrate(v2);
        GeoForgeConfig defaults = GeoForgeConfig.defaults();

        // All v1+v2 fields preserved — verify a sample
        assertEquals(-60, result.minHeight());
        assertEquals(200, result.maxHeight());
        assertEquals(0.02, result.riverFrequency());
        assertEquals(RiverProfile.FLOODPLAIN, result.riverValleyProfile());
        assertEquals("fastnoise", result.noiseBackend());

        // New v3 fields get builder defaults
        assertEquals(defaults.treeDensityFrequency(), result.treeDensityFrequency());

        // configVersion upgraded to 4
        assertEquals(4, result.configVersion());
    }

    @Test
    void migrateV2IsIdempotent() {
        GeoForgeConfig v2 = v2Config();
        GeoForgeConfig once = ConfigMigrator.migrate(v2);
        GeoForgeConfig twice = ConfigMigrator.migrate(once);
        assertEquals(once, twice);
    }

    @Test
    void migrateV3PassesThrough() {
        GeoForgeConfig v3 = GeoForgeConfig.defaults();
        assertSame(v3, ConfigMigrator.migrate(v3));
    }
}
