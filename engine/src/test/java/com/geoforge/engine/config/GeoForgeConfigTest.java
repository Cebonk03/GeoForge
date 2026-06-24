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
                          int em, int ei) {
        return new Object[]{min, max, sea, cb, cha, cf, co, cl, cp, tf, tyf, hf,
                cavf, cava, cavo, cavl, cavp, em, ei};
    }

    private GeoForgeConfig makeCfg(Object... args) {
        return new GeoForgeConfig(
                (int) args[0], (int) args[1], (int) args[2],
                (double) args[3], (double) args[4], (double) args[5],
                (int) args[6], (double) args[7], (double) args[8],
                (double) args[9], (double) args[10], (double) args[11],
                (double) args[12], (double) args[13], (int) args[14],
                (double) args[15], (double) args[16],
                (int) args[17], (int) args[18]);
    }

    @Test
    void validation_maxHeightMustExceedMinHeight() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(0, 0, 0, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(10, 5, 7, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
    }

    @Test
    void validation_seaLevelMustBeInBounds() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, -65, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 181, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
    }

    @Test
    void validation_octavesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 0, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, -1, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
    }

    @Test
    void validation_erosionStepsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 0, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, -5, 64)));
    }

    @Test
    void validation_erosionIterationsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 0)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, -10)));
    }

    @Test
    void validation_frequenciesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.0, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, -1.0, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
    }

    @Test
    void validation_temperatureFrequenciesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.0, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, -1.0, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.0, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, -1.0, 0.001, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.0, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, -1.0, 0.03, 8.0, 2, 2.0, 0.5, 10, 64)));
    }

    @Test
    void validation_caveOctavesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 0, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, -1, 2.0, 0.5, 10, 64)));
    }

    @Test
    void validation_caveFrequencyMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.0, 8.0, 2, 2.0, 0.5, 10, 64)));
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, -1.0, 8.0, 2, 2.0, 0.5, 10, 64)));
    }

    @Test
    void validation_caveLacunarityMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 0.0, 0.5, 10, 64)));
    }

    @Test
    void validation_cavePersistenceMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> makeCfg(args(-64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5, 0.001, 0.005, 0.001, 0.03, 8.0, 2, 2.0, 0.0, 10, 64)));
    }

    @Test
    void withSeaLevel_overridesOnlySeaLevel() {
        var cfg = GeoForgeConfig.withSeaLevel(50);
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
        assertEquals(defaults.erosionMaxDropletSteps(), cfg.erosionMaxDropletSteps());
        assertEquals(defaults.erosionIterations(), cfg.erosionIterations());
    }

    @Test
    void withCaveAmplitude_overridesOnlyCaveAmplitude() {
        var cfg = GeoForgeConfig.withCaveAmplitude(0.0);
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
        assertEquals(defaults.erosionMaxDropletSteps(), cfg.erosionMaxDropletSteps());
        assertEquals(defaults.erosionIterations(), cfg.erosionIterations());
    }
}
