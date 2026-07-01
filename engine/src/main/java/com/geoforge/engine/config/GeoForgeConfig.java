package com.geoforge.engine.config;


/**
 * Immutable configuration record for all terrain generation tuning parameters.
 *
 * <p>Fields are grouped into ten categories:
 *
 * <dl>
 *   <dt>Terrain bounds</dt>
 *   <dd>{@code minHeight}, {@code maxHeight}, {@code seaLevel} — define the vertical extent of
 *       the world and the target water level.
 *   <dt>Continental noise</dt>
 *   <dd>{@code continentalBase}, {@code continentalHeightAmplitude} —
 *       control the large-scale landmass shape via octaved noise.
 *   <dt>Climate noise</dt>
 *   <dd>{@code temperatureFrequency}, {@code temperatureYFrequency}, {@code humidityFrequency} —
 *       drive biome temperature and humidity sampling.
 *   <dt>Cave noise</dt>
 *   <dd>{@code caveFrequency}, {@code caveAmplitude}, {@code caveOctaves},
 *       {@code caveLacunarity}, {@code cavePersistence} — control 3D cave carving.
 *   <dt>Cave Y-envelope</dt>
 *   <dd>{@code caveCenterY}, {@code caveSpread}, {@code caveSurfaceCutoff},
 *       {@code caveSpaghettiThreshold}, {@code caveCheeseThreshold}, {@code caveNoodleThreshold},
 *       {@code caveNoodleFrequency} — control vertical cave distribution and cave shape variants.
 *   <dt>River carving</dt>
 *   <dd>{@code riverFrequency}, {@code riverDepth}, {@code riverWidth} — control river valley
 *       carving via 2D simplex noise moisture convergence.</dd>
 *   <dd>{@code riverCanyonDepth}, {@code riverCanyonWidth}, {@code riverValleyProfile},
 *       {@code riverFloodplainWidth}, {@code riverTableResponse} — extended river v2 parameters.
 *   <dt>Multi-noise terrain</dt>
 *   <dd>{@code ridgeFrequency}, {@code ridgeOctaves}, {@code ridgeAmplitude},
 *       {@code fbmFrequency}, {@code fbmOctaves}, {@code flatFrequency},
 *       {@code continentalnessBlendSharpness} — multi-octave blending for varied terrain shapes.
 *   <dt>Decorations</dt>
 *   <dd>{@code featureSeedOffset}, {@code treeDensityFrequency} — control surface decoration placement.
 *   <dt>Erosion</dt>
 *   <dd>{@code erosionMaxDropletSteps}, {@code erosionIterations} — bound the hydraulic erosion
 *       simulation.</dd>
 *   <dd>{@code erosionDropletCount}, {@code erosionGravity} — extended erosion parameters.
 *   <dt>Domain warping</dt>
 *   <dd>{@code domainWarpAmplitude} — amplitude of domain-warping noise for terrain distortion.
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
 * @param riverDepth                 Maximum depth of river carving in blocks. {@code 0} disables river carving.
 * @param riverWidth                 River width parameter (higher = wider rivers). Must be {@code > 0}.
 * @param erosionMaxDropletSteps     Maximum steps per erosion droplet. Must be {@code > 0}.
 * @param erosionIterations          Total erosion iterations per column. Must be {@code > 0}.
 * @param caveCenterY                Center Y level for cave distribution.
 * @param caveSpread                 Vertical spread of cave generation. Must be {@code > 0}.
 * @param caveSurfaceCutoff          Surface cutoff for cave generation off at top. Must be {@code >= 0}.
 * @param caveSpaghettiThreshold     Threshold for spaghetti cave variant selection.
 * @param caveCheeseThreshold        Threshold for cheese cave variant selection.
 * @param caveNoodleThreshold        Threshold for noodle cave variant selection.
 * @param caveNoodleFrequency        Frequency for noodle cave noise.
 * @param riverCanyonDepth           Depth of river canyon carving. {@code 0} disables canyon.
 * @param riverCanyonWidth           Width of river canyon blocks.
 * @param riverValleyProfile         Valley profile shape (VSHAPED, CANYON, or FLOODPLAIN).
 * @param riverFloodplainWidth       Width of river floodplain in blocks.
 * @param riverTableResponse         Response factor for river water table adjustment.
 * @param ridgeFrequency             Base frequency for ridge noise.
 * @param ridgeOctaves               Number of octaves for ridge noise. Must be {@code > 0}.
 * @param ridgeAmplitude             Amplitude for ridge noise.
 * @param fbmFrequency               Base frequency for FBM noise.
 * @param fbmOctaves                 Number of octaves for FBM noise. Must be {@code > 0}.
 * @param flatFrequency              Base frequency for flat-region noise.
 * @param continentalnessBlendSharpness     Sharpness of continentalness blending transition.
 * @param featureSeedOffset          Seed offset for feature placement.
 * @param erosionDropletCount        Number of erosion droplets per column.
 * @param erosionGravity             Gravity factor for erosion droplet simulation.
 * @param noiseBackend               Noise backend implementation ("gradient", "simplex" (deprecated), or "fastnoise").
 * @param plateauSize                Side length of flattened plateau region in blocks (0 = disabled). Must be {@code >= 0}.
 * @param plateauTargetHeight        Target height for plateau flattening in blocks. Must be within {@code [minHeight, maxHeight]}.
 * @param domainWarpAmplitude        Amplitude of domain-warping noise distortion.
 * @param boundaryWarpFrequency    Frequency of noise used to warp continentalness at biome boundaries.
 *                                 Higher = more frequent boundary shifts. 0.001 = smooth, large-scale warps.
 * @param boundaryWarpAmplitude    Amplitude of boundary warp in continentalness units (0-1 range).
 *                                 0.15 shifts boundaries by ~15% of the continentalness range,
 *                                 creating organic, non-detectable biome borders.
 * @param caveWarpAmplitude        Amplitude of coordinate warping for cave noise (0 = no warping).
 *                                 Adds organic twisting to cave tunnels. Default: 0.
 * @param noodleWarpAmplitude      Amplitude of coordinate warping for noodle cave noise (0 = no warping).
 *                                 Default: 0.
 * @param riverWarpAmplitude       Amplitude of coordinate warping for river noise (0 = no warping).
 *                                 Creates natural river meanders. Default: 0.
 * @param treeDensityFrequency    Frequency of tree density noise for placement. Must be {@code > 0}.
 * @param configVersion              Configuration version for migration support.
 */
