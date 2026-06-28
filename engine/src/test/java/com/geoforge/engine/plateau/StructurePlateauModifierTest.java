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
}
