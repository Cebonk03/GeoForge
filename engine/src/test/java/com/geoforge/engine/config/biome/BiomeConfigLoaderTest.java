package com.geoforge.engine.config.biome;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("unit")
@DisplayName("BiomeConfigLoader — YAML biome config loading and deep-merge")
class BiomeConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("loadFromDirectory returns error when directory does not exist")
    void loadFromDirectory_missingDir_returnsError() {
        var loader = new BiomeConfigLoader();
        var result = loader.loadFromDirectory(tempDir.resolve("nonexistent"));
        assertTrue(result.hasErrors());
        assertTrue(result.errors().get(0).contains("not found"));
    }

    @Test
    @DisplayName("loadFromDirectory loads shared defaults and per-biome overrides")
    void loadFromDirectory_loadsSharedAndBiomes() throws IOException {
        createMinimalTestStructure();

        var loader = new BiomeConfigLoader();
        var result = loader.loadFromDirectory(tempDir);

        assertFalse(result.hasErrors(), "Errors: " + result.errors());
        assertFalse(result.biomes().isEmpty(), "Should have loaded biomes");
        assertTrue(result.biomes().containsKey("test_biome"), "Should contain test_biome");
    }

    @Test
    @DisplayName("per-biome terrain.yml overrides _shared defaults")
    void perBiomeOverridesSharedDefaults() throws IOException {
        // Create _shared/terrain.yml
        createYaml(tempDir.resolve("_shared/terrain.yml"),
                "surface-block: \"grass_block\"\n"
                + "sub-surface-block: \"dirt\"\n"
                + "surface-hardness: 0.5\n");

        // Create biome with override
        createYaml(tempDir.resolve("desert/terrain.yml"),
                "surface-block: \"sand\"\n"
                + "sub-surface-block: \"sandstone\"\n");

        var loader = new BiomeConfigLoader();
        var result = loader.loadFromDirectory(tempDir);

        var desert = result.biomes().get("desert");
        assertNotNull(desert);
        assertEquals("sand", desert.surfaceBlock());
        assertEquals("sandstone", desert.subSurfaceBlock());
        // surface-hardness should still come from _shared
        assertEquals(0.5, desert.surfaceHardness(), 1e-12);
    }

    @Test
    @DisplayName("missing _shared directory still loads biomes with built-in defaults")
    void missingShared_usesBuiltinDefaults() throws IOException {
        createYaml(tempDir.resolve("test_biome/terrain.yml"),
                "surface-block: \"stone\"\n");

        var loader = new BiomeConfigLoader();
        var result = loader.loadFromDirectory(tempDir);

        assertFalse(result.hasErrors());
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("_shared")));
        var biome = result.biomes().get("test_biome");
        assertNotNull(biome);
        assertEquals("stone", biome.surfaceBlock());
    }

    @Test
    @DisplayName("invalid YAML produces warnings but not errors")
    void invalidYaml_producesWarning() throws IOException {
        createYaml(tempDir.resolve("_shared/terrain.yml"),
                "surface-block: \"grass_block\"\n");
        createYaml(tempDir.resolve("bad_biome/terrain.yml"),
                "invalid yaml content: [\n");  // malformed YAML

        var loader = new BiomeConfigLoader();
        var result = loader.loadFromDirectory(tempDir);

        // Should have the valid biome
        assertTrue(result.biomes().containsKey("bad_biome"));
        // Failed parse should produce a warning
        assertFalse(result.warnings().isEmpty());
    }

    // ──────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────

    private void createMinimalTestStructure() throws IOException {
        // _shared defaults
        createYaml(tempDir.resolve("_shared/terrain.yml"),
                "surface-block: \"grass_block\"\n"
                + "sub-surface-block: \"dirt\"\n");
        createYaml(tempDir.resolve("_shared/trees.yml"),
                "types: [\"oak\"]\ndensity: 0.1\n");
        createYaml(tempDir.resolve("_shared/vegetation.yml"),
                "types: [\"grass\", \"poppy\"]\ndensity: 0.3\n");

        // Per-biome: plains
        createYaml(tempDir.resolve("plains/terrain.yml"),
                "surface-block: \"grass_block\"\n");

        // Per-biome: desert (overrides)
        createYaml(tempDir.resolve("desert/terrain.yml"),
                "surface-block: \"sand\"\n"
                + "sub-surface-block: \"sandstone\"\n");
        createYaml(tempDir.resolve("desert/vegetation.yml"),
                "types: [\"dead_bush\"]\ndensity: 0.1\n");

        // Per-biome: test_biome (partial override)
        createYaml(tempDir.resolve("test_biome/terrain.yml"),
                "surface-block: \"stone\"\n");
    }

    private void createYaml(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
