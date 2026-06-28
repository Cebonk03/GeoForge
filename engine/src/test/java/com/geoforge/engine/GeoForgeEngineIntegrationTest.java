package com.geoforge.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.config.GeoForgeConfig;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("integration")
@DisplayName("Full engine pipeline integration tests")
class GeoForgeEngineIntegrationTest {

    private static final long SEED = 42L;
    private static final GeoForgeConfig CFG = GeoForgeConfig.defaults();

    @DisplayName("Full chain produces deterministic height and biome across invocations")
    @Test
    void fullChain_deterministicAcrossInvocations() {
        var engine1 = new GeoForgeEngine(SEED, CFG);
        var engine2 = new GeoForgeEngine(SEED, CFG);

        for (int x = -20; x <= 20; x += 7) {
            for (int z = -20; z <= 20; z += 7) {
                double h1 = engine1.getHeightAt(x, z);
                double h2 = engine2.getHeightAt(x, z);
                assertEquals(h1, h2, 1e-9, "Height determinism failed at (" + x + "," + z + ")");

                String b1 = engine1.getBiomeId(x, (int) Math.round(h1), z);
                String b2 = engine2.getBiomeId(x, (int) Math.round(h2), z);
                assertEquals(b1, b2, "Biome determinism failed at (" + x + "," + z + ")");
            }
        }
    }

    @DisplayName("All heights in full chain are within config bounds")
    @Test
    void fullChain_heightsWithinConfigBounds() {
        var engine = new GeoForgeEngine(SEED, CFG);
        for (int x = -100; x <= 100; x += 10) {
            for (int z = -100; z <= 100; z += 10) {
                double h = engine.getHeightAt(x, z);
                assertThat(h).isBetween((double) CFG.minHeight(), (double) CFG.maxHeight());
            }
        }
    }

    @DisplayName("All biome IDs from full chain are valid")
    @Test
    void fullChain_biomeIsValid() {
        var engine = new GeoForgeEngine(SEED, CFG);
        for (int x = -50; x <= 50; x += 25) {
            for (int z = -50; z <= 50; z += 25) {
                int y = (int) Math.round(engine.getHeightAt(x, z));
                String biome = engine.getBiomeId(x, y, z);
                assertThat(biome).isNotNull();
                assertThat(engine.getAllBiomeIds()).contains(biome);
            }
        }
    }

    @DisplayName("Biome varies by altitude at some positions")
    @Test
    void fullChain_biomeVariesByAltitude() {
        var engine = new GeoForgeEngine(SEED, CFG);
        boolean anyDiff = false;
        outer:
        for (int x = -10; x <= 10; x += 5) {
            for (int z = -10; z <= 10; z += 5) {
                String surface = engine.getBiomeId(x, (int) Math.round(engine.getHeightAt(x, z)), z);
                String deep = engine.getBiomeId(x, CFG.minHeight(), z);
                if (!surface.equals(deep)) {
                    anyDiff = true;
                    break outer;
                }
            }
        }
        assertThat(anyDiff).isTrue();
    }

    @DisplayName("Erosion modifies the heightmap from initial values")
    @Test
    void fullChain_erosionModifiesHeightmap() {
        var engine = new GeoForgeEngine(SEED, CFG);
        int size = 16;
        float[] hm = new float[size * size];
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                hm[z * size + x] = (float) engine.getHeightAt(x, z);
            }
        }
        float before = 0;
        for (float v : hm) before += Math.abs(v);

        engine.erode(hm, size, SEED);

        float after = 0;
        for (float v : hm) after += Math.abs(v);
        assertNotEquals(before, after, 1e-6f);
    }

    @DisplayName("Erosion column with zero droplets does not change heights")
    @Test
    void fullChain_erodeColumn_zeroDroplets_noChange() {
        var cfg = GeoForgeConfig.builder().erosionDropletCount(0).build();
        var engine = new GeoForgeEngine(SEED, cfg);
        int size = 16;
        float[] hm = new float[size * size];
        engine.erodeColumn(hm, size, 0, 0, SEED);

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                float expected = (float) engine.getSurfaceHeight(x, z);
                assertEquals(expected, hm[z * size + x], 1e-6f);
            }
        }
    }

    @DisplayName("Erosion column modifies heights in 16x16 area")
    @Test
    void fullChain_erodeColumn_modifiesHeights() {
        var engine = new GeoForgeEngine(SEED, CFG);
        int size = 16;
        float[] hm = new float[size * size];
        engine.erodeColumn(hm, size, 0, 0, SEED);

        boolean anyDiff = false;
        outer:
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                float original = (float) engine.getSurfaceHeight(x, z);
                if (Math.abs(hm[z * size + x] - original) > 1e-4f) {
                    anyDiff = true;
                    break outer;
                }
            }
        }
        assertThat(anyDiff).isTrue();
    }

    @DisplayName("Erosion column is deterministic with same seed and coordinates")
    @Test
    void fullChain_erodeColumn_deterministic() {
        var engine = new GeoForgeEngine(SEED, CFG);
        int size = 16;
        float[] hm1 = new float[size * size];
        float[] hm2 = new float[size * size];
        int blockX = -5;
        int blockZ = 3;
        engine.erodeColumn(hm1, size, blockX, blockZ, SEED);
        engine.erodeColumn(hm2, size, blockX, blockZ, SEED);
        assertArrayEquals(hm1, hm2, 1e-6f);
    }

    @DisplayName("Engine seaLevel accessor matches config")
    @Test
    void fullChain_seaLevelAccessor() {
        var engine = new GeoForgeEngine(SEED, CFG);
        assertEquals(CFG.seaLevel(), engine.seaLevel());
    }
}
