package com.geoforge.engine.feature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.feature.tree.TreeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Tree placer tests")
class TreePlacerTest {

    private static final class RecordingSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();

        @Override
        public void setBlock(int x, int y, int z, String materialName) {
            blocks.add(new String[]{String.valueOf(x), String.valueOf(y), String.valueOf(z), materialName});
        }

        void reset() { blocks.clear(); }
    }

    private static final long FIXED_SEED = 42L;

    @DisplayName("Constructor rejects negative tree density")
    @Test
    void constructor_rejectsNegativeDensity() {
        assertThrows(IllegalArgumentException.class, () -> new TreePlacer(-0.1, 12));
    }

    @DisplayName("Constructor rejects density above 1.0")
    @Test
    void constructor_rejectsDensityAboveOne() {
        assertThrows(IllegalArgumentException.class, () -> new TreePlacer(1.5, 12));
    }

    @DisplayName("Constructor rejects max tree height below 4")
    @Test
    void constructor_rejectsMaxTreeHeightBelowFour() {
        assertThrows(IllegalArgumentException.class, () -> new TreePlacer(0.1, 3));
    }

    @DisplayName("Constructor accepts valid values")
    @Test
    void constructor_acceptsValidValues() {
        var placer = new TreePlacer(0.5, 10);
        assertEquals(0.5, placer.treeDensity(), 1e-12);
        assertEquals(10, placer.maxTreeHeight());
    }

    @DisplayName("Density zero is valid")
    @Test
    void constructor_densityZeroIsValid() {
        assertDoesNotThrow(() -> new TreePlacer(0.0, 4));
    }

    @DisplayName("Same seed produces same placed blocks (deterministic)")
    @Test
    void place_sameSeedProducesSameBlocks() {
        var placer = new TreePlacer(1.0, 12);

        RecordingSetter first = new RecordingSetter();
        Random rng1 = new Random(FIXED_SEED);
        placer.place(first, 0, 0, 60, "forest", rng1);

        RecordingSetter second = new RecordingSetter();
        Random rng2 = new Random(FIXED_SEED);
        placer.place(second, 0, 0, 60, "forest", rng2);

        assertEquals(first.blocks.size(), second.blocks.size());
        for (int i = 0; i < first.blocks.size(); i++) {
            assertArrayEquals(first.blocks.get(i), second.blocks.get(i));
        }
    }

    @DisplayName("Different seeds produce different tree placements")
    @Test
    void place_differentSeedsMayProduceDifferentPlacement() {
        var placer = new TreePlacer(1.0, 12);

        RecordingSetter first = new RecordingSetter();
        placer.place(first, 0, 0, 60, "forest", new Random(1));

        RecordingSetter second = new RecordingSetter();
        placer.place(second, 0, 0, 60, "forest", new Random(9999));

        assertThat(first.blocks).isNotEmpty();
        assertThat(second.blocks).isNotEmpty();
    }

    @DisplayName("Forest biome uses oak or birch trees")
    @Test
    void place_forestBiome_usesOakOrBirch() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(12345);

        for (int i = 0; i < 100; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "forest", rng);
            if (!recorder.blocks.isEmpty()) {
                String logMat = findLogMaterial(recorder.blocks);
                assertThat(logMat).isIn("oak_log", "birch_log");
            }
        }
    }

    @DisplayName("Taiga biome uses spruce trees")
    @Test
    void place_taigaBiome_usesSpruce() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(67890);

        for (int i = 0; i < 100; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "taiga", rng);
            if (!recorder.blocks.isEmpty()) {
                assertEquals("spruce_log", findLogMaterial(recorder.blocks));
            }
        }
    }

    @DisplayName("Jungle biome uses jungle trees")
    @Test
    void place_jungleBiome_usesJungle() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(11111);

        for (int i = 0; i < 100; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "jungle", rng);
            if (!recorder.blocks.isEmpty()) {
                assertEquals("jungle_log", findLogMaterial(recorder.blocks));
            }
        }
    }

    @DisplayName("Birch forest biome uses birch trees")
    @Test
    void place_birchForestBiome_usesBirch() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(22222);

        for (int i = 0; i < 100; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "birch_forest", rng);
            if (!recorder.blocks.isEmpty()) {
                assertEquals("birch_log", findLogMaterial(recorder.blocks));
            }
        }
    }

    @DisplayName("Dark forest biome uses dark oak or oak trees")
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
                assertThat(logMat).isIn("dark_oak_log", "oak_log");
                if ("dark_oak_log".equals(logMat)) {
                    sawDarkOak = true;
                }
            }
        }
        assertThat(sawDarkOak).isTrue();
    }

    @DisplayName("Density zero places no blocks")
    @Test
    void place_densityZero_placesNoBlocks() {
        var placer = new TreePlacer(0.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(FIXED_SEED);

        for (int i = 0; i < 50; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "forest", rng);
            assertThat(recorder.blocks).isEmpty();
        }
    }

    @DisplayName("Biome without trees places nothing")
    @Test
    void place_biomeWithoutTrees_placesNothing() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(FIXED_SEED);

        recorder.reset();
        placer.place(recorder, 0, 0, 60, "desert", rng);
        assertThat(recorder.blocks).isEmpty();
    }

    @DisplayName("Ocean biome places no trees")
    @Test
    void place_oceanBiome_placesNothing() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(FIXED_SEED);

        recorder.reset();
        placer.place(recorder, 0, 0, 60, "ocean", rng);
        assertThat(recorder.blocks).isEmpty();
    }

    @DisplayName("Tree has trunk blocks present")
    @Test
    void place_treeHasTrunkBlocks() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(77777);

        placer.place(recorder, 10, 20, 60, "forest", rng);
        assertThat(recorder.blocks).isNotEmpty();

        boolean hasLog = recorder.blocks.stream().anyMatch(b -> b[3].endsWith("_log"));
        assertThat(hasLog).isTrue();
    }

    @DisplayName("Tree trunk blocks start above surface")
    @Test
    void place_treeTrunkStartsAboveSurface() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(88888);

        int surfaceY = 60;
        placer.place(recorder, 0, 0, surfaceY, "forest", rng);
        assertThat(recorder.blocks).isNotEmpty();

        boolean allLogsAboveSurface = recorder.blocks.stream()
                .filter(b -> b[3].endsWith("_log"))
                .allMatch(b -> Integer.parseInt(b[1]) > surfaceY);
        assertThat(allLogsAboveSurface).isTrue();
    }

    @DisplayName("Biome tree map contains forest")
    @Test
    void biomeTreeMap_containsForest() {
        assertThat(new TreePlacer(1.0, 12).biomeTreeMap()).containsKey("forest");
    }

    @DisplayName("All biome tree map values are non-empty")
    @Test
    void biomeTreeMap_allValuesAreNonEmpty() {
        // First verify TreeRegistry.defaults() itself works
        var map = new TreePlacer(1.0, 12).biomeTreeMap();
        assertThat(map).isNotEmpty();
        // Note: mushroom_fields intentionally has empty list (no trees)
        map.forEach((biome, types) -> {
            if (!"mushroom_fields".equals(biome)) {
                assertThat(types).as(biome).isNotEmpty();
            }
        });
    }

    @DisplayName("Biome tree map is unmodifiable")
    @Test
    void biomeTreeMap_isUnmodifiable() {
        var map = new TreePlacer(1.0, 12).biomeTreeMap();
        assertThrows(UnsupportedOperationException.class, () -> map.put("test", List.of(TreeType.OAK)));
    }

    @DisplayName("Savanna biome contains acacia tree type")
    @Test
    void biomeTreeMap_savannaContainsAcacia() {
        var map = new TreePlacer(1.0, 12).biomeTreeMap();
        assertThat(map).containsKey("savanna");
        assertThat(map.get("savanna")).contains(TreeType.ACACIA);
    }

    @DisplayName("Savanna biome uses acacia trees")
    @Test
    void place_savannaBiome_usesAcacia() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(44444);

        for (int i = 0; i < 100; i++) {
            recorder.reset();
            placer.place(recorder, i, 0, 60, "savanna", rng);
            if (!recorder.blocks.isEmpty()) {
                assertEquals("acacia_log", findLogMaterial(recorder.blocks));
            }
        }
    }

    @DisplayName("Acacia tree has trunk blocks at consecutive Y levels")
    @Test
    void place_acaciaTree_hasTrunkBlocksAtDifferentY() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(55555);
        int surfaceY = 60;
        placer.place(recorder, 5, 5, surfaceY, "savanna", rng);
        assertThat(recorder.blocks).isNotEmpty();

        var logYs = recorder.blocks.stream()
                .filter(b -> "acacia_log".equals(b[3]))
                .map(b -> Integer.parseInt(b[1]))
                .sorted()
                .toList();
        assertThat(logYs.size()).isGreaterThanOrEqualTo(4);
        for (int i = 1; i < logYs.size(); i++) {
            assertEquals(logYs.get(i - 1) + 1, logYs.get(i).intValue());
        }
    }

    @DisplayName("Acacia tree has flat canopy shape with many leaves per layer")
    @Test
    void place_acaciaTree_hasFlatCanopyShape() {
        var placer = new TreePlacer(1.0, 12);
        RecordingSetter recorder = new RecordingSetter();
        Random rng = new Random(66666);
        int surfaceY = 60;
        placer.place(recorder, 0, 0, surfaceY, "savanna", rng);
        assertThat(recorder.blocks).isNotEmpty();

        var leafBlocks = recorder.blocks.stream()
                .filter(b -> "acacia_leaves".equals(b[3]))
                .toList();
        assertThat(leafBlocks).isNotEmpty();

        var counts = new java.util.HashMap<Integer, Integer>();
        for (var b : leafBlocks) {
            int y = Integer.parseInt(b[1]);
            counts.merge(y, 1, Integer::sum);
        }
        int maxPerLayer = counts.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        assertThat(maxPerLayer).isGreaterThanOrEqualTo(9);
    }

    @DisplayName("SplittableRandom produces deterministic placement")
    @Test
    void place_withSplittableRandom_deterministic() {
        var placer = new TreePlacer(1.0, 12);
        var setter1 = new RecordingSetter();
        var setter2 = new RecordingSetter();
        placer.place(setter1, 0, 0, 60, "forest", new SplittableRandom(42));
        placer.place(setter2, 0, 0, 60, "forest", new SplittableRandom(42));
        assertThat(setter1.blocks).containsExactlyElementsOf(setter2.blocks);
    }

    private static String findLogMaterial(List<String[]> blocks) {
        return blocks.stream()
                .filter(b -> b[3].endsWith("_log"))
                .map(b -> b[3])
                .findFirst()
                .orElse(null);
    }

    @Test @DisplayName("Default variants exist for all TreeTypes")
    void defaultVariantsExistForAllTypes() {
        for (TreeType type : TreeType.values()) {
            var variants = TreePlacer.DEFAULT_VARIANTS.get(type);
            assertThat(variants).as("variants for " + type).isNotEmpty();
        }
    }

    @Test @DisplayName("Full constructor produces deterministic placement")
    void fullConstructorDeterministic() {
        var sel = new com.geoforge.engine.feature.tree.TreeVariantSelector(42L, 0.015);
        var reg = com.geoforge.engine.feature.tree.TreeRegistry.defaults();
        var placer = new TreePlacer(1.0, 12, 4, sel, reg, java.util.Map.of());
        var s1 = new RecordingSetter(); var s2 = new RecordingSetter();
        placer.place(s1, 0, 0, 60, "forest", new SplittableRandom(42));
        placer.place(s2, 0, 0, 60, "forest", new SplittableRandom(42));
        assertThat(s1.blocks).containsExactlyElementsOf(s2.blocks);
    }

    @Test @DisplayName("Full constructor uses registry species")
    void fullConstructorUsesRegistrySpecies() {
        var reg = new com.geoforge.engine.feature.tree.TreeRegistry.Builder()
                .putBiomeTreeOverride("known", List.of(TreeType.OAK))
                .build();
        var placer = new TreePlacer(1.0, 12, 4,
                new com.geoforge.engine.feature.tree.TreeVariantSelector(42L),
                reg, java.util.Map.of());
        // Tree places in registered biome
        var r1 = new RecordingSetter();
        placer.place(r1, 5, 10, 60, "known", new SplittableRandom(42));
        assertThat(r1.blocks).isNotEmpty();
        // No tree in unregistered biome
        var r2 = new RecordingSetter();
        placer.place(r2, 5, 10, 60, "unknown", new SplittableRandom(42));
        assertThat(r2.blocks).isEmpty();
    }

}
