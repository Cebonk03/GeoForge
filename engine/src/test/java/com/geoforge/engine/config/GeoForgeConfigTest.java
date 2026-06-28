package com.geoforge.engine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("validation")
@DisplayName("GeoForgeConfig tests")
class GeoForgeConfigTest {

    @Test
    void defaults_returnsValidConfig() {
        assertDoesNotThrow(GeoForgeConfig::defaults);
    }

    @Test
    void defaults_knownValues() {
        var cfg = GeoForgeConfig.defaults();
        assertEquals(-64, cfg.minHeight());
        assertEquals(180, cfg.maxHeight());
        assertEquals(63, cfg.seaLevel());
        assertEquals(50.0, cfg.continentalBase());
        assertEquals(120.0, cfg.continentalHeightAmplitude());
        assertEquals(0.001, cfg.temperatureFrequency());
        assertEquals(0.005, cfg.temperatureYFrequency());
        assertEquals(0.001, cfg.humidityFrequency());
        assertEquals(0.03, cfg.caveFrequency());
        assertEquals(8.0, cfg.caveAmplitude());
        assertEquals(2, cfg.caveOctaves());
        assertEquals(2.0, cfg.caveLacunarity());
        assertEquals(0.5, cfg.cavePersistence());
        assertEquals(0.01, cfg.riverFrequency());
        assertEquals(8, cfg.riverDepth());
        assertEquals(3, cfg.riverWidth());
        assertEquals(10, cfg.erosionMaxDropletSteps());
        assertEquals(64, cfg.erosionIterations());
    }

