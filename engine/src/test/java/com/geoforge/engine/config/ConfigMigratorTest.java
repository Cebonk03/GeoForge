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
        return new GeoForgeConfig(
                // V1 fields — set to values distinct from defaults
                -60,    // minHeight (default: -64)
                200,    // maxHeight (default: 180)
                70,     // seaLevel (default: 63)
                40.0,   // continentalBase (default: 50.0)
                100.0,  // continentalHeightAmplitude (default: 120.0)
                0.002,  // temperatureFrequency (default: 0.001)
                0.01,   // temperatureYFrequency (default: 0.005)
                0.002,  // humidityFrequency (default: 0.001)
                0.04,   // caveFrequency (default: 0.03)
                10.0,   // caveAmplitude (default: 8.0)
                3,      // caveOctaves (default: 2)
                3.0,    // caveLacunarity (default: 2.0)
                0.6,    // cavePersistence (default: 0.5)
                0.02,   // riverFrequency (default: 0.01)
                6,      // riverDepth (default: 8)
                4,      // riverWidth (default: 3)
                15,     // erosionMaxDropletSteps (default: 10)
                128,    // erosionIterations (default: 64)
                // V2 fields — set to non-default values to detect overwrite
                -10.0,  // caveCenterY (default: -20.0)
                40.0,   // caveSpread (default: 48.0)
                5.0,    // caveSurfaceCutoff (default: 8.0)
                0.4,    // caveSpaghettiThreshold (default: 0.3)
                0.6,    // caveCheeseThreshold (default: 0.5)
                0.2,    // caveNoodleThreshold (default: 0.15)
                0.06,   // caveNoodleFrequency (default: 0.05)
                1,      // riverCanyonDepth (default: 0)
                3,      // riverCanyonWidth (default: 2)
                RiverProfile.FLOODPLAIN, // riverValleyProfile
                10,     // riverFloodplainWidth (default: 5)
                0.5,    // riverTableResponse (default: 0.0)
                0.004,  // ridgeFrequency (default: 0.003)
                4,      // ridgeOctaves (default: 3)
                2.0,    // ridgeAmplitude (default: 1.0)
                0.006,  // fbmFrequency (default: 0.005)
                5,      // fbmOctaves (default: 4)
                0.01,   // flatFrequency (default: 0.008)
                3.0,    // continentalnessBlendSharpness (default: 2.0)
                "fastnoise", // noiseBackend (default: "simplex")
                0.2,    // treeDensity (default: 0.1)
                0.5,    // vegetationDensity (default: 0.3)
                0xBEEFL, // featureSeedOffset (default: 0xCAFEBABEL)
                15,     // maxTreeHeight (default: 12)
                2048,   // erosionDropletCount (default: 1024)
                0.3f,   // erosionGravity (default: 0.2f)
                5,      // plateauSize (default: 0)
                120,    // plateauTargetHeight (default: 64)
                1.0,    // domainWarpAmplitude (default: 1.5)
                6,      // minTreeHeight — non-default (default: 4)
                0.04,   // treeDensityFrequency — non-default (default: 0.02)
                1       // configVersion = 1 (v1)
        );
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
                .treeDensity(0.2)
                .vegetationDensity(0.5)
                .featureSeedOffset(0xBEEFL)
                .maxTreeHeight(15)
                .erosionDropletCount(2048)
                .erosionGravity(0.3f)
                .plateauSize(5)
                .plateauTargetHeight(120)
                .domainWarpAmplitude(1.0)
                // v3 fields — non-default to verify overwrite
                .minTreeHeight(6)
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

        // configVersion must be upgraded to 3
        assertEquals(3, result.configVersion());
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
        assertEquals(defaults.treeDensity(), result.treeDensity());
        assertEquals(defaults.vegetationDensity(), result.vegetationDensity());
        assertEquals(defaults.featureSeedOffset(), result.featureSeedOffset());
        assertEquals(defaults.maxTreeHeight(), result.maxTreeHeight());

        // Erosion defaults
        assertEquals(1024, result.erosionDropletCount());
        assertEquals(defaults.erosionGravity(), result.erosionGravity());

        // Domain warping defaults
        assertEquals(defaults.domainWarpAmplitude(), result.domainWarpAmplitude());

        // New v3 fields must get defaults
        assertEquals(defaults.minTreeHeight(), result.minTreeHeight());
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
        assertEquals(0.2, result.treeDensity());

        // New v3 fields get builder defaults
        assertEquals(defaults.minTreeHeight(), result.minTreeHeight());
        assertEquals(defaults.treeDensityFrequency(), result.treeDensityFrequency());

        // configVersion upgraded to 3
        assertEquals(3, result.configVersion());
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
