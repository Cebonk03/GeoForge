package com.geoforge.engine.config;

/**
 * Immutable configuration record for all terrain generation tuning parameters.
 *
 * <p>Fields are grouped into six categories:
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
 *   <dt>River carving</dt>
 *   <dd>{@code riverFrequency}, {@code riverDepth}, {@code riverWidth} — control river valley
 *       carving via 2D simplex noise moisture convergence.
 *   <dt>Erosion</dt>
 *   <dd>{@code erosionMaxDropletSteps}, {@code erosionIterations} — bound the hydraulic erosion
 *       simulation.
 * </dl>
 *
 * <p>Use {@link #defaults()} to obtain a sensible starting configuration, or use the
 * {@link Builder} for fine-grained control.
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
 * @param riverFrequency             Frequency for 2D river carving noise. Must be {@code > 0}.
 * @param riverDepth                 Maximum depth of river carving in blocks. Must be {@code > 0}.
 * @param riverWidth                 River width parameter (higher = wider rivers). Must be {@code > 0}.
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
        double riverFrequency,
        int riverDepth,
        int riverWidth,
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
        if (riverFrequency <= 0) {
            throw new IllegalArgumentException(
                    "riverFrequency must be > 0, got %s"
                            .formatted(riverFrequency));
        }
        if (riverDepth <= 0) {
            throw new IllegalArgumentException(
                    "riverDepth must be > 0, got %d".formatted(riverDepth));
        }
        if (riverWidth <= 0) {
            throw new IllegalArgumentException(
                    "riverWidth must be > 0, got %d".formatted(riverWidth));
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
                0.01,  // riverFrequency
                8,     // riverDepth
                3,     // riverWidth
                10,    // erosionMaxDropletSteps
                64     // erosionIterations
        );
    }

    /**
     * Returns a new {@link Builder} initialized with the default values.
     *
     * @return a builder pre-populated with default parameter values
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Mutable builder for creating {@link GeoForgeConfig} instances.
     *
     * <p>All fields are pre-populated with the same defaults as
     * {@link GeoForgeConfig#defaults()}. Call the wither methods to override
     * specific parameters, then {@link #build()} to produce the immutable record.
     */
    public static final class Builder {
        private int minHeight = -64;
        private int maxHeight = 180;
        private int seaLevel = 63;
        private double continentalBase = 50.0;
        private double continentalHeightAmplitude = 120.0;
        private double continentalFrequency = 0.004;
        private int continentalOctaves = 4;
        private double continentalLacunarity = 2.0;
        private double continentalPersistence = 0.5;
        private double temperatureFrequency = 0.001;
        private double temperatureYFrequency = 0.005;
        private double humidityFrequency = 0.001;
        private double caveFrequency = 0.03;
        private double caveAmplitude = 8.0;
        private int caveOctaves = 2;
        private double caveLacunarity = 2.0;
        private double cavePersistence = 0.5;
        private double riverFrequency = 0.01;
        private int riverDepth = 8;
        private int riverWidth = 3;
        private int erosionMaxDropletSteps = 10;
        private int erosionIterations = 64;

        private Builder() {}

        public Builder minHeight(int minHeight) { this.minHeight = minHeight; return this; }
        public Builder maxHeight(int maxHeight) { this.maxHeight = maxHeight; return this; }
        public Builder seaLevel(int seaLevel) { this.seaLevel = seaLevel; return this; }
        public Builder continentalBase(double continentalBase) { this.continentalBase = continentalBase; return this; }
        public Builder continentalHeightAmplitude(double continentalHeightAmplitude) { this.continentalHeightAmplitude = continentalHeightAmplitude; return this; }
        public Builder continentalFrequency(double continentalFrequency) { this.continentalFrequency = continentalFrequency; return this; }
        public Builder continentalOctaves(int continentalOctaves) { this.continentalOctaves = continentalOctaves; return this; }
        public Builder continentalLacunarity(double continentalLacunarity) { this.continentalLacunarity = continentalLacunarity; return this; }
        public Builder continentalPersistence(double continentalPersistence) { this.continentalPersistence = continentalPersistence; return this; }
        public Builder temperatureFrequency(double temperatureFrequency) { this.temperatureFrequency = temperatureFrequency; return this; }
        public Builder temperatureYFrequency(double temperatureYFrequency) { this.temperatureYFrequency = temperatureYFrequency; return this; }
        public Builder humidityFrequency(double humidityFrequency) { this.humidityFrequency = humidityFrequency; return this; }
        public Builder caveFrequency(double caveFrequency) { this.caveFrequency = caveFrequency; return this; }
        public Builder caveAmplitude(double caveAmplitude) { this.caveAmplitude = caveAmplitude; return this; }
        public Builder caveOctaves(int caveOctaves) { this.caveOctaves = caveOctaves; return this; }
        public Builder caveLacunarity(double caveLacunarity) { this.caveLacunarity = caveLacunarity; return this; }
        public Builder cavePersistence(double cavePersistence) { this.cavePersistence = cavePersistence; return this; }
        public Builder riverFrequency(double riverFrequency) { this.riverFrequency = riverFrequency; return this; }
        public Builder riverDepth(int riverDepth) { this.riverDepth = riverDepth; return this; }
        public Builder riverWidth(int riverWidth) { this.riverWidth = riverWidth; return this; }
        public Builder erosionMaxDropletSteps(int erosionMaxDropletSteps) { this.erosionMaxDropletSteps = erosionMaxDropletSteps; return this; }
        public Builder erosionIterations(int erosionIterations) { this.erosionIterations = erosionIterations; return this; }

        /**
         * Builds the immutable {@link GeoForgeConfig} record.
         *
         * @return a new GeoForgeConfig with the configured values
         */
        public GeoForgeConfig build() {
            return new GeoForgeConfig(
                    minHeight, maxHeight, seaLevel,
                    continentalBase, continentalHeightAmplitude,
                    continentalFrequency, continentalOctaves,
                    continentalLacunarity, continentalPersistence,
                    temperatureFrequency, temperatureYFrequency,
                    humidityFrequency,
                    caveFrequency, caveAmplitude, caveOctaves,
                    caveLacunarity, cavePersistence,
                    riverFrequency, riverDepth, riverWidth,
                    erosionMaxDropletSteps, erosionIterations);
        }
    }
}
