package com.geoforge.engine.config.biome;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Loads biome definitions from YAML files on disk or classpath resources.
 *
 * <p>Directory structure expected:
 * <pre>
 * Biomes/
 * &#47;-- _shared/
 * &#47;--   terrain.yml
 * &#47;--   trees.yml
 * &#47;--   vegetation.yml
 * &#47;--   cave.yml
 * &#47;--   river.yml
 * &#47;-- biome1/
 * &#47;--   terrain.yml       (partial — only overrides)
 * &#47;--   trees.yml
 * &#47;-- biome2/
 * &#47;--   terrain.yml
 * ...
 * </pre>
 *
 * <p>Per-biome files are partial: they override only the fields that differ
 * from {@code _shared/}. The deep-merge algorithm applies per-field overrides.
 */
@SuppressFBWarnings({"DLS_DEAD_LOCAL_STORE", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
public final class BiomeConfigLoader {

    private static final String SHARED_DIR = "_shared";
    private static final String CLIMATE_FILE = "climate.yml";
    private static final List<String> BIOME_FILES = List.of(
            "terrain.yml", "trees.yml", "vegetation.yml", "cave.yml", "river.yml");

    private final Yaml yaml;

    /** Creates a loader with a default SnakeYAML instance. */
    public BiomeConfigLoader() {
        var opts = new LoaderOptions();
        opts.setAllowDuplicateKeys(false);
        opts.setMaxAliasesForCollections(10);
        this.yaml = new Yaml(new SafeConstructor(opts));
    }

    /**
     * Loads all biome definitions from the given directory.
     *
     * @param biomesDir the root biomes directory (containing {@code _shared/}
     *                  and per-biome subdirectories)
     * @return the load result with parsed biomes, errors, and warnings
     */
    public BiomeLoadResult loadFromDirectory(Path biomesDir) {
        if (!Files.isDirectory(biomesDir)) {
            return new BiomeLoadResult(Map.of(),
                    List.of("Biomes directory not found: " + biomesDir),
                    List.of());
        }

        var errors = new ArrayList<String>();
        var warnings = new ArrayList<String>();

        // 1. Load shared defaults
        BiomeDefinition sharedDefaults = loadSharedDefaults(biomesDir, errors, warnings);
        if (sharedDefaults == null) {
            sharedDefaults = BiomeDefinition.defaults();
            warnings.add("No _shared/ defaults found, using built-in defaults");
        }

        // 2. Load climate config
        ClimateResolver.ClimateConfig climateConfig = loadClimateConfig(biomesDir, errors, warnings);

        // 3. Load per-biome overrides
        Map<String, BiomeDefinition> biomes = new LinkedHashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(biomesDir)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry)) continue;
                String dirName = entry.getFileName().toString();
                if (dirName.equals(SHARED_DIR) || dirName.startsWith(".")) continue;

                BiomeDefinition def = loadSingleBiome(entry, dirName, sharedDefaults,
                        errors, warnings, climateConfig);
                if (def != null) {
                    biomes.put(dirName, def);
                }
            }
        } catch (IOException e) {
            errors.add("Failed to list biomes directory: " + e.getMessage());
        }

        return new BiomeLoadResult(Map.copyOf(biomes),
                List.copyOf(errors), List.copyOf(warnings));
    }

    /**
     * Loads biome definitions from classpath resources (for testing/embedded use).
     *
     * @param loader       the class loader to resolve resources
     * @param resourcePath the root resource path (e.g. {@code "biomes"})
     * @return the load result
     */
    public BiomeLoadResult loadFromResources(ClassLoader loader, String resourcePath) {
        var errors = new ArrayList<String>();
        var warnings = new ArrayList<String>();

        BiomeDefinition sharedDefaults = loadSharedDefaultsFromResources(
                loader, resourcePath, errors, warnings);
        if (sharedDefaults == null) {
            sharedDefaults = BiomeDefinition.defaults();
        }

        var biomes = new LinkedHashMap<String, BiomeDefinition>();

        // Load from resource paths matching /Biomes/<name>/terrain.yml etc.
        // This requires knowing biome names at compile time; for runtime use loadFromDirectory().
        // Resource loading is primarily for test resources.
        return new BiomeLoadResult(Map.copyOf(biomes),
                List.copyOf(errors), List.copyOf(warnings));
    }

    /** Parses a YAML file into a raw map for structured access. */
    @SuppressWarnings("unchecked")
    Map<String, Object> loadYamlFile(Path file) {
        try (InputStream in = new FileInputStream(file.toFile())) {
            Object raw = yaml.load(in);
            if (raw instanceof Map) {
                return (Map<String, Object>) raw;
            }
            return Map.of();
        } catch (IOException | YAMLException e) {
            return null;
        }
    }

    // ──────────────────────────────────────────────
    //  Private helpers
    // ──────────────────────────────────────────────

    private BiomeDefinition loadSharedDefaults(Path biomesDir,
                                                ArrayList<String> errors,
                                                ArrayList<String> warnings) {
        Path sharedDir = biomesDir.resolve(SHARED_DIR);
        if (!Files.isDirectory(sharedDir)) return null;

        BiomeDefinition def = BiomeDefinition.defaults();
        for (String fileName : BIOME_FILES) {
            Path file = sharedDir.resolve(fileName);
            if (Files.isRegularFile(file)) {
                Map<String, Object> data = loadYamlFile(file);
                if (data != null) {
                    def = applyYamlOverrides(def, data, "_shared/" + fileName, warnings);
                }
            }
        }
        return def;
    }

    private BiomeDefinition loadSingleBiome(Path biomeDir, String biomeId,
                                             BiomeDefinition sharedDefaults,
                                             ArrayList<String> errors,
                                             ArrayList<String> warnings,
                                             ClimateResolver.ClimateConfig climateConfig) {
        BiomeDefinition merged = sharedDefaults;
        boolean hasFiles = false;

        for (String fileName : BIOME_FILES) {
            Path file = biomeDir.resolve(fileName);
            if (Files.isRegularFile(file)) {
                Map<String, Object> data = loadYamlFile(file);
                if (data != null) {
                    merged = applyYamlOverrides(merged, data, biomeId + "/" + fileName, warnings);
                    hasFiles = true;
                } else {
                    warnings.add(biomeId + "/" + fileName + ": failed to parse");
                }
            }
        }

        if (!hasFiles) {
            warnings.add("Biome '" + biomeId + "' has no config files, using _shared defaults");
        }

        // If climate config is available, look for climate envelope in data
        // (climate envelopes are defined in climate.yml, not per-biome files)
        if (climateConfig != null) {
            merged = new BiomeDefinition(
                    biomeId,
                    merged.heightOffset(), merged.amplitudeMultiplier(),
                    merged.surfaceBlock(), merged.subSurfaceBlock(),
                    merged.surfaceHardness(), merged.caveAmplitudeModifier(),
                    merged.treeType(), merged.treeDensity(),
                    merged.minTreeHeight(), merged.maxTreeHeight(),
                    merged.treeVariantModifiers(),
                    merged.vegetationTypes(), merged.vegetationDensity(),
                    merged.allowFloatingPlants(),
                    merged.tempMin(), merged.tempMax(),
                    merged.humidityMin(), merged.humidityMax(),
                    merged.continentalnessMin(), merged.continentalnessMax(),
                    merged.priority());
        } else if (merged.id().isEmpty()) {
            // Set biome ID from directory name
            merged = new BiomeDefinition(
                    biomeId,
                    merged.heightOffset(), merged.amplitudeMultiplier(),
                    merged.surfaceBlock(), merged.subSurfaceBlock(),
                    merged.surfaceHardness(), merged.caveAmplitudeModifier(),
                    merged.treeType(), merged.treeDensity(),
                    merged.minTreeHeight(), merged.maxTreeHeight(),
                    merged.treeVariantModifiers(),
                    merged.vegetationTypes(), merged.vegetationDensity(),
                    merged.allowFloatingPlants(),
                    merged.tempMin(), merged.tempMax(),
                    merged.humidityMin(), merged.humidityMax(),
                    merged.continentalnessMin(), merged.continentalnessMax(),
                    merged.priority());
        }

        return merged;
    }

    @SuppressWarnings("unchecked")
    private BiomeDefinition applyYamlOverrides(BiomeDefinition base,
                                                Map<String, Object> data,
                                                String source,
                                                ArrayList<String> warnings) {
        double heightOffset = getDouble(data, "height-offset", base.heightOffset());
        double amplitudeMul = getDouble(data, "amplitude-multiplier", base.amplitudeMultiplier());
        String surfaceBlock = getString(data, "surface-block", base.surfaceBlock());
        String subSurfaceBlock = getString(data, "sub-surface-block", base.subSurfaceBlock());
        double surfaceHardness = getDouble(data, "surface-hardness", base.surfaceHardness());
        double caveAmpMod = getDouble(data, "amplitude-modifier", base.caveAmplitudeModifier());
        String treeType = getString(data, "tree-type", base.treeType());
        double treeDensity = getDouble(data, "density", base.treeDensity());
        int minTreeH = getInt(data, "min-height", base.minTreeHeight());
        int maxTreeH = getInt(data, "max-height", base.maxTreeHeight());

        // Trees types list (for trees.yml)
        List<String> treeTypes = (List<String>) data.get("types");
        String effectiveTreeType = treeType;
        if (treeTypes != null && !treeTypes.isEmpty() && effectiveTreeType.isEmpty()) {
            effectiveTreeType = treeTypes.get(0).toUpperCase();
        }

        // Vegetation types — only from explicit "vegetation-types" key, not from "types"
        List<String> effectiveVegTypes = base.vegetationTypes();
        if (data.containsKey("vegetation-types")) {
            effectiveVegTypes = List.copyOf((List<String>) data.get("vegetation-types"));
        }

        double vegDensity = getDouble(data, "vegetation-density", base.vegetationDensity());
        boolean allowFloat = data.containsKey("allow-floating-plants")
                ? Boolean.TRUE.equals(data.get("allow-floating-plants"))
                : base.allowFloatingPlants();

        // Tree variant modifiers
        Map<String, Double> variantMods = base.treeVariantModifiers();
        if (data.containsKey("variant-modifiers")) {
            Object raw = data.get("variant-modifiers");
            if (raw instanceof Map) {
                var rawMap = (Map<String, Object>) raw;
                var mods = new LinkedHashMap<String, Double>();
                for (var e : rawMap.entrySet()) {
                    if (e.getValue() instanceof Number n) {
                        mods.put(e.getKey(), n.doubleValue());
                    }
                }
                variantMods = Map.copyOf(mods);
            }
        }

        // Climate envelope
        double tMin = getDouble(data, "temp-min", base.tempMin());
        double tMax = getDouble(data, "temp-max", base.tempMax());
        double hMin = getDouble(data, "humidity-min", base.humidityMin());
        double hMax = getDouble(data, "humidity-max", base.humidityMax());
        double cMin = getDouble(data, "continentalness-min", base.continentalnessMin());
        double cMax = getDouble(data, "continentalness-max", base.continentalnessMax());
        int prio = getInt(data, "priority", base.priority());

        return new BiomeDefinition(
                base.id(),
                heightOffset, amplitudeMul,
                surfaceBlock, subSurfaceBlock, surfaceHardness,
                caveAmpMod, effectiveTreeType, treeDensity,
                minTreeH, maxTreeH,
                variantMods, effectiveVegTypes, vegDensity, allowFloat,
                tMin, tMax, hMin, hMax, cMin, cMax, prio);
    }

    private ClimateResolver.ClimateConfig loadClimateConfig(Path biomesDir,
                                                             ArrayList<String> errors,
                                                             ArrayList<String> warnings) {
        Path climateFile = biomesDir.resolve(CLIMATE_FILE);
        if (!Files.isRegularFile(climateFile)) {
            warnings.add("No climate.yml found, using default climate sampling");
            return new ClimateResolver.ClimateConfig(0.001, 0.005, 0.001);
        }
        Map<String, Object> data = loadYamlFile(climateFile);
        if (data == null) {
            warnings.add("Failed to parse climate.yml, using defaults");
            return new ClimateResolver.ClimateConfig(0.001, 0.005, 0.001);
        }

        double tempFreq = nestedDouble(data, "sampling", "temperature", "frequency", 0.001);
        double tempYFreq = nestedDouble(data, "sampling", "temperature", "y-frequency", 0.005);
        double humFreq = nestedDouble(data, "sampling", "humidity", "frequency", 0.001);
        return new ClimateResolver.ClimateConfig(tempFreq, tempYFreq, humFreq);
    }

    // ──────────────────────────────────────────────
    //  Resource-based loading
    // ──────────────────────────────────────────────

    private BiomeDefinition loadSharedDefaultsFromResources(ClassLoader loader,
                                                             String resourcePath,
                                                             ArrayList<String> errors,
                                                             ArrayList<String> warnings) {
        BiomeDefinition def = BiomeDefinition.defaults();
        for (String fileName : BIOME_FILES) {
            String resPath = resourcePath + "/" + SHARED_DIR + "/" + fileName;
            try (InputStream in = loader.getResourceAsStream(resPath)) {
                if (in != null) {
                    Map<String, Object> data = yaml.load(in);
                    if (data != null) {
                        def = applyYamlOverrides(def, data, resPath, warnings);
                    }
                }
            } catch (IOException | YAMLException e) {
                warnings.add("Failed to load resource " + resPath + ": " + e.getMessage());
            }
        }
        return def;
    }

    // ──────────────────────────────────────────────
    //  Type-safe extraction helpers
    // ──────────────────────────────────────────────

    private static double getDouble(Map<String, Object> data, String key, double fallback) {
        Object val = data.get(key);
        if (val instanceof Number n) return n.doubleValue();
        return fallback;
    }

    private static int getInt(Map<String, Object> data, String key, int fallback) {
        Object val = data.get(key);
        if (val instanceof Number n) return n.intValue();
        return fallback;
    }

    private static String getString(Map<String, Object> data, String key, String fallback) {
        Object val = data.get(key);
        if (val instanceof String s) return s;
        return fallback;
    }

    @SuppressWarnings("unchecked")
    private static double nestedDouble(Map<String, Object> data, String... keys) {
        Map<String, Object> current = data;
        for (int i = 0; i < keys.length - 1; i++) {
            Object next = current.get(keys[i]);
            if (!(next instanceof Map)) return 0.0;
            current = (Map<String, Object>) next;
        }
        Object val = current.get(keys[keys.length - 1]);
        if (val instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    private static double nestedDouble(Map<String, Object> data, String k1, String k2,
                                        String k3, double fallback) {
        double v = nestedDouble(data, k1, k2, k3);
        return v != 0.0 ? v : fallback;
    }
}
