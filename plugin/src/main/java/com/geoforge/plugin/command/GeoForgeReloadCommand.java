package com.geoforge.plugin.command;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.biome.BiomeRegistry;
import com.geoforge.engine.config.biome.ClimateResolver;
import com.geoforge.plugin.GeoForgePlugin;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Reloads GeoForge biome configurations from disk without a server restart.
 *
 * <p>Uses {@link org.bukkit.plugin.Plugin#getServer() getServer()}.{@link
 * org.bukkit.Server#getGlobalRegionScheduler() getGlobalRegionScheduler()} for
 * Folia-safe asynchronous execution. The biome registry is swapped atomically on
 * the engine via {@link GeoForgeEngine#replaceBiomeRegistry(BiomeRegistry)} —
 * readers continue to see the old registry until the reference is updated.
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public final class GeoForgeReloadCommand implements CommandExecutor {

    private final GeoForgePlugin plugin;

    public GeoForgeReloadCommand(GeoForgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("geoforge.reload")) {
            sender.sendMessage("§cYou don't have permission to reload GeoForge biomes.");
            return true;
        }

        sender.sendMessage("§7Reloading GeoForge biome configs…");

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            try {
                Path biomesDir = plugin.getDataFolder().toPath().resolve("Biomes");
                if (!Files.isDirectory(biomesDir)) {
                    sender.sendMessage("§cNo Biomes directory found at " + biomesDir
                            + " — nothing to reload.");
                    plugin.getLogger().info("Biome reload skipped — directory missing: " + biomesDir);
                    return;
                }

                var climateConfig = new ClimateResolver.ClimateConfig(0.001, 0.005, 0.001);
                BiomeRegistry newRegistry = BiomeRegistry.reload(
                        biomesDir, climateConfig, plugin.getWorldSeed());

                if (newRegistry == null || newRegistry.isEmpty()) {
                    sender.sendMessage("§cBiome reload failed — no valid biome definitions loaded."
                            + " Check console for errors.");
                    plugin.getLogger().severe("Biome reload failed: got empty or null registry");
                    return;
                }

                GeoForgeEngine engine = plugin.getEngine();
                engine.replaceBiomeRegistry(newRegistry);

                sender.sendMessage("§aGeoForge biomes reloaded! §7"
                        + newRegistry.size() + " biomes loaded.");
                plugin.getLogger().info("Biome reload complete — "
                        + newRegistry.size() + " biomes registered");
            } catch (Exception e) {
                sender.sendMessage("§cReload failed: " + e.getMessage());
                plugin.getLogger().severe("Biome reload failed: " + e.getMessage());
            }
        });

        return true;
    }
}
