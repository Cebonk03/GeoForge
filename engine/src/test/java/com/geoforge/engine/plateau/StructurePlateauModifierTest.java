package com.geoforge.engine.plateau;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Structure plateau modifier tests")
class StructurePlateauModifierTest {

    @DisplayName("Core interior cells are at target height")
    @Test
    void applyPlateau_coreInteriorEqualToTarget() {
        float[] heightmap = new float[32 * 32];
        java.util.Arrays.fill(heightmap, 40.0f);
        float target = 60.0f;

        StructurePlateauModifier.applyPlateau(heightmap, 32, 10, 10, 20, 20, target);

        for (int z = 13; z <= 17; z++) {
            for (int x = 13; x <= 17; x++) {
                float cell = heightmap[z * 32 + x];
                assertEquals(target, cell, 1e-6f,
                        "Core interior cell at (" + x + "," + z + ") = " + cell);
            }
        }
    }

    @DisplayName("Cells outside bounding box are unchanged")
    @Test
    void applyPlateau_cellsOutsideBoundingBoxUnchanged() {
        float[] heightmap = new float[32 * 32];
        java.util.Arrays.fill(heightmap, 40.0f);
        float initialOutside = heightmap[0];

        StructurePlateauModifier.applyPlateau(heightmap, 32, 8, 8, 16, 16, 70.0f);

        assertEquals(initialOutside, heightmap[0], 1e-6f);
        assertEquals(initialOutside, heightmap[31], 1e-6f);
        assertEquals(initialOutside, heightmap[31 * 32 + 0], 1e-6f);
    }

    @DisplayName("Applying plateau with out-of-range coordinates does not throw")
    @Test
    void applyPlateau_clampsToSizeBoundary() {
        float[] heightmap = new float[16 * 16];
        java.util.Arrays.fill(heightmap, 50.0f);

        assertDoesNotThrow(() ->
                StructurePlateauModifier.applyPlateau(heightmap, 16, -5, -5, 25, 25, 80.0f));
    }

    @DisplayName("Plateau on the edge of the heightmap does not throw")
    @Test
    void applyPlateau_plateauOnEdge_doesNotThrow() {
        float[] heightmap = new float[16 * 16];
        java.util.Arrays.fill(heightmap, 50.0f);

        assertDoesNotThrow(() ->
                StructurePlateauModifier.applyPlateau(heightmap, 16, 0, 0, 3, 3, 90.0f));
    }

    @DisplayName("Single-cell plateau unchanged in flat terrain")
    @Test
    void applyPlateau_singleCell_unchangedInFlatTerrain() {
        float[] heightmap = new float[16 * 16];
        java.util.Arrays.fill(heightmap, 50.0f);

        StructurePlateauModifier.applyPlateau(heightmap, 16, 7, 7, 7, 7, 100.0f);
        float cell = heightmap[7 * 16 + 7];
        assertEquals(50.0f, cell, 1e-6f);
    }


    @DisplayName("Feather creates monotonic transition from original to target height")
    @Test
    void feather_createsMonotonicTransition() {
        float[] heightmap = new float[32 * 32];
        for (int i = 0; i < heightmap.length; i++) {
            heightmap[i] = 40.0f;
        }
        // Lower outside cells on the left to create measurable gradient
        for (int z = 0; z < 32; z++) {
            for (int x = 8; x < 10; x++) {
                heightmap[z * 32 + x] = 30.0f;
            }
        }
        float target = 80.0f;

        StructurePlateauModifier.applyPlateau(heightmap, 32, 10, 10, 20, 20, target);

        // At z=15: outside (x=9) < border (x=10) < interior (x=13)
        float outside = heightmap[15 * 32 + 9];
        float border = heightmap[15 * 32 + 10];
        float interior = heightmap[15 * 32 + 13];

        assertThat(border).isBetween(outside, interior);
        assertThat(outside).isLessThanOrEqualTo(border);
        assertThat(border).isLessThanOrEqualTo(interior);
    }

    @DisplayName("Border cells blend original terrain with target height")
    @Test
    void feather_borderCells_blendOrigAndTarget() {
        float[] heightmap = new float[32 * 32];
        java.util.Arrays.fill(heightmap, 30.0f);
        float target = 80.0f;

        StructurePlateauModifier.applyPlateau(heightmap, 32, 10, 10, 20, 20, target);

        // Border cell at x=10 has outside neighbors at x=9
        // feather=0 at border → fully blended to neighbor avg = 30.0
        float border = heightmap[15 * 32 + 10];
        assertEquals(30.0f, border, 1e-6f);

        // Cell at x=11 has NO outside neighbors (3x3 all inside [10,20])
        // getNeighborAvg returns its own value (80.0 from first pass)
        // blend = 80*0.75 + 80*0.25 = 80 → stays at target
        float stepIn = heightmap[15 * 32 + 11];
        assertEquals(80.0f, stepIn, 1e-6f);

        // Interior (x=13+) at target
        float interior = heightmap[15 * 32 + 13];
        assertEquals(80.0f, interior, 1e-6f);
    }

    @DisplayName("Plateau at heightmap edge boundary conditions")
    @Test
    void feather_plateauAtHeightmapEdge_boundaryConditions() {
        float[] heightmap = new float[16 * 16];
        java.util.Arrays.fill(heightmap, 50.0f);

        assertDoesNotThrow(() ->
                StructurePlateauModifier.applyPlateau(heightmap, 16, 0, 0, 5, 5, 80.0f));

        // Interior starts at (3,3) for plateau (0,0)-(5,5)
        float interior = heightmap[3 * 16 + 3];
        assertEquals(80.0f, interior, 1e-6f);

        // Corner cell (0,0) should be blended, not throw
        float corner = heightmap[0];
        assertThat(corner).isBetween(50.0f, 80.0f);
    }
}
