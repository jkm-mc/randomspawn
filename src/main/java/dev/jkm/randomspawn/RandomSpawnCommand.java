package dev.jkm.randomspawn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A command for operators to teleport to a random spawn location.
 */
public class RandomSpawnCommand implements CommandExecutor {
    /**
     * Reference to the plugin.
     */
    private final RandomSpawn plugin;

    /**
     * The permission required to use this command.
     */
    private final String PERMISSION = "randomspawn.command.randomspawn";

    public RandomSpawnCommand(RandomSpawn plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player == false) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (player.hasPermission(PERMISSION) == false) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        plugin.setPlayerRandomSpawn(player);
        player.sendMessage("Teleported to a random spawn location.");

        return true;
    }
}
