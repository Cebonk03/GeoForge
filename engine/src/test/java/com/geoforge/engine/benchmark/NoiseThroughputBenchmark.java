package com.geoforge.engine.benchmark;

import com.geoforge.engine.noise.FastNoiseLiteSource;
import com.geoforge.engine.noise.NoiseSource;
import com.geoforge.engine.noise.SimplexNoise;

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
 * JMH microbenchmark measuring noise sampling throughput.
 *
 * <p>Compares {@link SimplexNoise} vs {@link FastNoiseLiteSource} in both 2D and 3D modes.
 * Results are reported as samples per second. Higher is better.
 *
 * <p>Uses varying input coordinates to exercise the full noise pipeline
 * and avoid cache-trivialization.
 */
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class NoiseThroughputBenchmark {

    private NoiseSource simplexNoise;
    private NoiseSource fastNoiseLite;
    private int coordIndex;

    /**
     * Initialises noise sources with the same seed for fair comparison.
     */
    @Setup
    public void setup() {
        simplexNoise = new SimplexNoise(42L);
        fastNoiseLite = new FastNoiseLiteSource(42L);
        coordIndex = 0;
    }

    private double coord() {
        int idx = coordIndex;
        coordIndex = (idx + 1) & 0xFFFF;
        return ((idx - 32768) & 0xFFFF) * 0.01;
    }

    @Benchmark
    public double simplex2D() {
        return simplexNoise.sample2D(coord(), coord());
    }

    @Benchmark
    public double simplex3D() {
        return simplexNoise.sample3D(coord(), coord(), coord());
    }

    @Benchmark
    public double fastNoiseLite2D() {
        return fastNoiseLite.sample2D(coord(), coord());
    }

    @Benchmark
    public double fastNoiseLite3D() {
        return fastNoiseLite.sample3D(coord(), coord(), coord());
    }
}
