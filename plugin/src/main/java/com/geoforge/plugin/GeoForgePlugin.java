package com.geoforge.plugin;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.GeoForgeConfig;
import com.geoforge.engine.config.RiverProfile;
import com.geoforge.plugin.command.GeoForgeReloadCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main plugin class for GeoForge world generation.
 *
 * <p>Initialises the version-specific adapter and the engine on enable, then provides the
 * custom {@link ChunkGenerator} for world creation.
 */
public final class GeoForgePlugin extends JavaPlugin {

    private GeoForgeAdapter adapter;
    private GeoForgeEngine engine;
    private long worldSeed;

    @Override
    public void onEnable() {
        this.adapter = AdapterFactory.create(this);
        saveDefaultConfig();
        FileConfiguration cfg = getConfig();
        this.worldSeed = cfg.getLong("seed", 0L);

        // Config version check for future migration support
        int configVersion = cfg.getInt("config-version", 4);
        int expectedVersion = 4;
        if (configVersion != expectedVersion) {
            getLogger().warning(
                    "Expected config-version=" + expectedVersion
                            + " but found " + configVersion
                            + " — config may be outdated. Re-save to upgrade.");
        }

        GeoForgeConfig engineConfig = GeoForgeConfig.builder()
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
.riverValleyProfile(RiverProfile.fromString(cfg.getString("river.valley-profile", "vshaped")))
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
.noiseBackend(cfg.getString("noise.backend", "simplex"))
            .domainWarpAmplitude(cfg.getDouble("domain-warp.amplitude", 1.5))
            .boundaryWarpFrequency(cfg.getDouble("boundary-warp.frequency", 0.001))
            .boundaryWarpAmplitude(cfg.getDouble("boundary-warp.amplitude", 0.15))
            .plateauSize(cfg.getInt("plateau.size", 0))
            .plateauTargetHeight(cfg.getInt("plateau.target-height", 64))
                .configVersion(cfg.getInt("config-version", 4))
.build();
        this.engine = new GeoForgeEngine(worldSeed, engineConfig);

        // Log config sanity warnings
        var sanityWarnings = engineConfig.sanityCheck();
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
            getLogger().warning("Could not register /geoforgereload — not defined in plugin.yml");
        }

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
        return new GeoForgeGenerator(adapter, engine, worldSeed);
    }

    /**
     * Returns the engine instance for access by commands.
     *
     * @return the GeoForgeEngine
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public GeoForgeEngine getEngine() {
        return engine;
    }

    /**
     * Returns the world generation seed.
     *
     * @return the seed
     */
    public long getWorldSeed() {
        return worldSeed;
    }
}
