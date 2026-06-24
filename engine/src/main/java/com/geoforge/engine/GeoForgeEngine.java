package com.geoforge.engine;

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
import com.geoforge.engine.geology.HydraulicErosion;
import com.geoforge.engine.geology.TectonicPlateMapper;
import com.geoforge.engine.noise.FractalNoise;
import com.geoforge.engine.noise.SimplexNoise;
import java.util.Set;

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
    private final FractalNoise continentalNoise;
    private final SimplexNoise temperatureNoise;
    private final SimplexNoise humidityNoise;
    private final TectonicPlateMapper plateMapper;
    private final DensityFunctionTree heightFunction;
    private final Set<String> allBiomeIds;
    private final HydraulicErosion erosion;
    private final SimplexNoise caveNoise;
    private final RiverCarver riverCarver;

    /**
     * Creates a new engine with the given world seed and configuration.
     *
     * @param seed   the world generation seed
     * @param config the configuration for all terrain tuning parameters
     */
    public GeoForgeEngine(long seed, GeoForgeConfig config) {
        this.config = config;
        this.plateMapper = new TectonicPlateMapper(seed ^ 0xDEADBEEFL);

        this.continentalNoise = new FractalNoise(
                new SimplexNoise(seed ^ 0x123456789ABCDEFL),
                config.continentalOctaves(),
                config.continentalLacunarity(),
                config.continentalPersistence());
        this.temperatureNoise = new SimplexNoise(seed ^ 0x23456789ABCDEF1L);
        this.humidityNoise = new SimplexNoise(seed ^ 0x3456789ABCDEF12L);

        // Build composite height function:
        // 1. Multi-octave fractal noise for terrain detail (additive, not multiplicative —
        //    avoids inverting the tectonic signal when noise is negative)
        DensityFunctionTree detailNoise = (x, y, z) -> continentalNoise.sample2D(
                x * config.continentalFrequency(), z * config.continentalFrequency());
        var scaledDetail = new MultiplyDensity(detailNoise,
                new ConstantDensity(config.continentalHeightAmplitude() / 6.0));
        // 2. Tectonic plate influence — at c=0 (deep ocean): -continentalBase blocks,
        //    at c=1 (continent interior): continentalHeightAmplitude blocks
        var plates = new PlateContinentalness(plateMapper,
                -config.continentalBase(),
                config.continentalBase() + config.continentalHeightAmplitude());
        var combined = new AddDensity(scaledDetail, plates);
        // 3. Shift so sea level aligns with the configured value
        var shifted = new AddDensity(combined, new ConstantDensity(config.seaLevel()));
        // 4. Clamp to world height bounds
        this.heightFunction = new ClampDensity(shifted, config.minHeight(), config.maxHeight());

        this.allBiomeIds = BiomeLookupTable.getAllBiomeIds();
        this.erosion = new HydraulicErosion(config.erosionMaxDropletSteps());

        // 3D cave noise for underground carving
        this.caveNoise = new SimplexNoise(seed ^ 0x456789ABCDEF123L);
        this.riverCarver = NoopRiverCarver.instance();

    /**
     * Returns the terrain height at the given block coordinates.
     *
     * <p>This is the 2D height function value. For 3D density use {@link #getDensity}.
     *
     * @param blockX the x-coordinate in block space
     * @param blockZ the z-coordinate in block space
     * @return the terrain height as a double
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
        double targetHeight = heightFunction.sample(blockX, 0, blockZ);
        double cave = caveNoise.sample(
                blockX * config.caveFrequency(),
                blockY * config.caveFrequency(),
                blockZ * config.caveFrequency());
        double density = targetHeight - blockY + cave * config.caveAmplitude();
        return riverCarver.carve(density, blockX, blockY, blockZ);
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
        double targetHeight = getHeightAt(blockX, blockZ);
        int low = config.minHeight();
        int high = Math.min((int) Math.ceil(targetHeight), config.maxHeight() - 1);
        // Binary search for highest Y with density >= 0
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
     * terrain height.
     *
     * @param blockX the x-coordinate in block space
     * @param blockY the y-coordinate in block space (affects temperature)
     * @param blockZ the z-coordinate in block space
     * @return a valid Minecraft biome ID string
     */
    public String getBiomeId(int blockX, int blockY, int blockZ) {
        double temp = temperatureNoise.sample(
                blockX * config.temperatureFrequency(),
                blockY * config.temperatureYFrequency(),
                blockZ * config.temperatureFrequency());
        double humidity = (humidityNoise.sample(
                blockX * config.humidityFrequency(),
                blockZ * config.humidityFrequency()) + 1.0) * 0.5;
        return BiomeLookupTable.lookup(temp, humidity);
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