public record GeoForgeConfig(
        int minHeight,
        int maxHeight,
        int seaLevel,
        double continentalBase,
        double continentalHeightAmplitude,
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
        int erosionIterations,
        // --- Cave Y-envelope ---
        double caveCenterY,
        double caveSpread,
        double caveSurfaceCutoff,
        double caveSpaghettiThreshold,
        double caveCheeseThreshold,
        double caveNoodleThreshold,
        double caveNoodleFrequency,
        // --- River v2 ---
        int riverCanyonDepth,
        int riverCanyonWidth,
        RiverProfile riverValleyProfile,
        int riverFloodplainWidth,
        double riverTableResponse,
        // --- Multi-noise terrain ---
        double ridgeFrequency,
        int ridgeOctaves,
        double ridgeAmplitude,
        double fbmFrequency,
        int fbmOctaves,
        double flatFrequency,
        double continentalnessBlendSharpness,
        // --- Noise backend ---
        String noiseBackend,
        // --- Decorations ---
        long featureSeedOffset,
        // --- Erosion ---
        int erosionDropletCount,
        float erosionGravity,
        // --- Plateau (structure flattening) ---
        int plateauSize,
        int plateauTargetHeight,
        // --- Domain warping ---
        double domainWarpAmplitude,
        // --- Boundary warp (biome border transitions) ---
        double boundaryWarpFrequency,
        double boundaryWarpAmplitude,
        // --- Domain warping amplitudes (noise decorators) ---
        double caveWarpAmplitude,
        double noodleWarpAmplitude,
        double riverWarpAmplitude,
        // --- Tree config ---
        double treeDensityFrequency,
        // --- Config version ---
        int configVersion) {

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
        if (riverDepth < 0) {
            throw new IllegalArgumentException(
                    "riverDepth must be >= 0, got %d".formatted(riverDepth));
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
        // Cave Y-envelope validation
        if (caveSpread <= 0) {
            throw new IllegalArgumentException(
                    "caveSpread must be > 0, got %s".formatted(caveSpread));
        }
        if (caveSurfaceCutoff < 0) {
            throw new IllegalArgumentException(
                    "caveSurfaceCutoff must be >= 0, got %s".formatted(caveSurfaceCutoff));
        }
        if (caveNoodleFrequency <= 0) {
            throw new IllegalArgumentException(
                    "caveNoodleFrequency must be > 0, got %s"
                            .formatted(caveNoodleFrequency));
        }
        if (riverValleyProfile == null) {
            throw new IllegalArgumentException("riverValleyProfile must not be null");
        }
        if (riverCanyonDepth < 0) {
            throw new IllegalArgumentException(
                    "riverCanyonDepth must be >= 0, got %d".formatted(riverCanyonDepth));
        }
        if (riverCanyonWidth < 1) {
            throw new IllegalArgumentException(
                    "riverCanyonWidth must be >= 1, got %d".formatted(riverCanyonWidth));
        }
        if (riverFloodplainWidth < 1) {
            throw new IllegalArgumentException(
                    "riverFloodplainWidth must be >= 1, got %d".formatted(riverFloodplainWidth));
        }
        if (riverTableResponse < 0.0) {
            throw new IllegalArgumentException(
                    "riverTableResponse must be >= 0.0, got %s"
                            .formatted(riverTableResponse));
        }
        // Multi-noise terrain validation
        if (ridgeFrequency <= 0) {
            throw new IllegalArgumentException(
                    "ridgeFrequency must be > 0, got %s".formatted(ridgeFrequency));
        }
        if (ridgeOctaves <= 0) {
            throw new IllegalArgumentException(
                    "ridgeOctaves must be > 0, got %d".formatted(ridgeOctaves));
        }
        if (fbmFrequency <= 0) {
            throw new IllegalArgumentException(
                    "fbmFrequency must be > 0, got %s".formatted(fbmFrequency));
        }
        if (fbmOctaves <= 0) {
            throw new IllegalArgumentException(
                    "fbmOctaves must be > 0, got %d".formatted(fbmOctaves));
        }
        if (flatFrequency <= 0) {
            throw new IllegalArgumentException(
                    "flatFrequency must be > 0, got %s".formatted(flatFrequency));
        }
        if (ridgeAmplitude < 0) {
            throw new IllegalArgumentException(
                    "ridgeAmplitude must be >= 0, got %s".formatted(ridgeAmplitude));
        }
        if (continentalnessBlendSharpness < 0.1) {
            throw new IllegalArgumentException(
                    "continentalnessBlendSharpness must be >= 0.1, got %s"
                            .formatted(continentalnessBlendSharpness));
        }
        // Erosion validation
        if (erosionDropletCount < 0) {
            throw new IllegalArgumentException(
                    "erosionDropletCount must be >= 0, got %d".formatted(erosionDropletCount));
        }
        if (erosionGravity <= 0) {
            throw new IllegalArgumentException(
                    "erosionGravity must be > 0, got %s".formatted(erosionGravity));
        }
        // Plateau validation
        if (plateauSize < 0) {
            throw new IllegalArgumentException(
                    "plateauSize must be >= 0, got %d".formatted(plateauSize));
        }
        if (plateauTargetHeight < minHeight || plateauTargetHeight > maxHeight) {
            throw new IllegalArgumentException(
                    "plateauTargetHeight (%d) must be between minHeight (%d) and maxHeight (%d)"
                            .formatted(plateauTargetHeight, minHeight, maxHeight));
        }
        // Noise backend validation
        if (!"simplex".equals(noiseBackend) && !"gradient".equals(noiseBackend) && !"fastnoise".equals(noiseBackend)) {
            throw new IllegalArgumentException(
                    "noiseBackend must be 'simplex', 'gradient', or 'fastnoise', got '" + noiseBackend + "'");
        }
        // Domain warping validation
        if (domainWarpAmplitude < 0) {
            throw new IllegalArgumentException(
                    "domainWarpAmplitude must be >= 0, got %s"
                            .formatted(domainWarpAmplitude));
        }
        if (boundaryWarpFrequency <= 0) {
            throw new IllegalArgumentException(
                    "boundaryWarpFrequency must be > 0, got %s"
                            .formatted(boundaryWarpFrequency));
        }
        if (boundaryWarpAmplitude < 0) {
            throw new IllegalArgumentException(
                    "boundaryWarpAmplitude must be >= 0, got %s"
                            .formatted(boundaryWarpAmplitude));
        }
        if (caveWarpAmplitude < 0) {
            throw new IllegalArgumentException(
                    "caveWarpAmplitude must be >= 0, got %s"
.formatted(caveWarpAmplitude));
        }
        if (noodleWarpAmplitude < 0) {
            throw new IllegalArgumentException(
                    "noodleWarpAmplitude must be >= 0, got %s"
.formatted(noodleWarpAmplitude));
        }
        if (riverWarpAmplitude < 0) {
            throw new IllegalArgumentException(
                    "riverWarpAmplitude must be >= 0, got %s"
.formatted(riverWarpAmplitude));
        }
        if (treeDensityFrequency <= 0) {
            throw new IllegalArgumentException(
                    "treeDensityFrequency must be > 0, got %s"
                            .formatted(treeDensityFrequency));
        }
        // Config version validation
        if (configVersion < 0) {
            throw new IllegalArgumentException(
                    "configVersion must be >= 0, got %d".formatted(configVersion));
        }
    }

    /**
     * Checks configuration sanity and returns a list of warnings for extreme or unusual
     * parameter combinations that may produce unexpected terrain.
     *
     * <p>Unlike the canonical constructor (which throws on invalid values), this method
     * logs warnings and returns gracefully — it never throws. Use it at engine construction
     * time to alert users of potentially problematic settings.
     *
     * @return a list of human-readable warning messages (empty if all checks pass)
     */
    public java.util.List<String> sanityCheck() {
        var warnings = new java.util.ArrayList<String>();
        if (caveAmplitude > maxHeight - minHeight) {
            warnings.add("caveAmplitude (" + caveAmplitude
                    + ") exceeds world height (" + (maxHeight - minHeight)
                    + ") — caves will dominate terrain");
        }
        if (continentalHeightAmplitude < 10) {
            warnings.add("continentalHeightAmplitude very low (" + continentalHeightAmplitude
                    + ") — terrain will be nearly flat");
        }
        if (seaLevel < 50 || seaLevel > 70) {
            warnings.add("seaLevel (" + seaLevel + ") outside typical range [50, 70]");
        }
        if (caveSpread < 8) {
            warnings.add("caveSpread (" + caveSpread
                    + ") very narrow — caves will be highly concentrated");
        }
        if (caveSurfaceCutoff > 32) {
            warnings.add("caveSurfaceCutoff (" + caveSurfaceCutoff
                    + ") very large — caves may appear unusually close to surface");
        }
        if (riverDepth > caveAmplitude * 4) {
            warnings.add("riverDepth (" + riverDepth + ") very large relative to caveAmplitude ("
                    + caveAmplitude + ") — rivers may cut through caves");
        }
        if (plateauSize > 0 && configVersion < 2) {
            warnings.add("plateauSize=" + plateauSize
                    + " but configVersion=" + configVersion
                    + " — plateau flattening requires configVersion >= 2");
        }
        if (domainWarpAmplitude > maxHeight - minHeight) {
            warnings.add("domainWarpAmplitude (" + domainWarpAmplitude
                    + ") exceeds world height — terrain will be severely distorted");
        }
        if (treeDensityFrequency > 0.1) {
            warnings.add("treeDensityFrequency (" + treeDensityFrequency
                    + ") very high — tree placement may be too concentrated");
        }
        return warnings;
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
                0.001, // temperatureFrequency
                0.005, // temperatureYFrequency
                0.001, // humidityFrequency
                0.03,  // caveFrequency
                8.0,   // caveAmplitude
                3,     // caveOctaves
                2.0,   // caveLacunarity
                0.5,   // cavePersistence
                0.01,  // riverFrequency
                8,     // riverDepth
                3,     // riverWidth
                10,    // erosionMaxDropletSteps
                64,    // erosionIterations
                // Cave Y-envelope
                -20.0, // caveCenterY
                48.0,  // caveSpread
                8.0,   // caveSurfaceCutoff
                0.3,   // caveSpaghettiThreshold
                0.5,   // caveCheeseThreshold
                0.15,  // caveNoodleThreshold
                0.05,  // caveNoodleFrequency
                // River v2
                0,     // riverCanyonDepth
                2,     // riverCanyonWidth
                RiverProfile.VSHAPED, // riverValleyProfile
                5,     // riverFloodplainWidth
                0.0,   // riverTableResponse
                // Multi-noise terrain
                0.003, // ridgeFrequency
                3,     // ridgeOctaves
                1.0,   // ridgeAmplitude
                0.005, // fbmFrequency
                4,     // fbmOctaves
                0.008, // flatFrequency
                1.0,   // continentalnessBlendSharpness
                "gradient", // noiseBackend
                // Decorations
                0xCAFEBABEL, // featureSeedOffset
                // Erosion
                1024,  // erosionDropletCount
                0.2f,  // erosionGravity
                // Plateau (structure flattening)
                0,     // plateauSize (0 = disabled)
                64,    // plateauTargetHeight
                // Domain warping
                1.5,   // domainWarpAmplitude
                // Boundary warp for organic biome border transitions
                0.001, // boundaryWarpFrequency
                0.15,  // boundaryWarpAmplitude
                // Domain warping amplitudes (noise decorators): 0 = disabled
                0.0,   // caveWarpAmplitude
                0.0,   // noodleWarpAmplitude
                0.0,   // riverWarpAmplitude
                // Tree config
                0.02,  // treeDensityFrequency
                // Config version — bumped to 4 for T2 (caveOctaves 2->3), T7 (biome borders widened)
                4      // configVersion (was 3)
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
        private double temperatureFrequency = 0.001;
        private double temperatureYFrequency = 0.005;
        private double humidityFrequency = 0.001;
        private double caveFrequency = 0.03;
        private double caveAmplitude = 8.0;
        private int caveOctaves = 3;
        private double caveLacunarity = 2.0;
        private double cavePersistence = 0.5;
        private double riverFrequency = 0.01;
        private int riverDepth = 8;
        private int riverWidth = 3;
        private int erosionMaxDropletSteps = 10;
        private int erosionIterations = 64;
        // Cave Y-envelope
        private double caveCenterY = -20.0;
        private double caveSpread = 48.0;
        private double caveSurfaceCutoff = 8.0;
        private double caveSpaghettiThreshold = 0.3;
        private double caveCheeseThreshold = 0.5;
        private double caveNoodleThreshold = 0.15;
        private double caveNoodleFrequency = 0.05;
        // River v2
        private int riverCanyonDepth = 0;
        private int riverCanyonWidth = 2;
        private RiverProfile riverValleyProfile = RiverProfile.VSHAPED;
        private int riverFloodplainWidth = 5;
        private double riverTableResponse = 0.0;
        // Multi-noise terrain
        private double ridgeFrequency = 0.003;
        private int ridgeOctaves = 3;
        private double ridgeAmplitude = 1.0;
        private double fbmFrequency = 0.005;
        private int fbmOctaves = 4;
        private double flatFrequency = 0.008;
        private double continentalnessBlendSharpness = 1.0;
        // Noise backend
        private String noiseBackend = "gradient";
        // Decorations
        private long featureSeedOffset = 0xCAFEBABEL;
        // Erosion
        private int erosionDropletCount = 1024;
        private float erosionGravity = 0.2f;
        // Plateau (structure flattening)
        private int plateauSize = 0;
        private int plateauTargetHeight = 64;
        // Domain warping
        private double domainWarpAmplitude = 1.5;
        // Boundary warp (biome border transitions)
        private double boundaryWarpFrequency = 0.001;
        private double boundaryWarpAmplitude = 0.15;
        // Domain warping amplitudes (noise decorators): 0 = disabled
        private double caveWarpAmplitude = 0.0;
        private double noodleWarpAmplitude = 0.0;
        private double riverWarpAmplitude = 0.0;
        // Tree config
        private double treeDensityFrequency = 0.02;
        // Config version
        private int configVersion = 4;

        private Builder() {}

        public Builder minHeight(int minHeight) { this.minHeight = minHeight; return this; }
        public Builder maxHeight(int maxHeight) { this.maxHeight = maxHeight; return this; }
        public Builder seaLevel(int seaLevel) { this.seaLevel = seaLevel; return this; }
        public Builder continentalBase(double continentalBase) { this.continentalBase = continentalBase; return this; }
        public Builder continentalHeightAmplitude(double continentalHeightAmplitude) { this.continentalHeightAmplitude = continentalHeightAmplitude; return this; }
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
        // Cave Y-envelope
        public Builder caveCenterY(double caveCenterY) { this.caveCenterY = caveCenterY; return this; }
        public Builder caveSpread(double caveSpread) { this.caveSpread = caveSpread; return this; }
        public Builder caveSurfaceCutoff(double caveSurfaceCutoff) { this.caveSurfaceCutoff = caveSurfaceCutoff; return this; }
        public Builder caveSpaghettiThreshold(double caveSpaghettiThreshold) { this.caveSpaghettiThreshold = caveSpaghettiThreshold; return this; }
        public Builder caveCheeseThreshold(double caveCheeseThreshold) { this.caveCheeseThreshold = caveCheeseThreshold; return this; }
        public Builder caveNoodleThreshold(double caveNoodleThreshold) { this.caveNoodleThreshold = caveNoodleThreshold; return this; }
        public Builder caveNoodleFrequency(double caveNoodleFrequency) { this.caveNoodleFrequency = caveNoodleFrequency; return this; }
        // River v2
        public Builder riverCanyonDepth(int riverCanyonDepth) { this.riverCanyonDepth = riverCanyonDepth; return this; }
        public Builder riverCanyonWidth(int riverCanyonWidth) { this.riverCanyonWidth = riverCanyonWidth; return this; }
        public Builder riverValleyProfile(RiverProfile riverValleyProfile) { this.riverValleyProfile = riverValleyProfile; return this; }
        public Builder riverFloodplainWidth(int riverFloodplainWidth) { this.riverFloodplainWidth = riverFloodplainWidth; return this; }
        public Builder riverTableResponse(double riverTableResponse) { this.riverTableResponse = riverTableResponse; return this; }
        // Multi-noise terrain
        public Builder ridgeFrequency(double ridgeFrequency) { this.ridgeFrequency = ridgeFrequency; return this; }
        public Builder ridgeOctaves(int ridgeOctaves) { this.ridgeOctaves = ridgeOctaves; return this; }
        public Builder ridgeAmplitude(double ridgeAmplitude) { this.ridgeAmplitude = ridgeAmplitude; return this; }
        public Builder fbmFrequency(double fbmFrequency) { this.fbmFrequency = fbmFrequency; return this; }
        public Builder fbmOctaves(int fbmOctaves) { this.fbmOctaves = fbmOctaves; return this; }
        public Builder flatFrequency(double flatFrequency) { this.flatFrequency = flatFrequency; return this; }
        public Builder continentalnessBlendSharpness(double continentalnessBlendSharpness) { this.continentalnessBlendSharpness = continentalnessBlendSharpness; return this; }
        // Noise backend
        public Builder noiseBackend(String noiseBackend) { this.noiseBackend = noiseBackend; return this; }
        // Decorations
        public Builder featureSeedOffset(long featureSeedOffset) { this.featureSeedOffset = featureSeedOffset; return this; }
        // Erosion
        public Builder erosionDropletCount(int erosionDropletCount) { this.erosionDropletCount = erosionDropletCount; return this; }
        public Builder erosionGravity(float erosionGravity) { this.erosionGravity = erosionGravity; return this; }
        // Plateau (structure flattening)
        public Builder plateauSize(int plateauSize) { this.plateauSize = plateauSize; return this; }
        public Builder plateauTargetHeight(int plateauTargetHeight) { this.plateauTargetHeight = plateauTargetHeight; return this; }
        // Domain warping
        public Builder domainWarpAmplitude(double domainWarpAmplitude) { this.domainWarpAmplitude = domainWarpAmplitude; return this; }
        public Builder boundaryWarpFrequency(double boundaryWarpFrequency) { this.boundaryWarpFrequency = boundaryWarpFrequency; return this; }
        public Builder boundaryWarpAmplitude(double boundaryWarpAmplitude) { this.boundaryWarpAmplitude = boundaryWarpAmplitude; return this; }
        public Builder caveWarpAmplitude(double caveWarpAmplitude) { this.caveWarpAmplitude = caveWarpAmplitude; return this; }
        public Builder noodleWarpAmplitude(double noodleWarpAmplitude) { this.noodleWarpAmplitude = noodleWarpAmplitude; return this; }
        public Builder riverWarpAmplitude(double riverWarpAmplitude) { this.riverWarpAmplitude = riverWarpAmplitude; return this; }
        // Tree config
        public Builder treeDensityFrequency(double treeDensityFrequency) { this.treeDensityFrequency = treeDensityFrequency; return this; }
        // Config version
        public Builder configVersion(int configVersion) { this.configVersion = configVersion; return this; }

        /**
         * Builds the immutable {@link GeoForgeConfig} record.
         *
         * @return a new GeoForgeConfig with the configured values
         */
        public GeoForgeConfig build() {
            return new GeoForgeConfig(
                    minHeight, maxHeight, seaLevel,
                    continentalBase, continentalHeightAmplitude,
                    temperatureFrequency, temperatureYFrequency,
                    humidityFrequency,
                    caveFrequency, caveAmplitude, caveOctaves,
                    caveLacunarity, cavePersistence,
                    riverFrequency, riverDepth, riverWidth,
                    erosionMaxDropletSteps, erosionIterations,
                    // Cave Y-envelope
                    caveCenterY, caveSpread, caveSurfaceCutoff,
                    caveSpaghettiThreshold, caveCheeseThreshold,
                    caveNoodleThreshold, caveNoodleFrequency,
                    // River v2
                    riverCanyonDepth, riverCanyonWidth, riverValleyProfile,
                    riverFloodplainWidth, riverTableResponse,
                    // Multi-noise terrain
                    ridgeFrequency, ridgeOctaves, ridgeAmplitude,
                    fbmFrequency, fbmOctaves, flatFrequency,
                    continentalnessBlendSharpness,
                    // Noise backend
                    noiseBackend,
                    // Decorations
                    featureSeedOffset,
                    // Erosion
                    erosionDropletCount, erosionGravity,
                    // Plateau (structure flattening)
                    plateauSize, plateauTargetHeight,
                    // Domain warping
                    domainWarpAmplitude,
                    // Boundary warp
                    boundaryWarpFrequency, boundaryWarpAmplitude,
                    // Domain warping amplitudes
                    caveWarpAmplitude, noodleWarpAmplitude, riverWarpAmplitude,
                    treeDensityFrequency,
                    // Config version
                    configVersion);
        }
    }
}
