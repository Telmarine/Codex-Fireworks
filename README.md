# Codex: Fireworks

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=for-the-badge) ![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-E68A2E?style=for-the-badge) ![Environment](https://img.shields.io/badge/Environment-Client%20%26%20Server-5865F2?style=for-the-badge) ![Type](https://img.shields.io/badge/Type-Quality%20of%20Life-8A2BE2?style=for-the-badge) [![Source](https://img.shields.io/badge/Source-GitHub-181717?style=for-the-badge&logo=github)](https://github.com/Telmarine/Codex-Fireworks)

A firework mod for NeoForge 1.21.1. It adds new burst shapes, rockets that pop into formations of up to 8 bursts (each with its own offset and delay), drawn patterns, launchers and timed shows. Install it on both the server and the client.

Rockets made by this mod are normal vanilla firework rockets with extra data. They also carry plain vanilla stars, so if the mod is turned off or removed they still fly and pop like any other rocket.

## Burst shapes

Peony, Chrysanthemum, Willow, Palm, Ring, Double Ring, Heart, Spiral, Crossette, Crackle, Strobe, Comet, Fish and Brocade. Every shape keeps the vanilla options: colors, fade colors, trail and twinkle. Hearts, rings and spirals face the player watching them.

## Rocket presets

| Preset | What it does |
| --- | --- |
| Golden Willow | One big gold willow that fades to dark orange |
| Rainbow Ring | Seven rings stacked bottom to top, one color each |
| Crossette Storm | A peony, then four crossettes around it |
| Heart Cluster | Six small hearts around one big heart |
| Comet Fan | Five comets in a fan |
| Strobe Field | A peony followed by blinking strobes scattered around it |
| Chrysanthemum Bloom | A chrysanthemum with late brocade glitter |
| Triple Break | Peony, ring and crackle, each higher than the last |
| Spiral Tower | Four spirals stacked up |
| Fish School | Six fish bursts in a line |
| Palm Grove | Three gold palms in a row |
| Grand Finale | All 8 slots in one quick rocket |

Presets are datapack files in `data/<namespace>/rocket_presets/<id>.json`. A datapack file with the same path replaces a built-in preset, and `"enabled": false` hides it. Reload with `/reload`.

```json
{
  "name": "Golden Willow",
  "flight": 3,
  "bursts": [
    { "shape": "willow", "colors": ["#FFC83D", "#FFD700"], "fades": ["#B5651D"], "size": 1.4 }
  ]
}
```

Rocket fields: `name`, `flight` (1-3), `enabled`, `bursts` (1-8), `scale` (0.25-4.0, default 1.0) and `rarity` (`common`, `rare`, `epic`). `scale` makes that one rocket bigger or smaller: it multiplies the spread, the spacing between bursts and the spark size.

Burst fields: `shape`, `colors` (1-8), `fades` (0-8), `trail`, `twinkle`, `size` (0.25-2.0), `dx` / `dy` / `dz` (offset in blocks), `delay` (ticks). Colors can be a dye name (`"light_blue"`) or a hex color (`"#FFD700"`).

## Firework Workbench

Crafted from a crafting table, 4 paper and 4 gunpowder. Put in paper, 1-3 gunpowder (the flight) and up to 8 bursts: firework stars or Pattern Cards.

- Click a burst slot (or its dot on the formation view) to select it. Pick its shape, size and delay, and move it with the X / Y / Z buttons or by dragging the dot. Shift makes the steps finer (offset) or bigger (delay).
- Formation presets place all bursts at once: Single, Ring, Line, Cross, Stack, Multi-break and Random cluster, with a spacing you choose.
- Preview pops the rocket in the sky in front of you without using anything. Click or press a key to get the screen back.
- Craft uses 1 paper, the flight's gunpowder and one of each firework star. Pattern Cards are not used up.
- Rocket presets can be made here too. They cost 1 paper, the flight's gunpowder and 1 gunpowder per burst (`limits.toml` `[workbench] preset_gunpowder_per_burst`).

## Pattern Cards

Craft a blank card from paper and any dye, then use it to open the design screen. Using a stack draws on one card and puts it in your inventory.

- Left mouse draws, right mouse erases. Draw, Erase, Fill and Clear tools, and the 16 dye colors.
- Custom hex colors need `fireworks.pattern.hex`, unless `hex_for_everyone` is on in `features.toml`.
- Options: size (small, medium, large), face the viewer or a fixed direction, spin, sparkle and a fade color.
- Text mode: type up to 12 characters (A-Z, 0-9 and basic punctuation). Use `|` to start the second line. A line holds up to 8 characters.
- Ready-made patterns can be loaded from the screen or given with `/fireworks give pattern`.
- Put a drawn card and blank cards in a crafting grid to copy it. The original stays.

Built-in patterns: Smiley, Star, Ghost, Sword, Crown, Skull, Pumpkin, Snowflake, Christmas Tree, Present, and the text patterns GG, Happy New Year and Welcome. They are datapack files in `data/<namespace>/pattern_presets/<id>.json`. A server can also put its own in `config/codex_fireworks/patterns/` and load them with `/fireworks reload`.

```json
{
  "name": "Heart",
  "rows": [
    "..rr...rr..",
    ".rrrr.rrrr.",
    ".rrrrrrrrr.",
    "..rrrrrrr..",
    "...rrrrr...",
    "....rrr....",
    ".....r....."
  ],
  "colors": { "r": "red" },
  "sparkle": true
}
```

Text patterns use `"text": "GG"` and `"colors": ["yellow", "orange"]` (line 1 and line 2) instead of rows. Both kinds take `size`, `face_viewer`, `yaw`, `spin`, `sparkle` and `fade`.

## Mystery Rocket and rarity

- A Mystery Rocket turns into a random rocket preset when it's launched, from the hand, a dispenser, a crossbow or a launcher. The pool is `limits.toml` `[mystery] pool` (empty = every preset). Give it with `/fireworks give mystery`. The mod adds no loot; put it in your own loot tables or crates.
- Rocket presets can have `"rarity": "common"`, `"rare"` or `"epic"`. The name shows in white, aqua or light purple and the tooltip shows the rarity.

## Sounds

Crackle, Comet, Willow and Strobe have their own sounds. The other shapes use the vanilla firework sounds. `shape_sounds = false` in `features.toml` turns the shape sounds off. Each player can set the volume in `client.toml`.

## Launcher

Crafted from a dispenser, 7 iron ingots and 1 gunpowder. Load up to 9 stacks of rockets.

- A redstone pulse fires a volley: the number of rockets set in the screen, the interval apart.
- Settings: direction, tilt (0 = straight up), random spread, rockets per pulse and interval. Test fires one rocket.
- `min_interval_ticks` in `limits.toml` is the shortest time between two shots of one launcher.

## Show Controller

Crafted from a launcher, a clock and 4 redstone.

- Shift-right-click the controller (or press Link in its screen) to start linking, shift-right-click launchers to add or remove them, then shift-right-click the controller again to finish. Works with an item in hand. Linking ends by itself after 2 minutes. Launchers must be within `link_range` blocks.
- Right-click to edit the show. Each cue has a time, a launcher (#1, #2, ... in link order, or All) and a rocket slot (or the next rocket).
- A show can loop and can start on a redstone pulse. A looping show stops by itself once all its launchers are empty. Give it a name to start it with `/fireworks show start <name>`.
- Show Cards (paper + redstone) store a show. Hold one and press Write card or Read card to copy shows between controllers. Writing from a stack of blank cards writes one card and puts it in your inventory; Read card uses a written card in either hand.
- `limits.toml` sets the most launchers per controller, cues per show and shows running near each other.

Launchers and controllers belong to the player who placed them. Only that player, the owner of the land (Elements: Claims, Open Parties and Claims or FTB Chunks, if installed) and staff with `fireworks.bypass` can change them. `claims = false` under `[rules]` turns the land owner check off.

## Size

Bursts can be made bigger or smaller without rebuilding the mod:

- `limits.toml`, `[display]` section, for every rocket on the server:
  - `burst_scale` (default 1.6): how far the sparks spread and the spacing between bursts.
  - `spark_scale` (default 1.5): the size of each spark.
  - 1.0 is vanilla firework size; the range is 0.25 to 4.0. Players get the new values while the server runs, no restart needed.
- `"scale"` in a rocket file (default 1.0): makes that one rocket bigger or smaller on top of the `[display]` values. It's saved on the rocket item.

Bigger bursts use more particles. Players on slower PCs can lower `quality` in their own `client.toml`.

## Commands

| Command | Permission | Default |
| --- | --- | --- |
| `/fireworks give rocket <id> [players] [amount]` | `fireworks.give` | OP 2 |
| `/fireworks give pattern <id> [players] [amount]` | `fireworks.give` | OP 2 |
| `/fireworks give mystery [players] [amount]` | `fireworks.give` | OP 2 |
| `/fireworks show start <name>` / `stop <name>` / `list` | `fireworks.show.command` | OP 2 |
| `/fireworks reload` | `fireworks.admin` | OP 3 |

`/fireworks reload` re-reads `messages.toml` and `config/codex_fireworks/patterns/`, checks installed mods again and sends the switches to every player.

| Permission | What it allows | Default |
| --- | --- | --- |
| `fireworks.design` | Firework Workbench and Pattern Cards | everyone |
| `fireworks.launcher.use` | Using launchers | everyone |
| `fireworks.show.edit` | Editing shows and linking launchers | everyone |
| `fireworks.pattern.hex` | Custom hex colors in patterns | OP 2 |
| `fireworks.bypass` | Changing anyone's launchers and controllers | OP 2 |

## Config

`config/codex_fireworks/`

- `features.toml`: master switch, one switch per shape and per feature, the firework damage switch and the overlap guard
- `limits.toml`: burst and spark size for every rocket (`[display] burst_scale`, `spark_scale`), most bursts per rocket, longest delay, furthest offset, workbench preset cost, most lit pixels per pattern, the Mystery Rocket pool, and the launcher and show limits
- `messages.toml`: every message, `&` colors
- `patterns/`: the server's own patterns, one file each
- `client.toml` (each player's own): particle quality (FULL, REDUCED, MINIMAL), most sparks per burst, most sparks on screen at once, volume

A shape that's switched off on the server is drawn as a peony, and `/fireworks give` refuses presets that use it.

`player_damage = false` under `[rules]` makes firework explosions harmless to players. Mobs still take damage.

## Overlap guard

When another installed mod already does the same job, that feature turns itself off. The list is `overlap_guard.mods` in `features.toml`, written as `"feature=modid"`. It's empty by default, since no other NeoForge 1.21.1 mod changes vanilla rockets this way. Add a feature to `overlap_guard.force_on` to keep it on anyway.

## Building

`gradlew build`. The jar is `build/libs/codex_fireworks-<version>.jar`.

## License

Free to use in any modpack. Credit Telmarine. No resale. See LICENSE.
