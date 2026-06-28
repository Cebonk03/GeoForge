package com.geoforge.engine.density;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Floodplain river carver tests")
class FloodplainRiverCarverTest {

    @DisplayName("Carve reduces density at some coordinates")
    @Test
    void carve_reducesDensityAtSomeCoordinates() {
        var carver = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        double nearSurface = 1.0;
        boolean carved = false;
        for (int x = 1; x <= 20 && !carved; x++) {
            for (int z = 1; z <= 20 && !carved; z++) {
                if (carver.carve(nearSurface, x, 0, z) < nearSurface) {
                    carved = true;
                }
            }
        }
        assertThat(carved).isTrue();
    }

    @DisplayName("Low noise frequency produces no carving")
    @Test
    void carve_noCarvingWhenNoiseIsPositive() {
        var carver = new FloodplainRiverCarver(42L, 0.01, 8, 3);
        double original = 5.0;
        double result = carver.carve(original, 0, 0, 0);
        assertEquals(original, result, 1e-12);
    }

    @DisplayName("Already negative density (air) is not affected")
    @Test
    void carve_doesNotAffectAir() {
        var carver = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        double result = carver.carve(-5.0, 7, 0, 11);
        assertEquals(-5.0, result, 1e-12);
    }

    @DisplayName("Carve is deterministic")
    @Test
    void carve_deterministic() {
        var carver = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        double r1 = carver.carve(1.0, 7, 0, 11);
        double r2 = carver.carve(1.0, 7, 0, 11);
        assertEquals(r1, r2, 1e-12);
    }

    @DisplayName("Different seeds produce different carving results")
    @Test
    void carve_differentSeedsProduceDifferentCarving() {
        var carver1 = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        var carver2 = new FloodplainRiverCarver(99L, 0.1, 8, 10);
        boolean anyDiff = false;
        outer:
        for (int x = 1; x <= 20; x++) {
            for (int z = 1; z <= 20; z++) {
                double r1 = carver1.carve(1.0, x, 0, z);
                double r2 = carver2.carve(1.0, x, 0, z);
                if (Math.abs(r1 - r2) > 1e-12) {
                    anyDiff = true;
                    break outer;
                }
            }
        }
        assertThat(anyDiff).isTrue();
    }

    @DisplayName("Carve with depth zero does not change density")
    @Test
    void carve_noCarvingWhenDepthIsZero() {
        var carver = new FloodplainRiverCarver(42L, 0.1, 0, 10);
        boolean unchanged = true;
        for (int x = 1; x <= 20; x++) {
            for (int z = 1; z <= 20; z++) {
                double result = carver.carve(1.0, x, 0, z);
                if (Math.abs(result - 1.0) > 1e-12) {
                    unchanged = false;
                }
            }
        }
        assertThat(unchanged).isTrue();
    }

    @DisplayName("Floodplain is wider than v-shaped carver")
    @Test
    void carve_floodplainIsWiderThanVshaped() {
        var floodplain = new FloodplainRiverCarver(42L, 0.1, 8, 20);
        var vshaped = new SimplexRiverCarver(42L, 0.1, 8, 3);

        int floodplainCarved = 0;
        int vshapedCarved = 0;
        for (int x = 1; x <= 30; x++) {
            for (int z = 1; z <= 30; z++) {
                if (floodplain.carve(1.0, x, 0, z) < 1.0) floodplainCarved++;
                if (vshaped.carve(1.0, x, 0, z) < 1.0) vshapedCarved++;
            }
        }
        assertThat(floodplainCarved).isGreaterThanOrEqualTo(vshapedCarved);
    }

    @DisplayName("Floodplain is shallower than v-shaped carver")
    @Test
    void carve_floodplainIsShallowerThanVshaped() {
        var floodplain = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        var vshaped = new SimplexRiverCarver(42L, 0.1, 8, 10);

        boolean floodplainShallower = false;
        outer:
        for (int x = 1; x <= 30; x++) {
            for (int z = 1; z <= 30; z++) {
                double fp = floodplain.carve(1.0, x, 0, z);
                double vs = vshaped.carve(1.0, x, 0, z);
                if (fp < 1.0 || vs < 1.0) {
                    if (fp > vs + 1e-12) {
                        floodplainShallower = true;
                        break outer;
                    }
                }
            }
        }
        assertThat(floodplainShallower).isTrue();
    }

    @DisplayName("Gentle banks produce smooth falloff")
    @Test
    void carve_gentleBanksProduceSmoothFalloff() {
        var carver = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        double nearSurface = 1.0;

        double prev = nearSurface;
        boolean hasTransition = false;
        boolean smoothTransition = true;
        for (int x = 1; x <= 30; x++) {
            double result = carver.carve(nearSurface, x, 0, 5);
            if (result < nearSurface && prev == nearSurface) {
                hasTransition = true;
            }
            if (result < nearSurface - 4.0 && prev == nearSurface) {
                smoothTransition = false;
            }
            prev = result;
        }
        assertThat(hasTransition).isTrue();
        assertThat(smoothTransition).isTrue();
    }
}
