package com.geoforge.engine.noise;


/**
 * A {@link NoiseSource} implementation wrapping Auburn's FastNoiseLite library.
 *
 * <p>Uses OpenSimplex2 noise with FBM fractal by default. Produces values in [-1, 1]
 * and is deterministic for the same seed. Benchmark results indicate 2-4x throughput
 * improvement over {@link SimplexNoise} for 3D samples.
 *
 * <p>Internally uses a FastNoiseLite instance configured with:
 * <ul>
 *   <li>OpenSimplex2 noise type (high quality, no directional artifacts)</li>
 *   <li>FBM fractal with lacunarity 2.0 and gain 0.5</li>
 *   <li>3 octaves by default</li>
 * </ul>
 *
 * <p>This class is thread-safe after construction (FastNoiseLite instances are stateless
 * after initial configuration).
 */
public final class FastNoiseLiteSource implements NoiseSource {

    private final FastNoiseLite noise;

    /**
     * Creates a FastNoiseLite source with the given seed.
     *
     * @param seed the world generation seed
     */
    public FastNoiseLiteSource(long seed) {
        this(seed, 3, 2.0f, 0.5f);
    }

    /**
     * Creates a FastNoiseLite source with full fractal control.
     *
     * @param seed       the world generation seed
     * @param octaves    number of fractal octaves
     * @param lacunarity frequency multiplier between octaves
     * @param gain       amplitude multiplier between octaves (persistence)
     */
    public FastNoiseLiteSource(long seed, int octaves, float lacunarity, float gain) {
        this.noise = new FastNoiseLite();
        long xored = seed ^ 0xF1A2B3C4D5E6F7L;
        this.noise.SetSeed((int) xored);
        this.noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.noise.SetFractalType(FastNoiseLite.FractalType.FBm);
        this.noise.SetFractalOctaves(octaves);
        this.noise.SetFractalLacunarity(lacunarity);
        this.noise.SetFractalGain(gain);
        this.noise.SetFrequency(1.0f); // Caller applies frequency scaling
    }

    @Override
    public double sample2D(double x, double z) {
        return noise.GetNoise((float) x, (float) z);
    }

    @Override
    public double sample3D(double x, double y, double z) {
        return noise.GetNoise((float) x, (float) y, (float) z);
    }
}
