package com.geoforge.engine.config;

/**
 * Utility for migrating a {@link GeoForgeConfig} between versions.
 *
 * <p>Migration paths:
 * <ul>
 *   <li>v1 → v3: copies 22 original values, fills v2/v3 fields with builder defaults.
 *   <li>v2 → v3: copies all 48 v2 fields, fills v3 fields with builder defaults.
 *   <li>v3+: returned unchanged (idempotent).
 * </ul>
 *
 * <p>This class is thread-safe and stateless.
 */
public final class ConfigMigrator {

    private ConfigMigrator() {
        // utility class
    }

    /**
     * Migrates a {@link GeoForgeConfig} to the latest version if needed.
     *
     * @param oldConfig the configuration to migrate; must not be null
     * @return the migrated configuration with configVersion = 3, or the same instance
     *         if it was already at v3 or higher
     */
    public static GeoForgeConfig migrate(GeoForgeConfig oldConfig) {
        if (oldConfig.configVersion() >= 3) {
            return oldConfig;
        }

        // v2 → v3: copy all existing v2 fields; new v3 fields get builder defaults
        if (oldConfig.configVersion() == 2) {
            return GeoForgeConfig.builder()
                    .minHeight(oldConfig.minHeight())
                    .maxHeight(oldConfig.maxHeight())
                    .seaLevel(oldConfig.seaLevel())
                    .continentalBase(oldConfig.continentalBase())
                    .continentalHeightAmplitude(oldConfig.continentalHeightAmplitude())
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
                    // Cave Y-envelope
                    .caveCenterY(oldConfig.caveCenterY())
                    .caveSpread(oldConfig.caveSpread())
                    .caveSurfaceCutoff(oldConfig.caveSurfaceCutoff())
                    .caveSpaghettiThreshold(oldConfig.caveSpaghettiThreshold())
                    .caveCheeseThreshold(oldConfig.caveCheeseThreshold())
                    .caveNoodleThreshold(oldConfig.caveNoodleThreshold())
                    .caveNoodleFrequency(oldConfig.caveNoodleFrequency())
                    // River v2
                    .riverCanyonDepth(oldConfig.riverCanyonDepth())
                    .riverCanyonWidth(oldConfig.riverCanyonWidth())
                    .riverValleyProfile(oldConfig.riverValleyProfile())
                    .riverFloodplainWidth(oldConfig.riverFloodplainWidth())
                    .riverTableResponse(oldConfig.riverTableResponse())
                    // Multi-noise terrain
                    .ridgeFrequency(oldConfig.ridgeFrequency())
                    .ridgeOctaves(oldConfig.ridgeOctaves())
                    .ridgeAmplitude(oldConfig.ridgeAmplitude())
                    .fbmFrequency(oldConfig.fbmFrequency())
                    .fbmOctaves(oldConfig.fbmOctaves())
                    .flatFrequency(oldConfig.flatFrequency())
                    .continentalnessBlendSharpness(oldConfig.continentalnessBlendSharpness())
                    // Noise backend
                    .noiseBackend(oldConfig.noiseBackend())
                    // Decorations
                    .treeDensity(oldConfig.treeDensity())
                    .vegetationDensity(oldConfig.vegetationDensity())
                    .featureSeedOffset(oldConfig.featureSeedOffset())
                    .maxTreeHeight(oldConfig.maxTreeHeight())
                    // Erosion
                    .erosionDropletCount(oldConfig.erosionDropletCount())
                    .erosionGravity(oldConfig.erosionGravity())
                    // Plateau
                    .plateauSize(oldConfig.plateauSize())
                    .plateauTargetHeight(oldConfig.plateauTargetHeight())
                    // Domain warping
                    .domainWarpAmplitude(oldConfig.domainWarpAmplitude())
                    // New v3 fields get builder defaults (minTreeHeight=4, treeDensityFrequency=0.02)
                    .configVersion(3)
                    .build();
        }

        // v1 → v3: copy only the 22 original v1 fields; v2+v3 fields get builder defaults
        return GeoForgeConfig.builder()
                .minHeight(oldConfig.minHeight())
                .maxHeight(oldConfig.maxHeight())
                .seaLevel(oldConfig.seaLevel())
                .continentalBase(oldConfig.continentalBase())
                .continentalHeightAmplitude(oldConfig.continentalHeightAmplitude())
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
