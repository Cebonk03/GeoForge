package com.geoforge.engine;

import com.geoforge.engine.biome.BiomeLookupTable;
import com.geoforge.engine.density.AddDensity;
import com.geoforge.engine.density.ClampDensity;
import com.geoforge.engine.density.ConstantDensity;
import com.geoforge.engine.density.DensityFunctionTree;
import com.geoforge.engine.density.MultiplyDensity;
import com.geoforge.engine.density.ScaledNoise;
import com.geoforge.engine.noise.SimplexNoise;
import java.util.Set;

/**
 * The core world generation engine for GeoForge.
 *
 * <p>Generates terrain height and biome assignments using seeded noise functions. This class
 * has zero Bukkit dependencies and is safe to use in any context.
 *
 * <p>Height generation combines continental-scale noise with tectonic plate mapping to create
 * realistic landmasses, mountain ranges, and ocean basins.
 */
public final class GeoForgeEngine {

    /** Sea level in block units — always use this constant, never the literal 63. */
    public static final int SEA_LEVEL = 63;

    private static final int MAX_TERRAIN_HEIGHT = 180;

    private final SimplexNoise continentalNoise;
    private final SimplexNoise temperatureNoise;
    private final SimplexNoise humidityNoise;
    private final SimplexNoise erosionNoise;
    private final DensityFunctionTree heightFunction;
    private final Set<String> allBiomeIds;

    /** Creates a new engine with the given world seed. */
    public GeoForgeEngine(long seed) {
        this.continentalNoise = new SimplexNoise(seed ^ 0x123456789ABCDEFL);
        this.temperatureNoise = new SimplexNoise(seed ^ 0x23456789ABCDEF1L);
        this.humidityNoise = new SimplexNoise(seed ^ 0x3456789ABCDEF12L);
        this.erosionNoise = new SimplexNoise(seed ^ 0x456789ABCDEF123L);

        // Build a composite height function:
        // continental base (0.003 frequency, 1.5 amplitude)
        var continent =
                new ScaledNoise(continentalNoise, 0.004, 0.0, 0.004);
        // scale to [0, MAX_TERRAIN_HEIGHT]
        var scaled = new MultiplyDensity(continent, new ConstantDensity(90.0));
        // shift so sea level sits at ~63 for moderate continent values
        var shifted = new AddDensity(scaled, new ConstantDensity(63.0));
        // clamp to valid range
        this.heightFunction = new ClampDensity(shifted, -64.0, MAX_TERRAIN_HEIGHT);

        this.allBiomeIds = BiomeLookupTable.getAllBiomeIds();
    }

    /**
     * Returns the terrain height at the given block coordinates.
     *
     * @param blockX the x-coordinate in block space
     * @param blockZ the z-coordinate in block space
     * @return the terrain height as a double
     */
    public double getHeightAt(int blockX, int blockZ) {
        return heightFunction.sample(blockX, 0, blockZ);
    }

    /**
     * Returns the biome ID at the given block coordinates.
     *
     * @param blockX the x-coordinate in block space
     * @param blockY the y-coordinate in block space
     * @param blockZ the z-coordinate in block space
     * @return a valid Minecraft biome ID string
     */
    public String getBiomeId(int blockX, int blockY, int blockZ) {
        double temp = temperatureNoise.sample(blockX * 0.001, blockY * 0.001, blockZ * 0.001);
        double humidity = (humidityNoise.sample(blockX * 0.001, blockZ * 0.001) + 1.0) * 0.5;
        return BiomeLookupTable.lookup(temp, humidity);
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
