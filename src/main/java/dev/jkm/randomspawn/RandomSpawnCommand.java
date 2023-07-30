package dev.jkm.randomspawn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A command for operators to teleport to a random spawn location.
 */
public class RandomSpawnCommand implements CommandExecutor {
    private final RandomSpawn plugin;

    public RandomSpawnCommand(RandomSpawn plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;
        if (player.isOp()) {
            plugin.setPlayerRandomSpawn(player);
            player.sendMessage("Teleported to a random spawn location.");
        } else {
            player.sendMessage("You do not have permission to use this command.");
        }

        return true;
    }
}
