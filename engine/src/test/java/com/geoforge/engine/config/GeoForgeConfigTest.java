package com.geoforge.engine.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

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
        assertEquals(0.004, cfg.continentalFrequency());
        assertEquals(4, cfg.continentalOctaves());
        assertEquals(2.0, cfg.continentalLacunarity());
        assertEquals(0.5, cfg.continentalPersistence());
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
        return new GeoForgeConfig(
                (int) args[0], (int) args[1], (int) args[2],
                (double) args[3], (double) args[4], (double) args[5],
                (int) args[6], (double) args[7], (double) args[8],
                (double) args[9], (double) args[10], (double) args[11],
                (double) args[12], (double) args[13], (int) args[14],
                (double) args[15], (double) args[16],
                (double) args[17], (int) args[18], (int) args[19],
                (int) args[20], (int) args[21]);
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
    void validation_octavesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 0, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, -1, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
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
    void validation_frequenciesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.0, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, -1.0, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, RIV_DEPTH, RIV_WIDTH, 10, 64)));
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
    void validation_riverDepthMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, RIV_FREQ, 0, RIV_WIDTH, 10, 64)));
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
        assertEquals(defaults.continentalFrequency(), cfg.continentalFrequency());
        assertEquals(defaults.continentalOctaves(), cfg.continentalOctaves());
        assertEquals(defaults.continentalLacunarity(), cfg.continentalLacunarity());
        assertEquals(defaults.continentalPersistence(), cfg.continentalPersistence());
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
        assertEquals(defaults.continentalFrequency(), cfg.continentalFrequency());
        assertEquals(defaults.continentalOctaves(), cfg.continentalOctaves());
        assertEquals(defaults.continentalLacunarity(), cfg.continentalLacunarity());
        assertEquals(defaults.continentalPersistence(), cfg.continentalPersistence());
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
}
