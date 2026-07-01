package com.geoforge.engine;

import com.geoforge.engine.noise.NoiseSource;

import com.geoforge.engine.util.DensityGuard;
import com.geoforge.engine.config.GeoForgeConfig;
import com.geoforge.engine.config.biome.ClimateResolver;
import com.geoforge.engine.config.biome.BiomeDefinition;
import com.geoforge.engine.config.biome.GeoForgeBiomeDefaults;
import com.geoforge.engine.config.biome.BiomeRegistry;
import com.geoforge.engine.density.AddDensity;
import com.geoforge.engine.density.ClampDensity;
import com.geoforge.engine.density.ConstantDensity;
import com.geoforge.engine.density.DensityFunctionTree;
import com.geoforge.engine.density.MultiplyDensity;
import com.geoforge.engine.density.PlateContinentalness;
import com.geoforge.engine.density.RiverCarver;
import com.geoforge.engine.density.NoopRiverCarver;
import com.geoforge.engine.density.SimplexRiverCarver;
import com.geoforge.engine.density.CanyonRiverCarver;
import com.geoforge.engine.density.FloodplainRiverCarver;
import com.geoforge.engine.density.DomainWarpDensity;
import com.geoforge.engine.geology.HydraulicErosion;
import com.geoforge.engine.geology.TectonicPlateMapper;
import com.geoforge.engine.density.MultiNoiseHeightFunction;
import com.geoforge.engine.feature.ScenicFeatureDetector;
import com.geoforge.engine.noise.FractalNoise;
import com.geoforge.engine.noise.GradientNoise;
import com.geoforge.engine.noise.FastNoiseLiteSource;
import com.geoforge.engine.plateau.StructurePlateauModifier;
import java.util.Set;
import com.geoforge.engine.biome.BiomeTerrainConfig;
import com.geoforge.engine.density.EnhancedCaveSystem;
import java.util.Map;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The core world generation engine for GeoForge.
 *
 * <p>Generates terrain density and biome assignments using multi-octave fractal noise
 * modulated by tectonic plate continentalness and 3D cave noise. This class has zero
 * Bukkit dependencies and is safe to use in any context.
 *
 * <p>The density field is computed as: {@code density(x,y,z) = heightFunc(x,z) - y + caveNoise(x,y,z)}.
 * Positive density = solid block, negative density = air.
 *
 * <p>Hydraulic erosion may be applied as a post-process step (see {@link #erodeColumn(float[], int, int, int, long)}).
 */
public final class GeoForgeEngine {

    // Boundary warp params come from config.boundaryWarpFrequency/Amplitude

    private static final Logger LOG = Logger.getLogger(GeoForgeEngine.class.getName());
    private final GeoForgeConfig config;
    private final NoiseSource temperatureNoise;
    private final NoiseSource humidityNoise;
    private final TectonicPlateMapper plateMapper;
    private final NoiseSource boundaryWarpNoise;
    private final DensityFunctionTree heightFunction;
    private final ClimateResolver climateResolver;
    private final HydraulicErosion erosion;
    private final FractalNoise caveNoise;
    private final RiverCarver riverCarver;
    private final NoiseSource noodleNoise;
    private volatile BiomeRegistry biomeRegistry;
    private final DensityFunctionTree domainWarpedHeight;
    private final ScenicFeatureDetector scenicDetector;


    /**
     * Creates a new engine with the given world seed and configuration.
     *
     * @param seed   the world generation seed
     * @param config the configuration for all terrain tuning parameters
     */
    public GeoForgeEngine(long seed, GeoForgeConfig config) {
        this.config = config;
        this.plateMapper = new TectonicPlateMapper(seed ^ 0xDEADBEEFL);

        // Multi-noise terrain: ridge (mountains, multi-octave), FBM (hills, multi-octave), flat (plains/oceans, single-octave)
        var ridgeNoise = new FractalNoise(
                createNoiseSource(seed ^ 0xA123456789ABCDEFL),
                config.ridgeOctaves(), 2.0, 0.5);
        var fbmNoise = new FractalNoise(
                createNoiseSource(seed ^ 0xB23456789ABCDEF1L),
                config.fbmOctaves(), 2.0, 0.5);
        var flatNoise = createNoiseSource(seed ^ 0xC3456789ABCDEF12L);
        this.temperatureNoise = createNoiseSource(seed ^ 0x23456789ABCDEF1L);
        this.humidityNoise = createNoiseSource(seed ^ 0x3456789ABCDEF12L);
        this.boundaryWarpNoise = createNoiseSource(seed ^ 0xFEEDFACE1234L);

        // Climate resolver for biome selection (mirrors BiomeLookupTable behavior)
        // Climate resolver for biome selection: prefer biome-definition-driven envelopes,
        // fall back to the legacy hardcoded table for biomes not yet assigned custom ranges
        var biomeDefs = GeoForgeBiomeDefaults.createDefaults();
        var climateEnvs = ClimateResolver.exportFromBiomeDefinitions(biomeDefs);
        if (climateEnvs.isEmpty()) {
            climateEnvs = ClimateResolver.exportFromLegacyTable();
        }
        this.climateResolver = new ClimateResolver(
                ClimateResolver.ClimateConfig.defaults(),
                climateEnvs,
                "ocean");
        // 1. MultiNoiseHeightFunction blends ridge/FBM/flat based on continentalness with noise-warped boundaries
        var multiNoise = new MultiNoiseHeightFunction(
                ridgeNoise, fbmNoise, flatNoise,
                plateMapper,
                seed ^ 0xE70DE5L,
                config.ridgeFrequency(),
                config.fbmFrequency(),
                config.flatFrequency(),
                config.ridgeAmplitude(),
                config.continentalnessBlendSharpness(),
                this.boundaryWarpNoise,
                config.boundaryWarpFrequency(),
                config.boundaryWarpAmplitude());
        // 2. Scale the blended detail to block height magnitude
        var scaledMulti = new MultiplyDensity(multiNoise,
                new ConstantDensity(config.continentalHeightAmplitude() / 6.0));
        // 3. Tectonic plate influence — at c=0 (deep ocean): -continentalBase blocks,
        //    at c=1 (continent interior): continentalHeightAmplitude blocks
        var plates = new PlateContinentalness(plateMapper,
                -config.continentalBase(),
                config.continentalBase() + config.continentalHeightAmplitude());
        var combined = new AddDensity(scaledMulti, plates);
        // 4. Shift so sea level aligns with the configured value
        var shifted = new AddDensity(combined, new ConstantDensity(config.seaLevel()));
        // 5. Clamp to world height bounds
        this.heightFunction = new ClampDensity(shifted, config.minHeight(), config.maxHeight());

        // Domain warping: wrap the height function for terrain distortion
        this.domainWarpedHeight = new DomainWarpDensity(
                createNoiseSource(seed ^ 0xABCDEFABCDEFL),
                createNoiseSource(seed ^ 0xBCDEFA12345L),
                this.heightFunction,
                config.domainWarpAmplitude());

        this.erosion = new HydraulicErosion(config.erosionMaxDropletSteps(), config.erosionGravity());

        // 3D cave noise: multi-octave fractal for underground carving
        NoiseSource rawCaveSource = createNoiseSource(seed ^ 0x456789ABCDEF123L);
        if (config.caveWarpAmplitude() > 0.0) {
            rawCaveSource = new com.geoforge.engine.noise.DomainWarpedNoiseSource(
                    rawCaveSource,
                    createNoiseSource(seed ^ 0xABCDEF01L),
                    createNoiseSource(seed ^ 0xABCDEF02L),
                    createNoiseSource(seed ^ 0xABCDEF03L),
                    config.caveWarpAmplitude());
        }
        this.caveNoise = new FractalNoise(rawCaveSource,
                config.caveOctaves(),
                config.caveLacunarity(),
                config.cavePersistence());

        // Noodle cave noise source (seed-decorrelated from spaghetti cave noise)
        NoiseSource rawNoodleSource = createNoiseSource(seed ^ 0x56789ABCDEF0123L);
        if (config.noodleWarpAmplitude() > 0.0) {
            rawNoodleSource = new com.geoforge.engine.noise.DomainWarpedNoiseSource(
                    rawNoodleSource,
                    createNoiseSource(seed ^ 0xABCDEF04L),
                    createNoiseSource(seed ^ 0xABCDEF05L),
                    createNoiseSource(seed ^ 0xABCDEF06L),
                    config.noodleWarpAmplitude());
        }
        this.noodleNoise = rawNoodleSource;


        this.biomeRegistry = new BiomeRegistry(GeoForgeBiomeDefaults.createDefaults(), this.climateResolver);
        RiverCarver baseCarver;
        if (config.riverDepth() == 0) {
            baseCarver = NoopRiverCarver.instance();
        } else {
            baseCarver = switch (config.riverValleyProfile()) {
                case CANYON -> new CanyonRiverCarver(
                        seed ^ 0xFEEDBEEFL,
                        config.riverFrequency(),
                        config.riverCanyonDepth(),
                        config.riverCanyonWidth());
                case FLOODPLAIN -> new FloodplainRiverCarver(
                        seed ^ 0xFEEDBEEFL,
                        config.riverFrequency(),
                        config.riverDepth(),
                        config.riverFloodplainWidth());
                case VSHAPED -> new SimplexRiverCarver(
                        seed ^ 0xFEEDBEEFL,
                        config.riverFrequency(),
                        config.riverDepth(),
                        config.riverWidth());
            };
        }
        // Apply domain warping to river noise if configured
        if (config.riverWarpAmplitude() > 0.0 && !(baseCarver instanceof NoopRiverCarver)) {
            this.riverCarver = new com.geoforge.engine.density.DomainWarpedRiverCarver(
                    baseCarver,
                    createNoiseSource(seed ^ 0xABCDEF07L),
                    createNoiseSource(seed ^ 0xABCDEF08L),
                    config.riverWarpAmplitude());
        } else {
            this.riverCarver = baseCarver;
        }
        this.scenicDetector = new ScenicFeatureDetector(seed);
    }

    /**
     * Atomically replaces the biome registry (for hot reload).
     *
     * @param registry the new biome registry to use
     */
    public void replaceBiomeRegistry(BiomeRegistry registry) {
        this.biomeRegistry = registry;
    }

    /**
     * Returns the current biome registry.
     *
     * @return the biome registry instance
     */
    public BiomeRegistry getBiomeRegistry() {
        return biomeRegistry;
    }

    /**
     * Returns the scenic feature detector for wow-moment detection.
     *
     * @return the scenic feature detector
     */
    public ScenicFeatureDetector getScenicDetector() {
        return scenicDetector;
    }

    /**
     * Creates a noise source based on the configured noise backend.
     *
     * @param seed the seed for the noise source
     * @return a new noise source (GradientNoise or FastNoiseLiteSource)
     */
    private NoiseSource createNoiseSource(long seed) {
        return switch (config.noiseBackend()) {
            case "fastnoise" -> new FastNoiseLiteSource(seed);
            case "simplex", "gradient" -> new GradientNoise(seed);
            default -> {
                LOG.warning("Unknown noise backend '" + config.noiseBackend() + "', falling back to GradientNoise");
                yield new GradientNoise(seed);
            }
        };
    }

    /**
     * Returns the base terrain height at the given block coordinates.
     *
     * <p>This samples the raw {@code heightFunction} <b>without</b> domain warping,
     * cave noise, or river carving. When {@code domainWarpAmplitude > 0}, this
     * value may differ slightly from the effective surface in {@link #getDensity}
     * (which uses warped coordinates). For the exact per-block surface including
     * warping, use binary search via {@link #getSurfaceHeight}.
     *
     * @param blockX the x-coordinate in block space
     * @param blockZ the z-coordinate in block space
     * @return the base terrain height (without cave/river carving or domain warping)
     */
    public double getHeightAt(int blockX, int blockZ) {
        return heightFunction.sample(blockX, 0, blockZ);
    }

    /**
     * Returns the domain-warped surface height at the given block coordinates.
     * This matches the actual terrain surface used by {@link #getDensity}.
     *
     * @param blockX the x-coordinate in block space
     * @param blockZ the z-coordinate in block space
     * @return the domain-warped terrain height
     */
    public double getWarpedHeightAt(int blockX, int blockZ) {
        return domainWarpedHeight.sample(blockX, 0, blockZ);
    }

    /**
     * Returns the 3D density value at the given block coordinates.
     *
     * <p>Positive density indicates solid ground, negative density indicates air.
     * The density function is: {@code density = heightFunc(x,z) - y + caveNoise(x,y,z)}.
     *
     * @param blockX the x-coordinate in block space
     * @param blockY the y-coordinate in block space
     * @param blockZ the z-coordinate in block space
     * @return density value (positive = solid, negative = air)
     */
    public double getDensity(int blockX, int blockY, int blockZ) {
        return getDensity(blockX, blockY, blockZ,
                ColumnContext.compute(this, blockX, blockZ));
    }

    /**
     * Returns the 3D density value using a pre-computed ColumnContext.
     *
     * <p>This is the canonical density method — no caching or biome lookup.
     * All per-column data comes from the context. Callers iterating multiple Y
     * values in the same column should compute the context once and reuse it.
     *
     * @param blockX the x-coordinate in block space
     * @param blockY the y-coordinate in block space
     * @param blockZ the z-coordinate in block space
     * @param ctx    pre-computed column context
     * @return density value (positive = solid, negative = air)
     */
    public double getDensity(int blockX, int blockY, int blockZ, ColumnContext ctx) {
        double cave = caveNoise.sample3D(
                blockX * config.caveFrequency(),
                blockY * config.caveFrequency(),
                blockZ * config.caveFrequency());

        double heightComponent = ctx.targetHeight() - blockY;
        double caveComponent = cave * config.caveAmplitude();
        if (ctx.amplitudeMultiplier() != 1.0) {
            heightComponent *= ctx.amplitudeMultiplier();
        }
        if (ctx.caveModifier() != 1.0) {
            caveComponent *= ctx.caveModifier();
        }
        double density = heightComponent + caveComponent;
        if (ctx.heightOffset() != 0.0) {
            density += ctx.heightOffset();
        }

        // Enhanced cave system (v2+)
        if (config.configVersion() >= 2 && config.caveAmplitude() > 0
                && blockY < ctx.targetHeight() - config.caveSurfaceCutoff()) {
            double noodleFreq = config.caveNoodleFrequency();
            double noodleA = noodleNoise.sample3D(
                    blockX * noodleFreq,
                    blockY * noodleFreq,
                    blockZ * noodleFreq);
            double noodleB = noodleNoise.sample3D(
                    (blockX + 1000) * noodleFreq,
                    blockY * noodleFreq,
                    (blockZ + 1000) * noodleFreq);
            density = EnhancedCaveSystem.carve(
                    density, cave, noodleA, noodleB,
                    blockY, ctx.targetHeight(), config);
        }

        double carved = riverCarver.carve(density, blockX, blockY, blockZ);
        double weightedDensity = density + (carved - density) * ctx.valleyFactor();
        return DensityGuard.clamp(weightedDensity,
                config.minHeight(), config.maxHeight());
    }

    /**
     * Returns the terrain surface height (highest solid block) at the given column.
     *
     * <p>Uses binary search on the 3D density field to find the highest Y where
     * density >= 0. Each step calls {@link #getDensity} which includes cave noise,
     * biome modifier, and river carving, making this O(log n × C) where C is the
     * per-block density cost. For the default world height of 244 blocks, this is
     * approximately 8 iterations per call.
     *
     * <p>When cave amplitude is 0 and no erosion is configured, this matches the
     * 2D height function within integer precision.
     *
     * @param blockX the x-coordinate in block space
     * @param blockZ the z-coordinate in block space
     * @return the Y coordinate of the surface (highest solid block)
     */
    public int getSurfaceHeight(int blockX, int blockZ) {
        // Pre-compute ColumnContext once for this column (fixes Y-bug + optimization)
        ColumnContext ctx = ColumnContext.compute(this, blockX, blockZ);
        int low = config.minHeight();
        int high = config.maxHeight() - 1;
        while (low < high) {
            int mid = (low + high + 1) / 2;
            if (getDensity(blockX, mid, blockZ, ctx) >= 0) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    /**
     * Returns the biome ID at the given block coordinates.
     *
     * <p>The Y coordinate affects the temperature value via the configured vertical
     * temperature gradient ({@link GeoForgeConfig#temperatureYFrequency()}), so
     * altitude-aware biome transitions occur naturally when queried at the actual
     * terrain height. The continentalness from the {@link TectonicPlateMapper}
     * selects the environment tier (ocean, coast, inland, or highland) via
     * via the {@link ClimateResolver}.
     *
     * @param blockX the x-coordinate in block space
     * @param blockY the y-coordinate in block space (affects temperature)
     * @param blockZ the z-coordinate in block space
     * @return a valid Minecraft biome ID string
     */
    public String getBiomeId(int blockX, int blockY, int blockZ) {
        double temp = temperatureNoise.sample3D(
                blockX * config.temperatureFrequency(),
                blockY * config.temperatureYFrequency(),
                blockZ * config.temperatureFrequency());
        double humidity = (humidityNoise.sample2D(
                blockX * config.humidityFrequency(),
                blockZ * config.humidityFrequency()) + 1.0) * 0.5;
        float continentalness = plateMapper.getContinentalness(blockX, blockZ);
        double biomeWarp = boundaryWarpNoise.sample2D(
                (double) blockX * config.boundaryWarpFrequency(),
                (double) blockZ * config.boundaryWarpFrequency()) * config.boundaryWarpAmplitude();
        continentalness = (float) Math.min(1.0, Math.max(0.0, continentalness + biomeWarp));
        return this.biomeRegistry.climateResolver().resolve(temp, humidity, continentalness);
    }

    /**
     * Applies hydraulic erosion to the given heightmap in place.
     *
     * <p>The heightmap should be a flat float array of size {@code size × size},
     * typically populated by calling {@link #getHeightAt} for each column and then
     * passing the result to this method before placing blocks.
     *
     * @param heightmap the heightmap to erode (modified in place)
     * @param size      the width/height of the square heightmap
     * @param seed      a random seed for the erosion droplets
     */
    public void erode(float[] heightmap, int size, long seed) {
        erosion.erode(heightmap, size, config.erosionDropletCount(), seed);
    }

    /**
     * Populates a {@code size × size} heightmap area with surface heights and applies
     * hydraulic erosion in place for the given column area (size × size).
     *
     * <p>The heightmap is filled with the surface heights from {@link #getSurfaceHeight}
     * for each column in the region starting at {@code (blockX, blockZ)}. When erosion
     * is configured (both {@link GeoForgeConfig#erosionIterations erosionIterations}
     * and {@link GeoForgeConfig#erosionDropletCount erosionDropletCount} are positive),
     * hydraulic erosion is applied to the heightmap in place.
     *
     * <p>Despite its name, this method processes a multi-column area, not a single
     * column. The name reflects per-column processing within the area.
     *
     * @param heightmap a flat float array of size {@code size × size} (modified in place)
     * @param size      the width and height of the square column area
     * @param blockX    the world x-coordinate of the column area origin
     * @param blockZ    the world z-coordinate of the column area origin
     * @param seed      the world seed for deterministic erosion
     */
    public void erodeColumn(float[] heightmap, int size, int blockX, int blockZ, long seed) {
        // Populate heightmap with surface heights from the 3D density field
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                heightmap[z * size + x] = (float) getSurfaceHeight(blockX + x, blockZ + z);
            }
        }

        // Apply hydraulic erosion when both config guards are satisfied
        if (config.erosionIterations() > 0 && config.erosionDropletCount() > 0) {
            long erosionSeed = seed ^ (blockX * 1664525L + blockZ * 1013904223L) ^ 0xE70DE1L;
            float[] orig = new float[size * size];
            System.arraycopy(heightmap, 0, orig, 0, size * size);
            erosion.erode(heightmap, size, config.erosionDropletCount(), erosionSeed);
            // Apply surface hardness using pre-computed ColumnContext per column
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    int idx = z * size + x;
                    int surfaceY = Math.round(orig[idx]);
                    ColumnContext ctx = ColumnContext.compute(this,
                            blockX + x, blockZ + z);
                    double h = toTerrainConfig(
                            biomeRegistry.forBiome(ctx.biomeId())).surfaceHardness();
                    if (Math.abs(h - 0.5) > 1e-12) {
                        float blend = (float) Math.max(0.0, Math.min(2.0, (1.0 - h) / 0.5));
                        heightmap[idx] = orig[idx] + (heightmap[idx] - orig[idx]) * blend;
                    }
                }
            }
        }

        // Apply structure plateau flattening when plateauSize > 0 (chunk-centered,
        // for development/testing use — produces a flat area at each chunk's center.
        // For production structure-aligned placement, the plateau center coordinates
        // should be derived from a structure locator rather than chunk center.

        if (config.plateauSize() > 0) {
            int half = config.plateauSize() / 2;
            int cx = size / 2;
            int cz = size / 2;
            StructurePlateauModifier.applyPlateau(heightmap, size, cx - half, cz - half, cx + half, cz + half, (float) config.plateauTargetHeight());
        }
    }

    /**
     * Returns the configured sea level in blocks.
     *
     * @return the sea level
     */
    public int seaLevel() {
        return config.seaLevel();
    }

    /**
     * Returns the engine's configuration.
     *
     * @return the configuration record
     */
    public GeoForgeConfig config() {
        return config;
    }

    /**
     * Returns the set of all biome IDs this engine can produce.
     *
     * @return an immutable set of biome ID strings
     */
    public Set<String> getAllBiomeIds() {
        return biomeRegistry.getAllBiomeIds();
    }

    /**
     * Returns the biome terrain config for the given biome ID.
     *
     * @param biomeId the biome identifier
     * @return the biome terrain config (never null, returns defaults if unknown)
     */
    public BiomeTerrainConfig getBiomeConfig(String biomeId) {
        return toTerrainConfig(biomeRegistry.forBiome(biomeId));
    }

    /**
     * Returns the map of all biome terrain configs.
     *
     * @return the map of biome ID to terrain config
     */
    public Map<String, BiomeTerrainConfig> getBiomeConfigs() {
        var map = new java.util.HashMap<String, BiomeTerrainConfig>();
        for (String id : biomeRegistry.getAllBiomeIds()) {
            map.put(id, toTerrainConfig(biomeRegistry.forBiome(id)));
        }
        return java.util.Collections.unmodifiableMap(map);
    }

    /**
     * Returns the map of biome ID to vegetation block types.
     *
     * Vegetation types are sourced from the configured BiomeDefinition data
     * loaded from YAML vegetation configs (vegetation.yml).
     *
     * @return an unmodifiable map of biome ID to list of vegetation block types;
     *         biomes with no configured vegetation are omitted
     */
    public java.util.Map<String, java.util.List<String>> getBiomeVegetation() {
        var map = new java.util.HashMap<String, java.util.List<String>>();
        for (String id : biomeRegistry.getAllBiomeIds()) {
            java.util.List<String> veg = biomeRegistry.forBiome(id).vegetationTypes();
            if (veg != null && !veg.isEmpty()) {
                map.put(id, veg);
            }
        }
        return java.util.Collections.unmodifiableMap(map);
    }

    private static BiomeTerrainConfig toTerrainConfig(BiomeDefinition def) {
        return new BiomeTerrainConfig(
                def.heightOffset(),
                def.amplitudeMultiplier(),
                def.caveAmplitudeModifier(),
                def.treeType(),
                def.surfaceBlock(),
                def.subSurfaceBlock(),
                def.allowFloatingPlants(),
                def.surfaceHardness(),
                def.treeDensity(),
                def.minTreeHeight(),
                def.maxTreeHeight(),
                def.surfaceDepth());
    }
}
