# Codex: Fireworks

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=for-the-badge) ![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-E68A2E?style=for-the-badge) ![Environment](https://img.shields.io/badge/Environment-Client%20%26%20Server-5865F2?style=for-the-badge) ![Type](https://img.shields.io/badge/Type-Quality%20of%20Life-8A2BE2?style=for-the-badge) [![Source](https://img.shields.io/badge/Source-GitHub-181717?style=for-the-badge&logo=github)](https://github.com/Telmarine/Codex-Fireworks)

A firework mod for NeoForge 1.21.1. It adds new burst shapes and rockets that pop into formations of up to 8 bursts, each with its own offset and delay. Install it on both the server and the client.

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

Rocket fields: `name`, `flight` (1-3), `enabled`, `bursts` (1-8) and `scale` (0.25-4.0, default 1.0). `scale` makes that one rocket bigger or smaller: it multiplies the spread, the spacing between bursts and the spark size.

Burst fields: `shape`, `colors` (1-8), `fades` (0-8), `trail`, `twinkle`, `size` (0.25-2.0), `dx` / `dy` / `dz` (offset in blocks), `delay` (ticks). Colors can be a dye name (`"light_blue"`) or a hex color (`"#FFD700"`).

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
| `/fireworks reload` | `fireworks.admin` | OP 3 |

`/fireworks reload` re-reads `messages.toml`, checks installed mods again and sends the switches to every player.

## Config

`config/codex_fireworks/`

- `features.toml`: master switch, one switch per shape and per feature, the firework damage switch and the overlap guard
- `limits.toml`: burst and spark size for every rocket (`[display] burst_scale`, `spark_scale`), most bursts per rocket, longest delay, furthest offset and the other server limits
- `messages.toml`: every message, `&` colors
- `client.toml` (each player's own): particle quality (FULL, REDUCED, MINIMAL), most sparks per burst, volume

A shape that's switched off on the server is drawn as a peony, and `/fireworks give` refuses presets that use it.

`player_damage = false` under `[rules]` makes firework explosions harmless to players. Mobs still take damage.

## Overlap guard

When another installed mod already does the same job, that feature turns itself off. The list is `overlap_guard.mods` in `features.toml`, written as `"feature=modid"`. It's empty by default, since no other NeoForge 1.21.1 mod changes vanilla rockets this way. Add a feature to `overlap_guard.force_on` to keep it on anyway.

## Building

`gradlew build`. The jar is `build/libs/codex_fireworks-<version>.jar`.

## License

Free to use in any modpack. Credit Telmarine. No resale. See LICENSE.
