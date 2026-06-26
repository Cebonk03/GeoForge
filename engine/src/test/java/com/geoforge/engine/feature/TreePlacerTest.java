package com.geoforge.engine.feature;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.feature.TreePlacer.TreeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class TreePlacerTest {

    // ───────────────────────────────────────────────────────
    //  Recording BlockSetter — captures every setBlock call
    // ───────────────────────────────────────────────────────

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();

        @Override
        public void setBlock(int x, int y, int z, String materialName) {
            blocks.add(new String[]{String.valueOf(x), String.valueOf(y), String.valueOf(z), materialName});
        }

        void reset() {
            blocks.clear();
        }
    }

    private static final long FIXED_SEED = 42L;

    // ───────────────────────────────────────────────────────
    //  1. Constructor validation
    // ───────────────────────────────────────────────────────

    @Test
    void constructor_rejectsNegativeDensity() {
        assertThrows(IllegalArgumentException.class,
                () -> new TreePlacer(-0.1, 12));
    }

    @Test
    void constructor_rejectsDensityAboveOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new TreePlacer(1.5, 12));
    }

    @Test
    void constructor_rejectsMaxTreeHeightBelowFour() {
        assertThrows(IllegalArgumentException.class,
                () -> new TreePlacer(0.1, 3));
    }

    @Test
    void constructor_acceptsValidValues() {
        var placer = new TreePlacer(0.5, 10);
        assertEquals(0.5, placer.treeDensity(), 1e-12);
        assertEquals(10, placer.maxTreeHeight());
    }

    @Test
    void constructor_densityZeroIsValid() {
        assertDoesNotThrow(() -> new TreePlacer(0.0, 4));
    }

    // ───────────────────────────────────────────────────────
    //  2. Deterministic placement
    // ───────────────────────────────────────────────────────

    @Test
    void place_sameSeedProducesSameBlocks() {
        var placer = new TreePlacer(1.0, 12);

        RecordingSetter first = new RecordingSetter();
        Random rng1 = new Random(FIXED_SEED);
        placer.place(first, 0, 0, 60, "forest", rng1);

        RecordingSetter second = new RecordingSetter();
        Random rng2 = new Random(FIXED_SEED);
        placer.place(second, 0, 0, 60, "forest", rng2);

        assertEquals(first.blocks.size(), second.blocks.size(),
                "Same seed should produce same number of placed blocks");
        for (int i = 0; i < first.blocks.size(); i++) {
            assertArrayEquals(first.blocks.get(i), second.blocks.get(i),
                    "Block " + i + " differs between deterministic runs");
        }
    }

    @Test
    void place_differentSeedsMayProduceDifferentPlacement() {
        var placer = new TreePlacer(1.0, 12);

        RecordingSetter first = new RecordingSetter();
        placer.place(first, 0, 0, 60, "forest", new Random(1));

        RecordingSetter second = new RecordingSetter();
        placer.place(second, 0, 0, 60, "forest", new Random(9999));

        // Very unlikely that two different seeds produce identical tree placement
        // across all block coordinates — but we don't enforce difference, we
        // just verify that the call completes without error and at least one
        // block was placed (density=1 guarantees a tree).
        assertFalse(first.blocks.isEmpty(), "Tree should be placed at density 1.0");
        assertFalse(second.blocks.isEmpty(), "Tree should be placed at density 1.0");
    }

    // ───────────────────────────────────────────────────────
    //  3. Correct tree types per biome
    // ───────────────────────────────────────────────────────

    @Test
    void place_forestBiome_usesOakOrBirch() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(12345);

        // Run many placements across a forest biome
        for (int i = 0; i < 100; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "forest", rng);
            if (!recorder.blocks.isEmpty()) {
                String logMat = findLogMaterial(recorder.blocks);
                assertTrue("oak_log".equals(logMat) || "birch_log".equals(logMat),
                        "Forest biome should produce oak or birch trees, got: " + logMat);
            }
        }
    }

    @Test
    void place_taigaBiome_usesSpruce() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(67890);

        for (int i = 0; i < 100; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "taiga", rng);
            if (!recorder.blocks.isEmpty()) {
                String logMat = findLogMaterial(recorder.blocks);
                assertEquals("spruce_log", logMat,
                        "Taiga biome should produce spruce trees, got: " + logMat);
            }
        }
    }

    @Test
    void place_jungleBiome_usesJungle() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(11111);

        for (int i = 0; i < 100; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "jungle", rng);
            if (!recorder.blocks.isEmpty()) {
                String logMat = findLogMaterial(recorder.blocks);
                assertEquals("jungle_log", logMat,
                        "Jungle biome should produce jungle trees, got: " + logMat);
            }
        }
    }

    @Test
    void place_birchForestBiome_usesBirch() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(22222);

        for (int i = 0; i < 100; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "birch_forest", rng);
            if (!recorder.blocks.isEmpty()) {
                String logMat = findLogMaterial(recorder.blocks);
                assertEquals("birch_log", logMat,
                        "Birch forest biome should produce birch trees, got: " + logMat);
            }
        }
    }

    @Test
    void place_darkForestBiome_usesDarkOakOrOak() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(33333);

        boolean sawDarkOak = false;
        for (int i = 0; i < 100; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "dark_forest", rng);
            if (!recorder.blocks.isEmpty()) {
                String logMat = findLogMaterial(recorder.blocks);
                assertTrue("dark_oak_log".equals(logMat) || "oak_log".equals(logMat),
                        "Dark forest biome should produce dark_oak or oak trees, got: " + logMat);
                if ("dark_oak_log".equals(logMat)) {
                    sawDarkOak = true;
                }
            }
        }
        assertTrue(sawDarkOak, "Dark forest biome should produce at least one dark_oak tree");
    }

    // ───────────────────────────────────────────────────────
    //  4. treeDensity=0 → no trees
    // ───────────────────────────────────────────────────────

    @Test
    void place_densityZero_placesNoBlocks() {
        var placer = new TreePlacer(0.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(FIXED_SEED);

        for (int i = 0; i < 50; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "forest", rng);
            assertTrue(recorder.blocks.isEmpty(),
                    "No blocks should be placed when treeDensity is 0");
        }
    }

    // ───────────────────────────────────────────────────────
    //  5. Biome with no trees → no trees
    // ───────────────────────────────────────────────────────

    @Test
    void place_biomeWithoutTrees_placesNothing() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(FIXED_SEED);

        // Desert has no tree types
        recorder.reset();
        placer.place(recorder, 0, 0, 60, "desert", rng);
        assertTrue(recorder.blocks.isEmpty(),
                "Desert biome should not produce any tree blocks");
    }

    @Test
    void place_oceanBiome_placesNothing() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(FIXED_SEED);

        recorder.reset();
        placer.place(recorder, 0, 0, 60, "ocean", rng);
        assertTrue(recorder.blocks.isEmpty(),
                "Ocean biome should not produce any tree blocks");
    }

    // ───────────────────────────────────────────────────────
    //  6. Tree shape verification — trunk always present
    // ───────────────────────────────────────────────────────

    @Test
    void place_treeHasTrunkBlocks() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(77777);

        placer.place(recorder, 10, 20, 60, "forest", rng);
        assertFalse(recorder.blocks.isEmpty(), "A tree should be placed");

        // At least one log should be present in the placed blocks
        boolean hasLog = recorder.blocks.stream()
                .anyMatch(b -> b[3].endsWith("_log"));
        assertTrue(hasLog, "Tree placement should include at least one log block");
    }

    @Test
    void place_treeTrunkStartsAboveSurface() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(88888);

        int surfaceY = 60;
        placer.place(recorder, 0, 0, surfaceY, "forest", rng);
        assertFalse(recorder.blocks.isEmpty(), "A tree should be placed");

        // All log blocks should be above the surface (y > surfaceY)
        boolean allLogsAboveSurface = recorder.blocks.stream()
                .filter(b -> b[3].endsWith("_log"))
                .allMatch(b -> Integer.parseInt(b[1]) > surfaceY);
        assertTrue(allLogsAboveSurface,
                "All log blocks should be above the surface height");
    }

    // ───────────────────────────────────────────────────────
    //  7. Biome tree map coverage
    // ───────────────────────────────────────────────────────

    @Test
    void biomeTreeMap_containsForest() {
        assertTrue(TreePlacer.biomeTreeMap().containsKey("forest"));
    }

    @Test
    void biomeTreeMap_allValuesAreNonEmpty() {
        var map = TreePlacer.biomeTreeMap();
        assertFalse(map.isEmpty(), "Biome tree map should not be empty");
        map.forEach((biome, types) -> {
            assertFalse(types.isEmpty(),
                    "Biome '" + biome + "' should have at least one tree type");
        });
    }

    @Test
    void biomeTreeMap_isUnmodifiable() {
        var map = TreePlacer.biomeTreeMap();
        assertThrows(UnsupportedOperationException.class,
                () -> map.put("test", List.of(TreeType.OAK)));
    }

    // ───────────────────────────────────────────────────────
    //  Helpers
    // ───────────────────────────────────────────────────────

    /**
     * Finds the first log material name from recorded blocks, or null if none.
     */
    private static String findLogMaterial(List<String[]> blocks) {
        return blocks.stream()
                .filter(b -> b[3].endsWith("_log"))
                .map(b -> b[3])
                .findFirst()
                .orElse(null);
    }
}
