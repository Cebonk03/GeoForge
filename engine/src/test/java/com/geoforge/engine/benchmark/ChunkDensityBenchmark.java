package com.geoforge.engine.benchmark;

import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.GeoForgeConfig;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * JMH microbenchmark measuring full chunk density computation throughput.
 *
 * <p>A single chunk in Minecraft is 16×16 columns with height determined by the
 * world configuration (default: -64 to 180, totalling 244 layers). This benchmark
 * exercises {@link GeoForgeEngine#getDensity} for every block in a chunk, which
 * exercises the height function, tectonic plate continentalness, multi-octave
 * cave noise, enhanced cave system carving, and river carving.
 *
 * <p>The result is reported as chunks per second. Higher is better.
 */
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ChunkDensityBenchmark {

    private static final int CHUNK_SIZE = 16;

    private GeoForgeEngine engine;
    private GeoForgeConfig config;

    @Setup
    public void setup() {
        config = GeoForgeConfig.defaults();
        engine = new GeoForgeEngine(42L, config);
    }

    /**
     * Computes the full 3D density field for a single chunk at world origin.
     *
     * <p>Iterates over every column (x,z) in {@code [0, 16) × [0, 16)} and every
     * layer y in {@code [minHeight, maxHeight)} calling {@code getDensity} for
     * each block. The sum of all density values is returned to prevent dead-code
     * elimination.
     *
     * @return the sum of all density values in the chunk (meaningless, prevents DCE)
     */
    @Benchmark
    public double fullChunk() {
        double sum = 0;
        int minY = config.minHeight();
        int maxY = config.maxHeight();
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = minY; y < maxY; y++) {
                    sum += engine.getDensity(x, y, z);
                }
            }
        }
        return sum;
    }
}
