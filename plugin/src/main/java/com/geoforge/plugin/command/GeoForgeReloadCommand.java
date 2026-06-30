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
            sender.sendMessage("§cYou don't have permission to reload GeoForge biomes.");
            return true;
        }

        sender.sendMessage("§7Reloading GeoForge biome defaults…");

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            try {
                var defaults = GeoForgeBiomeDefaults.createDefaults();
                var engine = plugin.getEngine();
                var newRegistry = new BiomeRegistry(defaults,
                        engine.getBiomeRegistry().climateResolver());
                engine.replaceBiomeRegistry(newRegistry);

                sender.sendMessage("§aGeoForge biomes reloaded! §7"
                        + newRegistry.size() + " biomes registered from built-in defaults.");
                plugin.getLogger().info("Biome reload complete — "
                        + newRegistry.size() + " biomes registered from built-in defaults");
            } catch (Exception e) {
                sender.sendMessage("§cReload failed: " + e.getMessage());
                plugin.getLogger().severe("Biome reload failed: " + e.getMessage());
            }
        });

        return true;
    }
}
