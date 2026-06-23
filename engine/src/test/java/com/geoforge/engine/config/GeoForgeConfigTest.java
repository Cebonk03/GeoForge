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

    @Test
    void validation_maxHeightMustExceedMinHeight() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        0, 0, 0, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.005, 0.001, 10, 64));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        10, 5, 7, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.005, 0.001, 10, 64));
    }

    @Test
    void validation_seaLevelMustBeInBounds() {
        // seaLevel too low
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, -65, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.005, 0.001, 10, 64));
        // seaLevel too high
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 181, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.005, 0.001, 10, 64));
    }

    @Test
    void validation_octavesMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 0, 2.0, 0.5,
                        0.001, 0.005, 0.001, 10, 64));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, -1, 2.0, 0.5,
                        0.001, 0.005, 0.001, 10, 64));
    }

    @Test
    void validation_erosionStepsMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.005, 0.001, 0, 64));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.005, 0.001, -5, 64));
    }

    @Test
    void validation_erosionIterationsMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.005, 0.001, 10, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.005, 0.001, 10, -10));
    }

    @Test
    void validation_continentalFrequencyMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.0, 4, 2.0, 0.5,
                        0.001, 0.005, 0.001, 10, 64));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, -1.0, 4, 2.0, 0.5,
                        0.001, 0.005, 0.001, 10, 64));
    }

    @Test
    void validation_continentalLacunarityMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 0.0, 0.5,
                        0.001, 0.005, 0.001, 10, 64));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, -1.0, 0.5,
                        0.001, 0.005, 0.001, 10, 64));
    }

    @Test
    void validation_continentalPersistenceMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.0,
                        0.001, 0.005, 0.001, 10, 64));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, -0.5,
                        0.001, 0.005, 0.001, 10, 64));
    }

    @Test
    void validation_temperatureFrequencyMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.0, 0.005, 0.001, 10, 64));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        -1.0, 0.005, 0.001, 10, 64));
    }
    @Test
    void validation_temperatureYFrequencyMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.0, 0.001, 10, 64));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, -1.0, 0.001, 10, 64));
    }
    @Test
    void validation_humidityFrequencyMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.005, 0.0, 10, 64));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GeoForgeConfig(
                        -64, 180, 63, 50.0, 120.0, 0.004, 4, 2.0, 0.5,
                        0.001, 0.005, -1.0, 10, 64));
    }

    @Test
    void withSeaLevel_overridesOnlySeaLevel() {
        var cfg = GeoForgeConfig.withSeaLevel(50);
        assertEquals(50, cfg.seaLevel());

        // All other fields must match defaults
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
        assertEquals(defaults.erosionMaxDropletSteps(), cfg.erosionMaxDropletSteps());
        assertEquals(defaults.erosionIterations(), cfg.erosionIterations());
    }
}
