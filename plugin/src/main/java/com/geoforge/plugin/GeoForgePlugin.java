package com.geoforge.plugin;


import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.GeoForgeConfig;
import com.geoforge.plugin.command.GeoForgeReloadCommand;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main plugin class for GeoForge world generation.
 *
 * <p>Initialises the version-specific adapter and the engine on enable, then provides the
 * custom {@link ChunkGenerator} for world creation.
 */
public final class GeoForgePlugin extends JavaPlugin {

    private GeoForgeAdapter adapter;
    private final ConcurrentHashMap<String, GeoForgeEngine> worldEngines = new ConcurrentHashMap<>();
    private GeoForgeConfig defaultConfig;
    private long worldSeed;

    @Override
    public void onEnable() {
        this.adapter = AdapterFactory.create(this);
        saveDefaultConfig();
        FileConfiguration cfg = getConfig();
        this.worldSeed = cfg.getLong("seed", 0L);

        // Build default engine config from config.yml
        this.defaultConfig = GeoForgeConfig.builder()
.minHeight(cfg.getInt("terrain.min-height", -64))
.maxHeight(cfg.getInt("terrain.max-height", 180))
.seaLevel(cfg.getInt("terrain.sea-level", 63))
.continentalBase(cfg.getDouble("terrain.continental-base", 50.0))
.continentalHeightAmplitude(cfg.getDouble("terrain.continental-height-amplitude", 120.0))
.temperatureFrequency(cfg.getDouble("climate.temperature-frequency", 0.001))
.temperatureYFrequency(cfg.getDouble("climate.temperature-y-frequency", 0.005))
.humidityFrequency(cfg.getDouble("climate.humidity-frequency", 0.001))
.caveFrequency(cfg.getDouble("noise.cave-frequency", 0.03))
.caveAmplitude(cfg.getDouble("noise.cave-amplitude", 8.0))
.caveOctaves(cfg.getInt("noise.cave-octaves", 3))
.caveLacunarity(cfg.getDouble("noise.cave-lacunarity", 2.0))
.cavePersistence(cfg.getDouble("noise.cave-persistence", 0.5))
.riverFrequency(cfg.getDouble("river.frequency", 0.01))
.riverDepth(cfg.getInt("river.depth", 8))
.riverWidth(cfg.getInt("river.width", 3))
.erosionMaxDropletSteps(cfg.getInt("erosion.max-droplet-steps", 10))
.erosionIterations(cfg.getInt("erosion.iterations", 64))
.caveCenterY(cfg.getDouble("cave.center-y", -20.0))
.caveSpread(cfg.getDouble("cave.spread", 48.0))
.caveSurfaceCutoff(cfg.getDouble("cave.surface-cutoff", 8.0))
.caveSpaghettiThreshold(cfg.getDouble("cave.spaghetti-threshold", 0.3))
.caveCheeseThreshold(cfg.getDouble("cave.cheese-threshold", 0.5))
.caveNoodleThreshold(cfg.getDouble("cave.noodle-threshold", 0.15))
.caveNoodleFrequency(cfg.getDouble("cave.noodle-frequency", 0.05))
.riverCanyonDepth(cfg.getInt("river.canyon-depth", 0))
.riverCanyonWidth(cfg.getInt("river.canyon-width", 2))
.riverValleyProfile(com.geoforge.engine.config.RiverProfile.fromString(cfg.getString("river.valley-profile", "vshaped")))
.riverFloodplainWidth(cfg.getInt("river.floodplain-width", 5))
.riverTableResponse(cfg.getDouble("river.table-response", 0.0))
.ridgeFrequency(cfg.getDouble("noise.ridge-frequency", 0.003))
.ridgeOctaves(cfg.getInt("noise.ridge-octaves", 3))
.ridgeAmplitude(cfg.getDouble("noise.ridge-amplitude", 1.0))
.fbmFrequency(cfg.getDouble("noise.fbm-frequency", 0.005))
.fbmOctaves(cfg.getInt("noise.fbm-octaves", 4))
.flatFrequency(cfg.getDouble("noise.flat-frequency", 0.008))
.continentalnessBlendSharpness(cfg.getDouble("noise.continentalness-blend-sharpness", 1.0))
.featureSeedOffset(cfg.getLong("decorations.feature-seed-offset", 0xCAFEBABEL))
        .treeDensityFrequency(cfg.getDouble("decorations.tree-density-frequency", 0.02))
.erosionDropletCount(cfg.getInt("erosion.droplet-count", 1024))
.erosionGravity((float) cfg.getDouble("erosion.gravity", 0.2))
.noiseBackend(cfg.getString("noise.backend", "gradient"))
            .domainWarpAmplitude(cfg.getDouble("domain-warp.amplitude", 1.5))
            .boundaryWarpFrequency(cfg.getDouble("boundary-warp.frequency", 0.001))
            .boundaryWarpAmplitude(cfg.getDouble("boundary-warp.amplitude", 0.15))
            .plateauSize(cfg.getInt("plateau.size", 0))
            .plateauTargetHeight(cfg.getInt("plateau.target-height", 64))
.configVersion(cfg.getInt("config-version", 4))
.build();

        // Auto-migrate config if outdated
        int configVersion = cfg.getInt("config-version", 4);
        if (configVersion < 4) {
            getLogger().info("Migrating config from version " + configVersion + " to 4...");
            var migratedConfig = com.geoforge.engine.config.ConfigMigrator.migrate(this.defaultConfig);
            // Write migrated config back to config.yml
            var configMap = migratedConfig.toConfigMap();
            for (var entry : configMap.entrySet()) {
                cfg.set(entry.getKey(), entry.getValue());
            }
            saveConfig();
            this.defaultConfig = migratedConfig;
            getLogger().info("Config migrated and saved (version " + configVersion + " \u2192 4)");
        } else if (configVersion > 4) {
            getLogger().warning("Config version " + configVersion + " is newer than expected (4) \u2014 plugin may need updating");
        }

        // Log config sanity warnings
        var sanityWarnings = defaultConfig.sanityCheck();
        if (!sanityWarnings.isEmpty()) {
            getLogger().warning("GeoForge config sanity warnings:");
            sanityWarnings.forEach(w -> getLogger().warning("  - " + w));
        }

        // Register reload command
        var reloadCmd = getCommand("geoforgereload");
        if (reloadCmd != null) {
            reloadCmd.setExecutor(new GeoForgeReloadCommand(this));
            getLogger().info("Registered /geoforgereload command");
        } else {
            getLogger().warning("Could not register /geoforgereload \u2014 not defined in plugin.yml");
        }

        // Register world unload listener for cleaning up per-world engines
        Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);

