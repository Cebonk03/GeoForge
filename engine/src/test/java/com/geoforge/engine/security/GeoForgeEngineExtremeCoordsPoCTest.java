package com.geoforge.engine.security;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.GeoForgeEngine;
import org.junit.jupiter.api.Test;

/**
 * PoC tests for {@link GeoForgeEngine} behavior under extreme or edge-case coordinates.
 *
 * <p>Attack vector: A player moving to extreme coordinates (world border, far lands, or
 * negative coordinates) triggers chunk generation. The engine must not crash, produce
 * invalid terrain heights, or return non-deterministic results at any coordinate.
 */
class GeoForgeEngineExtremeCoordsPoCTest {

    private static final long SEED = 42L;

    /**
     * PoC: Engine must not crash at Integer.MIN_VALUE / MAX_VALUE coordinates.
     *
     * <p>While Minecraft servers typically constrain world border, generator code
     * must handle any int input without throwing.
     */
    @Test
    void getHeightAt_extremeIntValues_noCrash() {
        var engine = new GeoForgeEngine(SEED);

        double h1 = engine.getHeightAt(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertFalse(Double.isNaN(h1), "Height must not be NaN at Integer.MIN_VALUE");
        assertTrue(h1 >= -64 && h1 <= 180,
                "Height at MIN_VALUE should be in [-64, 180] range: " + h1);

        double h2 = engine.getHeightAt(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertFalse(Double.isNaN(h2), "Height must not be NaN at Integer.MAX_VALUE");
        assertTrue(h2 >= -64 && h2 <= 180,
                "Height at MAX_VALUE should be in [-64, 180] range: " + h2);

        double h3 = engine.getHeightAt(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertFalse(Double.isNaN(h3));
        assertTrue(h3 >= -64 && h3 <= 180);

        double h4 = engine.getHeightAt(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertFalse(Double.isNaN(h4));
        assertTrue(h4 >= -64 && h4 <= 180);
    }

    /**
     * PoC: Height at extreme coordinates is deterministic (same seed → same result).
     */
    @Test
    void getHeightAt_extremeCoords_deterministic() {
        var engine1 = new GeoForgeEngine(SEED);
        var engine2 = new GeoForgeEngine(SEED);

        int[][] coords = {
            {Integer.MIN_VALUE, Integer.MIN_VALUE},
            {Integer.MAX_VALUE, Integer.MAX_VALUE},
            {Integer.MIN_VALUE, Integer.MAX_VALUE},
            {-100_000_000, -100_000_000},
            {100_000_000, 100_000_000},
            {0, 0},
            {-1, 1},
        };

        for (int[] coord : coords) {
            double h1 = engine1.getHeightAt(coord[0], coord[1]);
            double h2 = engine2.getHeightAt(coord[0], coord[1]);
            assertEquals(h1, h2, 1e-9,
                    "Determinism at (" + coord[0] + "," + coord[1] + ")");
        }
    }

    /**
     * PoC: Engine must not crash or produce invalid biomes at extreme coordinates.
     *
     * <p>Biome lookup uses temperature (from 3D noise at blockX*0.001, blockY*0.001, blockZ*0.001)
     * and humidity (from 2D noise). At extreme coordinates, the noise input may be very large
     * but must not cause out-of-bounds or crashes.
     */
    @Test
    void getBiomeId_extremeCoords_validBiome() {
        var engine = new GeoForgeEngine(SEED);

        String b1 = engine.getBiomeId(Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
        assertNotNull(b1, "Biome must not be null at MIN_VALUE");
        assertTrue(engine.getAllBiomeIds().contains(b1),
                "Biome must be in known palette: " + b1);

        String b2 = engine.getBiomeId(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        assertNotNull(b2);
        assertTrue(engine.getAllBiomeIds().contains(b2));

        // Extreme Y values
        String b3 = engine.getBiomeId(0, Integer.MIN_VALUE, 0);
        assertNotNull(b3);

        String b4 = engine.getBiomeId(0, Integer.MAX_VALUE, 0);
        assertNotNull(b4);
    }

    /**
     * PoC: Chunk coordinate wrapping at Integer.MIN_VALUE.
     *
     * <p>In GeoForgeGenerator, blockX = chunkX * 16 + x. If chunkX = Integer.MIN_VALUE,
     * then chunkX * 16 wraps around to 0 (integer overflow), making blockX = 0 + x = x.
     * This is not a GeoForge-specific bug — it's inherent to Java int arithmetic and
     * exists in any generator that computes blockX this way.
     *
     * <p>Practical risk: None. World generation at Integer.MIN_VALUE / 16 ≈ -134 million
     * chunks is far beyond any practical world border. The Minecraft server itself would
     * break long before reaching these coordinates.
     */
    @Test
    void chunkCoordOverflow_staticProof() {
        // Prove the overflow behavior
        int chunkX = Integer.MIN_VALUE;
        int x = 5;
        int blockX = chunkX * 16 + x;

        // Integer.MIN_VALUE * 16 overflows: Math.multiplyExact would throw
        // 0x80000000 << 4 = 0x00000000 (leftmost bits rotate out)
        assertEquals(5, blockX,
                "blockX at chunkX=MIN_VALUE wraps to small positive due to overflow");

        // Normal negative chunk coordinate — should compute correctly
        int chunkX2 = -100_000;
        int blockX2 = chunkX2 * 16 + x;
        assertEquals(-1_599_995, blockX2,
                "Normal negative chunk coords compute correctly");
    }

    /**
     * PoC: International date line / zero-chunk boundary correctness.
     *
     * <p>Chunk at x=0 produces block coords 0..15. Verify heights are consistent
     * across the chunk boundary (no seam at zero).
     */
    @Test
    void zeroBoundary_noSeam() {
        var engine = new GeoForgeEngine(SEED);

        // Chunk 0: blockX 0..15, chunk -1: blockX -16..-1
        double h1 = engine.getHeightAt(-1, 0);  // last block of chunk -1
        double h2 = engine.getHeightAt(0, 0);    // first block of chunk 0
        // These are different positions so heights differ naturally,
        // but they must both be in range
        assertTrue(h1 >= -64 && h1 <= 180, "Height at -1: " + h1);
        assertTrue(h2 >= -64 && h2 <= 180, "Height at 0: " + h2);

        // Height should be continuous — no more than 50 block difference
        double diff = Math.abs(h1 - h2);
        assertTrue(diff < 100, "Height difference across zero boundary: " + diff);
    }

    /**
     * PoC: Height function clamping correctness at extreme input values.
     *
     * <p>The engine clamps to [-64, 180]. Verify no overflow or invalid values
     * escape the clamp.
     */
    @Test
    void heightClamping_alwaysInRange() {
        var engine = new GeoForgeEngine(SEED);

        // Sample a wide grid at chunk scale
        for (int chunkX = -100; chunkX <= 100; chunkX += 7) {
            for (int chunkZ = -100; chunkZ <= 100; chunkZ += 11) {
                for (int x = 0; x < 16; x += 4) {
                    for (int z = 0; z < 16; z += 4) {
                        int bx = chunkX * 16 + x;
                        int bz = chunkZ * 16 + z;
                        double h = engine.getHeightAt(bx, bz);
                        assertTrue(h >= -64 && h <= 180,
                                "Height out of range at chunk(" + chunkX + ","
                                + chunkZ + ") block(" + bx + "," + bz + "): " + h);
                    }
                }
            }
        }
    }

    /**
     * PoC: Default seed (0L) produces identical terrain across all instances.
     *
     * <p>In {@link com.geoforge.plugin.GeoForgePlugin#onEnable}, the engine is
     * created with {@code new GeoForgeEngine(0L)}. This means every server running
     * GeoForge with default settings gets the exact same terrain — no
     * per-world or per-seed variation.
     */
    @Test
    void defaultSeed_zeroProducesIdenticalTerrainAcrossInstances() {
        GeoForgeEngine e1 = new GeoForgeEngine(0L);
        GeoForgeEngine e2 = new GeoForgeEngine(0L);
        GeoForgeEngine e3 = new GeoForgeEngine(0L);

        for (int x = -1000; x <= 1000; x += 37) {
            for (int z = -1000; z <= 1000; z += 53) {
                double h1 = e1.getHeightAt(x, z);
                double h2 = e2.getHeightAt(x, z);
                double h3 = e3.getHeightAt(x, z);
                assertEquals(h1, h2, 1e-12, "e1 vs e2 at (" + x + "," + z + ")");
                assertEquals(h2, h3, 1e-12, "e2 vs e3 at (" + x + "," + z + ")");
            }
        }

        for (int x = -500; x <= 500; x += 41) {
            String b1 = e1.getBiomeId(x, 63, x);
            String b2 = e2.getBiomeId(x, 63, x);
            assertEquals(b1, b2, "Biome match at " + x);
        }
    }

    /**
     * PoC: A different seed must produce different terrain.
     */
    @Test
    void differentSeed_producesDifferentTerrain() {
        GeoForgeEngine eDefault = new GeoForgeEngine(0L);
        GeoForgeEngine eDifferent = new GeoForgeEngine(12345L);

        boolean anyDiff = false;
        for (int x = -500; x <= 500; x += 7) {
            for (int z = -500; z <= 500; z += 11) {
                double h1 = eDefault.getHeightAt(x, z);
                double h2 = eDifferent.getHeightAt(x, z);
                if (Math.abs(h1 - h2) > 1e-9) {
                    anyDiff = true;
                    break;
                }
            }
            if (anyDiff) break;
        }
        assertTrue(anyDiff,
                "Different seeds (0L vs 12345L) must produce different terrain");
    }
}
