package com.geoforge.engine;

import com.geoforge.engine.noise.NoiseSource;

import com.geoforge.engine.util.DensityGuard;
import com.geoforge.engine.biome.BiomeLookupTable;
import com.geoforge.engine.config.GeoForgeConfig;
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
import com.geoforge.engine.noise.FractalNoise;
import com.geoforge.engine.noise.SimplexNoise;
import java.util.Set;
import com.geoforge.engine.biome.BiomeTerrainConfig;
import com.geoforge.engine.density.EnhancedCaveSystem;
import java.util.HashMap;
import java.util.Map;

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
 * <p>Hydraulic erosion may be applied as a post-process step (see {@link #erode}).
 */
public final class GeoForgeEngine {

    private final GeoForgeConfig config;
    private final NoiseSource temperatureNoise;
    private final NoiseSource humidityNoise;
    private final TectonicPlateMapper plateMapper;
    private final DensityFunctionTree heightFunction;
    private final Set<String> allBiomeIds;
    private final HydraulicErosion erosion;
    private final FractalNoise caveNoise;
    private final RiverCarver riverCarver;
    private final NoiseSource noodleNoise;
    private final Map<String, BiomeTerrainConfig> biomeConfigs;
    private final DensityFunctionTree domainWarpedHeight;

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
                new SimplexNoise(seed ^ 0xA123456789ABCDEFL),
                config.ridgeOctaves(), 2.0, 0.5);
        var fbmNoise = new FractalNoise(
                new SimplexNoise(seed ^ 0xB23456789ABCDEF1L),
                config.fbmOctaves(), 2.0, 0.5);
        var flatNoise = new SimplexNoise(seed ^ 0xC3456789ABCDEF12L);
        this.temperatureNoise = new SimplexNoise(seed ^ 0x23456789ABCDEF1L);
        this.humidityNoise = new SimplexNoise(seed ^ 0x3456789ABCDEF12L);
        // Build composite height function using multi-noise blending:
        // 1. MultiNoiseHeightFunction blends ridge/FBM/flat based on continentalness
        var multiNoise = new MultiNoiseHeightFunction(
                ridgeNoise, fbmNoise, flatNoise,
                plateMapper,
                seed ^ 0xE70DE5L,
                config.ridgeFrequency(),
                config.fbmFrequency(),
                config.flatFrequency(),
                config.ridgeAmplitude(),
                config.continentalnessBlendSharpness());
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
                new SimplexNoise(seed ^ 0xABCDEFABCDEFL),
                new SimplexNoise(seed ^ 0xBCDEFA12345L),
                this.heightFunction,
                config.domainWarpAmplitude());

        this.allBiomeIds = BiomeLookupTable.getAllBiomeIds();
        this.erosion = new HydraulicErosion(config.erosionMaxDropletSteps(), config.erosionGravity());

        // 3D cave noise: multi-octave fractal for underground carving
        this.caveNoise = new FractalNoise(
                new SimplexNoise(seed ^ 0x456789ABCDEF123L),
                config.caveOctaves(),
                config.caveLacunarity(),
                config.cavePersistence());

        // Noodle cave noise source (seed-decorrelated from spaghetti cave noise)
        this.noodleNoise = new SimplexNoise(seed ^ 0x56789ABCDEF0123L);

        this.biomeConfigs = new HashMap<>();
        if (config.riverDepth() == 0) {
            this.riverCarver = NoopRiverCarver.instance();
        } else {
            this.riverCarver = switch (config.riverValleyProfile()) {
                case "canyon" -> new CanyonRiverCarver(
                        seed ^ 0xFEEDBEEFL,
                        config.riverFrequency(),
                        config.riverCanyonDepth(),
                        config.riverCanyonWidth());
                case "floodplain" -> new FloodplainRiverCarver(
                        seed ^ 0xFEEDBEEFL,
                        config.riverFrequency(),
                        config.riverDepth(),
                        config.riverFloodplainWidth());
                default -> new SimplexRiverCarver(
                        seed ^ 0xFEEDBEEFL,
                        config.riverFrequency(),
                        config.riverDepth(),
                        config.riverWidth());
            };

        }
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
        double targetHeight = domainWarpedHeight.sample(blockX, 0, blockZ);
        double cave = caveNoise.sample3D(
                blockX * config.caveFrequency(),
                blockY * config.caveFrequency(),
                blockZ * config.caveFrequency());
        double density = targetHeight - blockY + cave * config.caveAmplitude();

        // Enhanced cave system (v2+): additional three-type cave carving
        // Only applies to blocks well below the surface (outside the envelope cutoff zone)
        // to preserve a smooth density transition at the terrain surface
        if (config.configVersion() >= 2 && config.caveAmplitude() > 0
                && blockY < targetHeight - config.caveSurfaceCutoff()) {
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
                    blockY, targetHeight, config);
        }

        return DensityGuard.clamp(riverCarver.carve(density, blockX, blockY, blockZ),
                config.minHeight(), config.maxHeight());
    }

    /**
     * Returns the terrain surface height (highest solid block) at the given column.
     *
     * <p>Uses binary search on the 3D density field to find the highest Y where
     * density >= 0. When cave amplitude is 0, this matches the 2D height function.
     *
     * @param blockX the x-coordinate in block space
     * @param blockZ the z-coordinate in block space
     * @return the Y coordinate of the surface (highest solid block)
     */
    public int getSurfaceHeight(int blockX, int blockZ) {
        int low = config.minHeight();
        int high = config.maxHeight() - 1;
        // Binary search for highest Y with density >= 0.
        // Upper bound is maxHeight - 1 (not clamped to heightFunction) because
        // cave noise can push the surface up to caveAmplitude blocks higher.
        while (low < high) {
            int mid = (low + high + 1) / 2;
            if (getDensity(blockX, mid, blockZ) >= 0) {
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
     * {@link BiomeLookupTable#lookup(double, double, double)}.
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
        return BiomeLookupTable.lookup(temp, humidity, continentalness);
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
        erosion.erode(heightmap, size, config.erosionIterations(), seed);
    }

    /**
     * Populates a heightmap with surface heights and applies hydraulic erosion
     * in place for the given column area.
     *
     * <p>The heightmap is filled with the surface heights from {@link #getSurfaceHeight}
     * for each column in the {@code size × size} region starting at
     * {@code (blockX, blockZ)}. When erosion is configured (both
     * {@link GeoForgeConfig#erosionIterations erosionIterations} and
     * {@link GeoForgeConfig#erosionDropletCount erosionDropletCount} are positive),
     * hydraulic erosion is applied to the heightmap in place, modifying the surface
     * heights to simulate natural erosion processes.
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
            erosion.erode(heightmap, size, config.erosionIterations(), erosionSeed);
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
        return allBiomeIds;
    }
}