        getLogger().info(
                "GeoForge enabled | adapter=" + adapter.getClass().getSimpleName()
                        + " | folia=" + adapter.isFolia()
                        + " | seed=<redacted>"
                        + " | version=" + Bukkit.getMinecraftVersion()
                        + " | config-version=" + configVersion);
    }
    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(
            @NotNull String worldName, @Nullable String id) {
        // Sanitize world name to prevent path traversal
        String safeName = sanitizeWorldName(worldName);
        if (safeName == null) {
            getLogger().warning("Rejected invalid world name: '" + worldName + "'");
            return null;
        }
        GeoForgeEngine perWorldEngine = worldEngines.computeIfAbsent(safeName, wn -> {
            GeoForgeConfig wc = loadWorldConfig(wn);
            return new GeoForgeEngine(worldSeed, wc);
        });
        return new GeoForgeGenerator(adapter, perWorldEngine, worldSeed);
    }

    /**
     * Sanitizes a world name for safe filesystem use.
     * <p>Only allows alphanumeric, underscore, hyphen, and space characters.
     * Rejects names with path traversal sequences ({@code ..}, {@code /}, {@code \})
     * and names that are empty or too long.
     *
     * @param worldName the raw world name
     * @return sanitized name, or null if invalid
     */
    private static String sanitizeWorldName(String worldName) {
        if (worldName == null || worldName.isEmpty() || worldName.length() > 128) return null;
        if (worldName.contains("..") || worldName.contains("/") || worldName.contains("\\")) return null;
        // Allow only alphanumeric, underscore, hyphen, space
        for (int i = 0; i < worldName.length(); i++) {
            char c = worldName.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != ' ') return null;
        }
        return worldName;
    }

    private GeoForgeConfig loadWorldConfig(String worldName) {
        File worldFile = new File(getDataFolder(), "worlds/" + worldName + ".yml");
        if (!worldFile.exists()) return defaultConfig;
        FileConfiguration wcfg = YamlConfiguration.loadConfiguration(worldFile);
        return GeoForgeConfig.builder()
                .minHeight(wcfg.getInt("terrain.min-height", defaultConfig.minHeight()))
                .maxHeight(wcfg.getInt("terrain.max-height", defaultConfig.maxHeight()))
                .seaLevel(wcfg.getInt("terrain.sea-level", defaultConfig.seaLevel()))
                .continentalBase(wcfg.getDouble("terrain.continental-base", defaultConfig.continentalBase()))
                .continentalHeightAmplitude(wcfg.getDouble("terrain.continental-height-amplitude", defaultConfig.continentalHeightAmplitude()))
                .temperatureFrequency(wcfg.getDouble("climate.temperature-frequency", defaultConfig.temperatureFrequency()))
                .temperatureYFrequency(wcfg.getDouble("climate.temperature-y-frequency", defaultConfig.temperatureYFrequency()))
                .humidityFrequency(wcfg.getDouble("climate.humidity-frequency", defaultConfig.humidityFrequency()))
                .caveFrequency(wcfg.getDouble("noise.cave-frequency", defaultConfig.caveFrequency()))
                .caveAmplitude(wcfg.getDouble("noise.cave-amplitude", defaultConfig.caveAmplitude()))
                .caveOctaves(wcfg.getInt("noise.cave-octaves", defaultConfig.caveOctaves()))
                .caveLacunarity(wcfg.getDouble("noise.cave-lacunarity", defaultConfig.caveLacunarity()))
                .cavePersistence(wcfg.getDouble("noise.cave-persistence", defaultConfig.cavePersistence()))
                .riverFrequency(wcfg.getDouble("river.frequency", defaultConfig.riverFrequency()))
                .riverDepth(wcfg.getInt("river.depth", defaultConfig.riverDepth()))
                .riverWidth(wcfg.getInt("river.width", defaultConfig.riverWidth()))
                .erosionMaxDropletSteps(wcfg.getInt("erosion.max-droplet-steps", defaultConfig.erosionMaxDropletSteps()))
                .erosionIterations(wcfg.getInt("erosion.iterations", defaultConfig.erosionIterations()))
                .caveCenterY(wcfg.getDouble("cave.center-y", defaultConfig.caveCenterY()))
                .caveSpread(wcfg.getDouble("cave.spread", defaultConfig.caveSpread()))
                .caveSurfaceCutoff(wcfg.getDouble("cave.surface-cutoff", defaultConfig.caveSurfaceCutoff()))
                .caveSpaghettiThreshold(wcfg.getDouble("cave.spaghetti-threshold", defaultConfig.caveSpaghettiThreshold()))
                .caveCheeseThreshold(wcfg.getDouble("cave.cheese-threshold", defaultConfig.caveCheeseThreshold()))
                .caveNoodleThreshold(wcfg.getDouble("cave.noodle-threshold", defaultConfig.caveNoodleThreshold()))
                .caveNoodleFrequency(wcfg.getDouble("cave.noodle-frequency", defaultConfig.caveNoodleFrequency()))
                .riverCanyonDepth(wcfg.getInt("river.canyon-depth", defaultConfig.riverCanyonDepth()))
                .riverCanyonWidth(wcfg.getInt("river.canyon-width", defaultConfig.riverCanyonWidth()))
                .riverValleyProfile(wcfg.contains("river.valley-profile")
                        ? com.geoforge.engine.config.RiverProfile.fromString(wcfg.getString("river.valley-profile"))
                        : defaultConfig.riverValleyProfile())
                .riverFloodplainWidth(wcfg.getInt("river.floodplain-width", defaultConfig.riverFloodplainWidth()))
                .riverTableResponse(wcfg.getDouble("river.table-response", defaultConfig.riverTableResponse()))
                .ridgeFrequency(wcfg.getDouble("noise.ridge-frequency", defaultConfig.ridgeFrequency()))
                .ridgeOctaves(wcfg.getInt("noise.ridge-octaves", defaultConfig.ridgeOctaves()))
                .ridgeAmplitude(wcfg.getDouble("noise.ridge-amplitude", defaultConfig.ridgeAmplitude()))
                .fbmFrequency(wcfg.getDouble("noise.fbm-frequency", defaultConfig.fbmFrequency()))
                .fbmOctaves(wcfg.getInt("noise.fbm-octaves", defaultConfig.fbmOctaves()))
                .flatFrequency(wcfg.getDouble("noise.flat-frequency", defaultConfig.flatFrequency()))
                .continentalnessBlendSharpness(wcfg.getDouble("noise.continentalness-blend-sharpness", defaultConfig.continentalnessBlendSharpness()))
                .featureSeedOffset(wcfg.getLong("decorations.feature-seed-offset", defaultConfig.featureSeedOffset()))
                .treeDensityFrequency(wcfg.getDouble("decorations.tree-density-frequency", defaultConfig.treeDensityFrequency()))
                .erosionDropletCount(wcfg.getInt("erosion.droplet-count", defaultConfig.erosionDropletCount()))
                .erosionGravity((float) wcfg.getDouble("erosion.gravity", defaultConfig.erosionGravity()))
                .noiseBackend(wcfg.getString("noise.backend", defaultConfig.noiseBackend()))
                .domainWarpAmplitude(wcfg.getDouble("domain-warp.amplitude", defaultConfig.domainWarpAmplitude()))
                .boundaryWarpFrequency(wcfg.getDouble("boundary-warp.frequency", defaultConfig.boundaryWarpFrequency()))
                .boundaryWarpAmplitude(wcfg.getDouble("boundary-warp.amplitude", defaultConfig.boundaryWarpAmplitude()))
                .caveWarpAmplitude(wcfg.getDouble("cave.warp-amplitude", defaultConfig.caveWarpAmplitude()))
                .noodleWarpAmplitude(wcfg.getDouble("cave.noodle-warp-amplitude", defaultConfig.noodleWarpAmplitude()))
                .riverWarpAmplitude(wcfg.getDouble("river.warp-amplitude", defaultConfig.riverWarpAmplitude()))
                .plateauSize(wcfg.getInt("plateau.size", defaultConfig.plateauSize()))
                .plateauTargetHeight(wcfg.getInt("plateau.target-height", defaultConfig.plateauTargetHeight()))
                .configVersion(wcfg.getInt("config-version", defaultConfig.configVersion()))
                .build();
    }

    /**
     * Returns the engine for the given world name.
     *
     * @param worldName the world name
     * @return the GeoForgeEngine, or null if not yet created
     */
    public GeoForgeEngine getEngine(String worldName) {
        return worldEngines.get(worldName);
    }

    /**
     * Returns the default engine (from the first available world) for backward compatibility.
     *
     * @return the first GeoForgeEngine, or null if no world engines exist
     */
    public GeoForgeEngine getEngine() {
        return worldEngines.isEmpty() ? null : worldEngines.values().iterator().next();
    }

    /**
     * Returns an unmodifiable view of the per-world engine map.
     *
     * @return map of world name to engine
     */
    public Map<String, GeoForgeEngine> getWorldEngines() {
        return Collections.unmodifiableMap(worldEngines);
    }

    /**
     * Returns the world generation seed.
     *
     * @return the seed
     */
    public long getWorldSeed() {
        return worldSeed;
    }

    /**
     * Listener for cleaning up per-world engines on world unload.
     */
    private final class BukkitListener implements Listener {
        @EventHandler
        public void onWorldUnload(WorldUnloadEvent event) {
            GeoForgeEngine removed = worldEngines.remove(event.getWorld().getName());
            if (removed != null) {
                getLogger().info("Cleaned up GeoForge engine for world: " + event.getWorld().getName());
            }
        }
    }
}