    @Test
    void equality_recordsAreEqualByValue() {
        var a = GeoForgeConfig.defaults();
        var b = GeoForgeConfig.defaults();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    private Object[] args(int min, int max, int sea, double cb, double cha,
                          double cf, int co, double cl, double cp,
                          double tf, double tyf, double hf,
                          double cavf, double cava, int cavo, double cavl, double cavp,
                          double rf, int rd, int rw,
                          int em, int ei) {
        return new Object[]{min, max, sea, cb, cha, cf, co, cl, cp, tf, tyf, hf,
                cavf, cava, cavo, cavl, cavp, rf, rd, rw, em, ei};
    }

    private GeoForgeConfig makeCfg(Object... args) {
        return GeoForgeConfig.builder()
                .minHeight((int) args[0]).maxHeight((int) args[1]).seaLevel((int) args[2])
                .continentalBase((double) args[3]).continentalHeightAmplitude((double) args[4])
                .temperatureFrequency((double) args[9]).temperatureYFrequency((double) args[10])
                .humidityFrequency((double) args[11])
                .caveFrequency((double) args[12]).caveAmplitude((double) args[13])
                .caveOctaves((int) args[14]).caveLacunarity((double) args[15])
                .cavePersistence((double) args[16])
                .riverFrequency((double) args[17]).riverDepth((int) args[18])
                .riverWidth((int) args[19])
                .erosionMaxDropletSteps((int) args[20]).erosionIterations((int) args[21])
                .build();
    }

    // Default river params for validation tests that are not testing rivers
    private static final double RIV_FREQ = 0.01;
    private static final int RIV_DEPTH = 8;
    private static final int RIV_WIDTH = 3;

    @Test
    void validation_maxHeightMustExceedMinHeight() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(0, 0, 0, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(10, 5, 7, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
    }

    @Test
    void validation_seaLevelMustBeInBounds() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, -65, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 181, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
    }


    @Test
    void validation_erosionStepsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 0, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, -5, 64)));
    }

    @Test
    void validation_erosionIterationsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 0)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, -10)));
    }


    @Test
    void validation_temperatureFrequenciesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.0, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, -1.0, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.0, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, -1.0, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.0, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, -1.0, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
    }

    @Test
    void validation_caveOctavesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 0, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, -1, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
    }

    @Test
    void validation_caveFrequencyMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.0, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, -1.0, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
    }

    @Test
    void validation_caveLacunarityMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 0.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
    }

    @Test
    void validation_cavePersistenceMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.0, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
    }

    @Test
    void validation_riverFrequencyMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 0.0, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, -1.0, RIV_DEPTH, RIV_WIDTH, 10, 64)));
    }

    @Test
    void validation_riverDepthMustBeNonNegative() {
        // riverDepth=0 is valid (disables river carving)
        assertDoesNotThrow(() -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, 0, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, -5, RIV_WIDTH, 10, 64)));
    }

    @Test
    void validation_riverWidthMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, 0, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, -3, 10, 64)));
    }

    // --- Builder tests ---

    @Test
    void builder_defaultsMatchDefaultsFactory() {
        assertEquals(GeoForgeConfig.defaults(), GeoForgeConfig.builder().build());
    }

    @Test
    void builder_overridesSeaLevel() {
        var cfg = GeoForgeConfig.builder().seaLevel(50).build();
        assertEquals(50, cfg.seaLevel());

        var defaults = GeoForgeConfig.defaults();
        assertEquals(defaults.minHeight(), cfg.minHeight());
        assertEquals(defaults.maxHeight(), cfg.maxHeight());
        assertEquals(defaults.continentalBase(), cfg.continentalBase());
        assertEquals(defaults.continentalHeightAmplitude(), cfg.continentalHeightAmplitude());
        assertEquals(defaults.temperatureFrequency(), cfg.temperatureFrequency());
        assertEquals(defaults.temperatureYFrequency(), cfg.temperatureYFrequency());
        assertEquals(defaults.humidityFrequency(), cfg.humidityFrequency());
        assertEquals(defaults.caveFrequency(), cfg.caveFrequency());
        assertEquals(defaults.caveAmplitude(), cfg.caveAmplitude());
        assertEquals(defaults.caveOctaves(), cfg.caveOctaves());
        assertEquals(defaults.caveLacunarity(), cfg.caveLacunarity());
        assertEquals(defaults.cavePersistence(), cfg.cavePersistence());
        assertEquals(defaults.riverFrequency(), cfg.riverFrequency());
        assertEquals(defaults.riverDepth(), cfg.riverDepth());
        assertEquals(defaults.riverWidth(), cfg.riverWidth());
        assertEquals(defaults.erosionMaxDropletSteps(), cfg.erosionMaxDropletSteps());
        assertEquals(defaults.erosionIterations(), cfg.erosionIterations());
    }

    @Test
    void builder_overridesCaveAmplitude() {
        var cfg = GeoForgeConfig.builder().caveAmplitude(0.0).build();
        assertEquals(0.0, cfg.caveAmplitude());

        var defaults = GeoForgeConfig.defaults();
        assertEquals(defaults.minHeight(), cfg.minHeight());
        assertEquals(defaults.maxHeight(), cfg.maxHeight());
        assertEquals(defaults.seaLevel(), cfg.seaLevel());
        assertEquals(defaults.continentalBase(), cfg.continentalBase());
        assertEquals(defaults.continentalHeightAmplitude(), cfg.continentalHeightAmplitude());
        assertEquals(defaults.temperatureFrequency(), cfg.temperatureFrequency());
        assertEquals(defaults.temperatureYFrequency(), cfg.temperatureYFrequency());
        assertEquals(defaults.humidityFrequency(), cfg.humidityFrequency());
        assertEquals(defaults.caveFrequency(), cfg.caveFrequency());
        assertEquals(defaults.caveOctaves(), cfg.caveOctaves());
        assertEquals(defaults.caveLacunarity(), cfg.caveLacunarity());
        assertEquals(defaults.cavePersistence(), cfg.cavePersistence());
        assertEquals(defaults.riverFrequency(), cfg.riverFrequency());
        assertEquals(defaults.riverDepth(), cfg.riverDepth());
        assertEquals(defaults.riverWidth(), cfg.riverWidth());
        assertEquals(defaults.erosionMaxDropletSteps(), cfg.erosionMaxDropletSteps());
        assertEquals(defaults.erosionIterations(), cfg.erosionIterations());
    }

    @Test
    void builder_overridesRiverParams() {
        var cfg = GeoForgeConfig.builder()
                .riverFrequency(0.05)
                .riverDepth(16)
                .riverWidth(5)
                .build();
        assertEquals(0.05, cfg.riverFrequency());
        assertEquals(16, cfg.riverDepth());
        assertEquals(5, cfg.riverWidth());
    }

    @Test
    void builder_overridesMultipleParams() {
        var cfg = GeoForgeConfig.builder()
                .minHeight(-128)
                .maxHeight(256)
                .seaLevel(32)
                .continentalBase(30.0)
                .continentalHeightAmplitude(80.0)
                .build();
        assertEquals(-128, cfg.minHeight());
        assertEquals(256, cfg.maxHeight());
        assertEquals(32, cfg.seaLevel());
        assertEquals(30.0, cfg.continentalBase());
        assertEquals(80.0, cfg.continentalHeightAmplitude());
    }

    @Test
    void builder_chainingReturnsThis() {
        var builder = GeoForgeConfig.builder();
        assertSame(builder, builder.minHeight(0));
        assertSame(builder, builder.maxHeight(100));
        assertSame(builder, builder.seaLevel(50));
    }

    @Test
    void builder_validationStillApplies() {
        assertThrows(IllegalArgumentException.class,
                () -> GeoForgeConfig.builder().maxHeight(10).minHeight(20).build());
    }

    // ========== New field default value tests ==========

    @Test
    void defaults_knownValues_newFields() {
        var cfg = GeoForgeConfig.defaults();
        // Cave Y-envelope
        assertEquals(-20.0, cfg.caveCenterY());
        assertEquals(48.0, cfg.caveSpread());
        assertEquals(8.0, cfg.caveSurfaceCutoff());
        assertEquals(0.3, cfg.caveSpaghettiThreshold());
        assertEquals(0.5, cfg.caveCheeseThreshold());
        assertEquals(0.15, cfg.caveNoodleThreshold());
        assertEquals(0.05, cfg.caveNoodleFrequency());
        // River v2
        assertEquals(0, cfg.riverCanyonDepth());
        assertEquals(2, cfg.riverCanyonWidth());
        assertEquals(RiverProfile.VSHAPED, cfg.riverValleyProfile());
        assertEquals(5, cfg.riverFloodplainWidth());
        assertEquals(0.0, cfg.riverTableResponse());
        // Multi-noise terrain
        assertEquals(0.003, cfg.ridgeFrequency());
        assertEquals(3, cfg.ridgeOctaves());
        assertEquals(1.0, cfg.ridgeAmplitude());
        assertEquals(0.005, cfg.fbmFrequency());
        assertEquals(4, cfg.fbmOctaves());
        assertEquals(0.008, cfg.flatFrequency());
        assertEquals(2.0, cfg.continentalnessBlendSharpness());
        // Decorations
        assertEquals(0.1, cfg.treeDensity());
        assertEquals(0.3, cfg.vegetationDensity());
        assertEquals(0xCAFEBABEL, cfg.featureSeedOffset());
        assertEquals(12, cfg.maxTreeHeight());
        // Erosion
        assertEquals(1024, cfg.erosionDropletCount());
        assertEquals(0.2f, cfg.erosionGravity());
        // Domain warping
        assertEquals(1.5, cfg.domainWarpAmplitude());
        // Config version
        assertEquals(2, cfg.configVersion());
    }

    // ========== Validation tests for new fields ==========

    @Test
    void validation_caveSpreadMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().caveSpread(0).build());
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().caveSpread(-1).build());
    }

    @Test
    void validation_caveSurfaceCutoffMustBeNonNegative() {
        assertDoesNotThrow(() -> GeoForgeConfig.builder().caveSurfaceCutoff(0).build());
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().caveSurfaceCutoff(-1).build());
    }

    @Test
    void validation_ridgeFrequencyMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().ridgeFrequency(0).build());
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().ridgeFrequency(-0.1).build());
    }

    @Test
    void validation_ridgeOctavesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().ridgeOctaves(0).build());
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().ridgeOctaves(-1).build());
    }

    @Test
    void validation_fbmFrequencyMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().fbmFrequency(0).build());
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().fbmFrequency(-0.1).build());
    }

    @Test
    void validation_fbmOctavesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().fbmOctaves(0).build());
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().fbmOctaves(-1).build());
    }

    @Test
    void validation_flatFrequencyMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().flatFrequency(0).build());
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().flatFrequency(-0.1).build());
    }

    @Test
    void validation_treeDensityMustBeInRange() {
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().treeDensity(-0.01).build());
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().treeDensity(1.01).build());
        assertDoesNotThrow(() -> GeoForgeConfig.builder().treeDensity(0).build());
        assertDoesNotThrow(() -> GeoForgeConfig.builder().treeDensity(1.0).build());
    }

    @Test
    void validation_vegetationDensityMustBeInRange() {
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().vegetationDensity(-0.01).build());
        assertThrows(IllegalArgumentException.class, () -> GeoForgeConfig.builder().vegetationDensity(1.01).build());
        assertDoesNotThrow(() -> GeoForgeConfig.builder().vegetationDensity(0).build());
        assertDoesNotThrow(() -> GeoForgeConfig.builder().vegetationDensity(1.0).build());
    }

    // ========== Builder override tests for new fields ==========

    @Test
    void builder_overridesCaveYEnvelope() {
        var cfg = GeoForgeConfig.builder()
                .caveCenterY(-10)
                .caveSpread(64)
                .caveSurfaceCutoff(4)
                .caveSpaghettiThreshold(0.4)
                .caveCheeseThreshold(0.6)
                .caveNoodleThreshold(0.25)
                .caveNoodleFrequency(0.08)
                .build();
        assertEquals(-10.0, cfg.caveCenterY());
        assertEquals(64.0, cfg.caveSpread());
        assertEquals(4.0, cfg.caveSurfaceCutoff());
        assertEquals(0.4, cfg.caveSpaghettiThreshold());
        assertEquals(0.6, cfg.caveCheeseThreshold());
        assertEquals(0.25, cfg.caveNoodleThreshold());
        assertEquals(0.08, cfg.caveNoodleFrequency());
    }

    @Test
    void builder_overridesRiverV2() {
        var cfg = GeoForgeConfig.builder()
                .riverCanyonDepth(10)
                .riverCanyonWidth(4)
                .riverValleyProfile(RiverProfile.FLOODPLAIN)
                .riverFloodplainWidth(8)
                .riverTableResponse(1.5)
                .build();
        assertEquals(10, cfg.riverCanyonDepth());
        assertEquals(4, cfg.riverCanyonWidth());
        assertEquals(RiverProfile.FLOODPLAIN, cfg.riverValleyProfile());
        assertEquals(8, cfg.riverFloodplainWidth());
        assertEquals(1.5, cfg.riverTableResponse());
    }

    @Test
    void builder_overridesMultiNoise() {
        var cfg = GeoForgeConfig.builder()
                .ridgeFrequency(0.005)
                .ridgeOctaves(5)
                .ridgeAmplitude(2.0)
                .fbmFrequency(0.008)
                .fbmOctaves(6)
                .flatFrequency(0.01)
                .continentalnessBlendSharpness(3.5)
                .build();
        assertEquals(0.005, cfg.ridgeFrequency());
        assertEquals(5, cfg.ridgeOctaves());
        assertEquals(2.0, cfg.ridgeAmplitude());
        assertEquals(0.008, cfg.fbmFrequency());
        assertEquals(6, cfg.fbmOctaves());
        assertEquals(0.01, cfg.flatFrequency());
        assertEquals(3.5, cfg.continentalnessBlendSharpness());
    }

    @Test
    void builder_overridesDecorations() {
        var cfg = GeoForgeConfig.builder()
                .treeDensity(0.5)
                .vegetationDensity(0.8)
                .featureSeedOffset(12345L)
                .maxTreeHeight(20)
                .build();
        assertEquals(0.5, cfg.treeDensity());
        assertEquals(0.8, cfg.vegetationDensity());
        assertEquals(12345L, cfg.featureSeedOffset());
        assertEquals(20, cfg.maxTreeHeight());
    }

    @Test
    void builder_overridesErosion() {
        var cfg = GeoForgeConfig.builder()
                .erosionDropletCount(512)
                .erosionGravity(0.5f)
                .build();
        assertEquals(512, cfg.erosionDropletCount());
        assertEquals(0.5f, cfg.erosionGravity());
    }

    @Test
    void builder_overridesDomainWarp() {
        var cfg = GeoForgeConfig.builder()
                .domainWarpAmplitude(3.0)
                .build();
        assertEquals(3.0, cfg.domainWarpAmplitude());
    }

    @Test
    void builder_overridesConfigVersion() {
        var cfg = GeoForgeConfig.builder()
                .configVersion(3)
                .build();
        assertEquals(3, cfg.configVersion());
    }

    @Test
    void builder_chainingReturnsThis_newFields() {
        var builder = GeoForgeConfig.builder();
        assertSame(builder, builder.caveCenterY(-10));
        assertSame(builder, builder.ridgeFrequency(0.005));
        assertSame(builder, builder.treeDensity(0.5));
    }

    @Test
    void builder_fullChain() {
        // Verify the builder chain works: GeoForgeConfig.builder().caveCenterY(-10).ridgeFrequency(0.005).build()
        var cfg = GeoForgeConfig.builder().caveCenterY(-10).ridgeFrequency(0.005).build();
        assertEquals(-10.0, cfg.caveCenterY());
        assertEquals(0.005, cfg.ridgeFrequency());
    }

    @Test
    void validation_riverValleyProfileMustNotBeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> GeoForgeConfig.builder().riverValleyProfile(null).build());
    }
}
