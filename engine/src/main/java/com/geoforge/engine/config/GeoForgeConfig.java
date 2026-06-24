package com.geoforge.engine.config;

/**
 * Immutable configuration record for all terrain generation tuning parameters.
 *
 * <p>Fields are grouped into five categories:
 *
 * <dl>
 *   <dt>Terrain bounds</dt>
 *   <dd>{@code minHeight}, {@code maxHeight}, {@code seaLevel} — define the vertical extent of
 *       the world and the target water level.
 *   <dt>Continental noise</dt>
 *   <dd>{@code continentalBase}, {@code continentalHeightAmplitude}, {@code continentalFrequency},
 *       {@code continentalOctaves}, {@code continentalLacunarity}, {@code continentalPersistence} —
 *       control the large-scale landmass shape via octaved noise.
 *   <dt>Climate noise</dt>
 *   <dd>{@code temperatureFrequency}, {@code temperatureYFrequency}, {@code humidityFrequency} —
 *       drive biome temperature and humidity sampling.
 *   <dt>Cave noise</dt>
 *   <dd>{@code caveFrequency}, {@code caveAmplitude}, {@code caveOctaves},
 *       {@code caveLacunarity}, {@code cavePersistence} — control 3D cave carving.
 *   <dt>Erosion</dt>
 *   <dd>{@code erosionMaxDropletSteps}, {@code erosionIterations} — bound the hydraulic erosion
 *       simulation.
 * </dl>
 *
 * <p>Use {@link #defaults()} to obtain a sensible starting configuration, or call the
 * convenience factories for testing.
 *
 * @param minHeight                  Minimum build height (Y level).
 * @param maxHeight                  Maximum build height (Y level). Must be {@code > minHeight}.
 * @param seaLevel                   Target sea level. Must be {@code >= minHeight && <= maxHeight}.
 * @param continentalBase            Base Y offset for continental noise.
 * @param continentalHeightAmplitude Amplitude of continental noise in blocks.
 * @param continentalFrequency       Base frequency of continental noise (higher = more smaller
 *                                   continents).
 * @param continentalOctaves         Number of octaves for fractal continental noise. Must be
 *                                   {@code > 0}.
 * @param continentalLacunarity      Frequency multiplier between octaves. Must be {@code > 0}.
 * @param continentalPersistence     Amplitude multiplier between octaves. Must be {@code > 0}.
 * @param temperatureFrequency       Horizontal frequency for temperature noise.
 * @param temperatureYFrequency      Vertical (Y-axis) frequency for temperature noise.
 * @param humidityFrequency          Horizontal frequency for humidity noise.
 * @param caveFrequency              Frequency for 3D cave carving noise.
 * @param caveAmplitude              Amplitude of cave carving in blocks (0 = no caves).
 * @param caveOctaves                Number of octaves for cave noise. Must be {@code > 0}.
 * @param caveLacunarity             Frequency multiplier between cave octaves. Must be
 *                                   {@code > 0}.
 * @param cavePersistence            Amplitude multiplier between cave octaves. Must be
 *                                   {@code > 0}.
 * @param erosionMaxDropletSteps     Maximum steps per erosion droplet. Must be {@code > 0}.
 * @param erosionIterations          Total erosion iterations per column. Must be {@code > 0}.
 */
