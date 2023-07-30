# randomspawn

This Spigot plugin sets a random spawn location for players on their first join, within a configurable area. A whitelist of blocks can also be provided to ensure the player spawns on blocks you like.

If Towny is enabled, it's possible to set a minimum distance between random spawns and town blocks, so players don't spawn too close to towns.

## Configuration

You can configure the plugin by editing the `config.yml` files

### `config.yml`

```yaml
# The bounds of the random spawn area.
minX: -2000
minZ: -2000
maxX: 2000
maxZ: 2000

# Blocks that random spawns can be on.
spawnBlocks:
  - GRASS_BLOCK
  - DIRT
  - STONE
  - SAND
  - GRAVEL

# The minimum allowed distance between random spawns and town blocks, if Towny is installed.
minimumTownyDistance: 500
```

## Installation

1. Download the latest release from the [releases page](https://github.com/jameskmonger/mc-randomspawn/releases).
2. Place the downloaded JAR file in your server's `plugins` directory.

## Commands

- `/randomspawn` - Teleports and sets the player to a random spawn location (operator only).
