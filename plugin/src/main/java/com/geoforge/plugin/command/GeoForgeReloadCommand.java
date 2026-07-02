package com.geoforge.plugin.command;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.geoforge.engine.GeoForgeEngine;
import com.geoforge.engine.config.biome.BiomeRegistry;
import com.geoforge.engine.config.biome.GeoForgeBiomeDefaults;
import com.geoforge.plugin.GeoForgePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Reloads GeoForge biome definitions from built-in defaults without a server restart.
 *
 * <p>The biome registry is swapped atomically on the engine via
 * {@link GeoForgeEngine#replaceBiomeRegistry(BiomeRegistry)} — readers continue
 * to see the old registry until the reference is updated.
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
            sender.sendMessage("\u00a7cYou don't have permission to reload GeoForge biomes.");
            return true;
        }

        if (args.length > 0) {
            // Reload specific world
            String worldName = args[0];
            GeoForgeEngine engine = plugin.getEngine(worldName);
            if (engine == null) {
                sender.sendMessage("\u00a7cNo GeoForge engine found for world: " + worldName);
                return true;
            }
            reloadEngine(engine, sender);
            sender.sendMessage("\u00a7aGeoForge biomes reloaded for world: " + worldName);
        } else {
            // Reload all worlds synchronously — replaceBiomeRegistry is an atomic reference swap
            var engines = plugin.getWorldEngines();
            for (var entry : engines.entrySet()) {
                reloadEngine(entry.getValue(), sender);
            }
            sender.sendMessage("\u00a7aGeoForge biomes reloaded for all " + engines.size() + " worlds");
            plugin.getLogger().info("Biome reload complete \u2014 " + engines.size() + " worlds updated from built-in defaults");
        }

        return true;
    }

    private void reloadEngine(GeoForgeEngine engine, CommandSender sender) {
        var defaults = GeoForgeBiomeDefaults.createDefaults();
        var newRegistry = new BiomeRegistry(defaults,
                engine.getBiomeRegistry().climateResolver());
        engine.replaceBiomeRegistry(newRegistry);
    }
}