public record GeoForgeConfig(
        int minHeight,
        int maxHeight,
        int seaLevel,
        double continentalBase,
        double continentalHeightAmplitude,
        double continentalFrequency,
        int continentalOctaves,
        double continentalLacunarity,
        double continentalPersistence,
        double temperatureFrequency,
        double temperatureYFrequency,
        double humidityFrequency,
        double caveFrequency,
        double caveAmplitude,
        int caveOctaves,
        double caveLacunarity,
        double cavePersistence,
        int erosionMaxDropletSteps,
        int erosionIterations) {

    /**
     * Compact canonical constructor validating all constraints.
     *
     * @throws IllegalArgumentException if any constraint is violated
     */
    public GeoForgeConfig {
        if (maxHeight <= minHeight) {
            throw new IllegalArgumentException(
                    "maxHeight (%d) must be > minHeight (%d)".formatted(maxHeight, minHeight));
        }
        if (seaLevel < minHeight || seaLevel > maxHeight) {
            throw new IllegalArgumentException(
                    "seaLevel (%d) must be between minHeight (%d) and maxHeight (%d)"
                            .formatted(seaLevel, minHeight, maxHeight));
        }
        if (continentalOctaves <= 0) {
            throw new IllegalArgumentException(
                    "continentalOctaves must be > 0, got %d".formatted(continentalOctaves));
        }
        if (continentalFrequency <= 0) {
            throw new IllegalArgumentException(
                    "continentalFrequency must be > 0, got %s"
                            .formatted(continentalFrequency));
        }
        if (continentalLacunarity <= 0) {
            throw new IllegalArgumentException(
                    "continentalLacunarity must be > 0, got %s"
                            .formatted(continentalLacunarity));
        }
        if (continentalPersistence <= 0) {
            throw new IllegalArgumentException(
                    "continentalPersistence must be > 0, got %s"
                            .formatted(continentalPersistence));
        }
        if (temperatureFrequency <= 0) {
            throw new IllegalArgumentException(
                    "temperatureFrequency must be > 0, got %s"
                            .formatted(temperatureFrequency));
        }
        if (temperatureYFrequency <= 0) {
            throw new IllegalArgumentException(
                    "temperatureYFrequency must be > 0, got %s"
                            .formatted(temperatureYFrequency));
        }
        if (humidityFrequency <= 0) {
            throw new IllegalArgumentException(
                    "humidityFrequency must be > 0, got %s"
                            .formatted(humidityFrequency));
        }
        if (caveOctaves <= 0) {
            throw new IllegalArgumentException(
                    "caveOctaves must be > 0, got %d".formatted(caveOctaves));
        }
        if (caveFrequency <= 0) {
            throw new IllegalArgumentException(
                    "caveFrequency must be > 0, got %s".formatted(caveFrequency));
        }
        if (caveLacunarity <= 0) {
            throw new IllegalArgumentException(
                    "caveLacunarity must be > 0, got %s".formatted(caveLacunarity));
        }
        if (cavePersistence <= 0) {
            throw new IllegalArgumentException(
                    "cavePersistence must be > 0, got %s".formatted(cavePersistence));
        }
        if (erosionMaxDropletSteps <= 0) {
            throw new IllegalArgumentException(
                    "erosionMaxDropletSteps must be > 0, got %d"
                            .formatted(erosionMaxDropletSteps));
        }
        if (erosionIterations <= 0) {
            throw new IllegalArgumentException(
                    "erosionIterations must be > 0, got %d".formatted(erosionIterations));
        }
    }

    /**
     * Returns a default configuration suitable for a standard overworld-like terrain.
     *
     * @return a new {@code GeoForgeConfig} with sensible defaults
     */
    public static GeoForgeConfig defaults() {
        return new GeoForgeConfig(
                -64,   // minHeight
                180,   // maxHeight
                63,    // seaLevel
                50.0,  // continentalBase
                120.0, // continentalHeightAmplitude
                0.004, // continentalFrequency
                4,     // continentalOctaves
                2.0,   // continentalLacunarity
                0.5,   // continentalPersistence
                0.001, // temperatureFrequency
                0.005, // temperatureYFrequency
                0.001, // humidityFrequency
                0.03,  // caveFrequency
                8.0,   // caveAmplitude
                2,     // caveOctaves
                2.0,   // caveLacunarity
                0.5,   // cavePersistence
                10,    // erosionMaxDropletSteps
                64     // erosionIterations
        );
    }

    /**
     * Returns a configuration identical to {@link #defaults()} but with the given sea level.
     *
     * <p>Convenience factory for tests that need a specific sea level without duplicating all
     * defaults.
     *
     * @param seaLevel the desired sea level
     * @return a new {@code GeoForgeConfig} with all default values except {@code seaLevel}
     */
    public static GeoForgeConfig withSeaLevel(int seaLevel) {
        return new GeoForgeConfig(
                -64,   // minHeight
                180,   // maxHeight
                seaLevel,
                50.0,  // continentalBase
                120.0, // continentalHeightAmplitude
                0.004, // continentalFrequency
                4,     // continentalOctaves
                2.0,   // continentalLacunarity
                0.5,   // continentalPersistence
                0.001, // temperatureFrequency
                0.005, // temperatureYFrequency
                0.001, // humidityFrequency
                0.03,  // caveFrequency
                8.0,   // caveAmplitude
                2,     // caveOctaves
                2.0,   // caveLacunarity
                0.5,   // cavePersistence
                10,    // erosionMaxDropletSteps
                64     // erosionIterations
        );
    }

    /**
     * Returns a configuration identical to {@link #defaults()} but with the given cave
     * amplitude.
     *
     * <p>Setting cave amplitude to 0 disables cave carving, which is useful for testing
     * surface height extraction against the 2D height function.
     *
     * @param caveAmplitude the desired cave amplitude (0 = no caves)
     * @return a new {@code GeoForgeConfig} with all default values except {@code caveAmplitude}
     */
    public static GeoForgeConfig withCaveAmplitude(double caveAmplitude) {
        return new GeoForgeConfig(
                -64,   // minHeight
                180,   // maxHeight
                63,    // seaLevel
                50.0,  // continentalBase
                120.0, // continentalHeightAmplitude
                0.004, // continentalFrequency
                4,     // continentalOctaves
                2.0,   // continentalLacunarity
                0.5,   // continentalPersistence
                0.001, // temperatureFrequency
                0.005, // temperatureYFrequency
                0.001, // humidityFrequency
                0.03,  // caveFrequency
                caveAmplitude,
                2,     // caveOctaves
                2.0,   // caveLacunarity
                0.5,   // cavePersistence
                10,    // erosionMaxDropletSteps
                64     // erosionIterations
        );
    }
}
