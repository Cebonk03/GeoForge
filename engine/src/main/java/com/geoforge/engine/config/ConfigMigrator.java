package com.geoforge.engine.config;

/**
 * Utility for migrating a {@link GeoForgeConfig} from v1 (22 fields) to v2 (52 fields).
 *
 * <p>Migration extracts all 22 original values from the old config and fills the 27 new
 * fields with their defaults from {@link GeoForgeConfig#defaults()}. If the config already
 * has {@code configVersion &gt;= 2}, it is returned unchanged (idempotent).
 *
 * <p>This class is thread-safe and stateless.
 */
public final class ConfigMigrator {

    private ConfigMigrator() {
        // utility class
    }

    /**
     * Migrates a {@link GeoForgeConfig} from v1 to v2 if needed.
     *
     * @param oldConfig the configuration to migrate; must not be null
     * @return the migrated configuration with configVersion = 2, or the same instance
     *         if it was already at v2 or higher
     */
    public static GeoForgeConfig migrate(GeoForgeConfig oldConfig) {
        if (oldConfig.configVersion() >= 2) {
            return oldConfig;
        }

        return GeoForgeConfig.builder()
                .minHeight(oldConfig.minHeight())
                .maxHeight(oldConfig.maxHeight())
                .seaLevel(oldConfig.seaLevel())
                .continentalBase(oldConfig.continentalBase())
                .continentalHeightAmplitude(oldConfig.continentalHeightAmplitude())
                .continentalFrequency(oldConfig.continentalFrequency())
                .continentalOctaves(oldConfig.continentalOctaves())
                .continentalLacunarity(oldConfig.continentalLacunarity())
                .continentalPersistence(oldConfig.continentalPersistence())
                .temperatureFrequency(oldConfig.temperatureFrequency())
                .temperatureYFrequency(oldConfig.temperatureYFrequency())
                .humidityFrequency(oldConfig.humidityFrequency())
                .caveFrequency(oldConfig.caveFrequency())
                .caveAmplitude(oldConfig.caveAmplitude())
                .caveOctaves(oldConfig.caveOctaves())
                .caveLacunarity(oldConfig.caveLacunarity())
                .cavePersistence(oldConfig.cavePersistence())
                .riverFrequency(oldConfig.riverFrequency())
                .riverDepth(oldConfig.riverDepth())
                .riverWidth(oldConfig.riverWidth())
                .erosionMaxDropletSteps(oldConfig.erosionMaxDropletSteps())
                .erosionIterations(oldConfig.erosionIterations())
                .erosionDropletCount(1024)
                .build();
    }
}
