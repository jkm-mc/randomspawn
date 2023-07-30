package dev.jkm.randomspawn;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomSpawn extends JavaPlugin implements Listener {
    private int minX;
    private int minZ;
    private int maxX;
    private int maxZ;
    private List<Material> spawnBlocks;
    private int minimumTownyDistance;

    private FileConfiguration playerSpawnsConfig;
    private File playerSpawnsFile;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        this.getCommand("randomspawn").setExecutor(new RandomSpawnCommand(this));

        // Save the default config if it does not exist.
        saveDefaultConfig();
        createPlayerSpawnsConfig();

        // Load the values from the configuration.
        minX = getConfig().getInt("minX");
        minZ = getConfig().getInt("minZ");
        maxX = getConfig().getInt("maxX");
        maxZ = getConfig().getInt("maxZ");

        spawnBlocks = new ArrayList<>();
        for (String blockName : getConfig().getStringList("spawnBlocks")) {
            Material material = Material.getMaterial(blockName);
            if (material != null) {
                spawnBlocks.add(material);
            } else {
                getLogger().warning("Unknown block type in config: " + blockName);
            }
        }

        minimumTownyDistance = getConfig().getInt("minimumTownyDistance");

        if (isTownyLoaded()) {
            getLogger().info("Towny plugin detected, will ensure spawn location is far enough away from towns");
        } else {
            getLogger().info("Towny plugin not detected, will not ensure spawn location is far enough away from towns");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Only set the spawn location for new players.
        if (player.hasPlayedBefore()) {
            return;
        }

        setPlayerRandomSpawn(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Check if the respawn location is a bed spawn (i.e., the bed is still there)
        if (event.isBedSpawn()) {
            return;
        }

        // Retrieve the stored random spawn location for this player
        Location storedSpawnLocation = (Location) playerSpawnsConfig.get("spawn." + player.getUniqueId());

        if (storedSpawnLocation != null) {
            event.setRespawnLocation(storedSpawnLocation);
        }
    }

    public void setPlayerRandomSpawn(Player player) {
        World world = player.getWorld();
        Random random = new Random();

        int tries = 0;
        
        while (true) {
            // Limit the number of attempts to find a suitable location.
            tries++;
            if (tries > 1000) {
                getLogger().warning("Could not find a suitable location for player " + player.getName());
                break;
            }

            // Get a random location within the configured bounds.
            Location randomLocation = getRandomLocation(random, world);
            
            // Ensure the location is a valid block type
            if (!isLocationValidBlock(randomLocation)) {
                continue;
            }

            // Ensure the location is far enough away from a town block, if Towny is loaded
            if (!isLocationFarEnoughFromTown(randomLocation)) {
                continue;
            }

            // add 1 to y to avoid spawning in the ground, and 0.5 to x and z to spawn in the center of the block
            Location spawnLocation = randomLocation.add(0.5, 1, 0.5);

            String locationString = "(" + spawnLocation.getX() + ", " + spawnLocation.getY() + ", " + spawnLocation.getZ() + ")";
            getLogger().info("Setting spawn location for player " + player.getName() + " to " + locationString + " after " + tries + " tries");

            // Set the spawn location and stop searching.
            player.teleport(spawnLocation);
            player.setBedSpawnLocation(spawnLocation, true);

            savePlayerSpawnLocation(player, spawnLocation);

            break;
        }
    }

    /**
     * Get a random location within the configured bounds, choosing the highest
     * block at that location.
     * 
     * @param random The random number generator to use
     * @param world The world to spawn in
     * @return A random location within the configured bounds
     */
    private Location getRandomLocation(Random random, World world) {
        int x = random.nextInt(maxX - minX + 1) + minX;
        int z = random.nextInt(maxZ - minZ + 1) + minZ;
        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x, y, z);
    }

    /**
     * Check if the location is a valid block type to spawn on.
     * @param location The location to check
     * @return true if the location is a valid block type
     */
    private boolean isLocationValidBlock(Location location) {
        Material blockType = location.getBlock().getType();
        return spawnBlocks.contains(blockType);
    }

    private void savePlayerSpawnLocation(Player player, Location location) {
        playerSpawnsConfig.set("spawn." + player.getUniqueId(), location);
        savePlayerSpawnsConfig();
    }

    private void createPlayerSpawnsConfig() {
        playerSpawnsFile = new File(getDataFolder(), "playerSpawns.yml");
        if (!playerSpawnsFile.exists()) {
            playerSpawnsFile.getParentFile().mkdirs();
            try {
                playerSpawnsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        playerSpawnsConfig = new YamlConfiguration();
        try {
            playerSpawnsConfig.load(playerSpawnsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void savePlayerSpawnsConfig() {
        try {
            playerSpawnsConfig.save(playerSpawnsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Is the Towny plugin loaded?
     * @return true if the Towny plugin is loaded
     */
    private boolean isTownyLoaded() {
        return getServer().getPluginManager().getPlugin("Towny") != null;
    }

    /**
     * Is the location far enough away from a town block?
     * @param location The location to check
     * @return true if the location is far enough away from a town block
     */
    private boolean isLocationFarEnoughFromTown(Location location) {
        if (!isTownyLoaded()) {
            return true;
        }

        for (TownBlock townBlock : TownyAPI.getInstance().getTownBlocks()) {
            try {
                // TODO we should check that the town block is in the same world as the location
                Location townLocation = new Location(location.getWorld(), townBlock.getX() * 16, 64, townBlock.getZ() * 16); // Approximate
                                                                                                                   // location
                if (location.distance(townLocation) < minimumTownyDistance) {
                    return false;
                }

            } catch (Exception e) {
                getLogger().warning("Error comparing location to town: " + e.getMessage());
            }
        }

        return true;
    }
}
