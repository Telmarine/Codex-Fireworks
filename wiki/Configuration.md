# Configuration

Server files are in `config/codex_fireworks/`. Each player's own settings are in `config/codex_fireworks/client.toml` on their PC.

## features.toml

The master control file. Every switch is `true` or `false`.

| Section | Key | Default | What it does |
| --- | --- | --- | --- |
| `[general]` | `enabled` | true | Master switch. When off, rockets from this mod pop with their plain vanilla stars |
| `[shapes]` | `peony` ... `brocade` | true | One switch per [burst shape](Burst-Shapes). A shape that's off is drawn as a peony |
| `[patterns]` | `patterns` | true | [Pattern Cards](Pattern-Cards) |
| | `text_mode` | true | Text mode on Pattern Cards |
| | `hex_for_everyone` | false | Every player can use hex colors. When off, only `fireworks.pattern.hex` can |
| `[shells]` | `shells` | true | Custom rockets from the [Firework Workbench](Firework-Workbench) |
| | `rocket_presets` | true | [Rocket presets](Rocket-Presets) |
| | `pattern_presets` | true | Ready-made patterns |
| | `mystery_rocket` | true | [Mystery Rocket](Mystery-Rocket-and-Rarity) |
| | `rarity` | true | Common / Rare / Epic on rockets |
| | `shape_sounds` | true | Shape sounds for Crackle, Comet, Willow and Strobe |
| `[shows]` | `launchers` | true | [Launchers](Launcher) |
| | `shows` | true | [Show Controller](Show-Controller), Show Cards and `/fireworks show` |
| `[rules]` | `player_damage` | true | Firework explosions hurt players (vanilla). Off makes them harmless to players |
| | `claims` | true | Land owners from claim mods can change launchers and controllers. See [Protection](Protection) |

### Overlap guard

Turns a feature off when another installed mod already does the same job. Mods are checked at server start and on `/fireworks reload`.

| Key | Default | What it does |
| --- | --- | --- |
| `enabled` | true | Turn the overlap guard on or off |
| `mods` | empty | Which mods turn which feature off, as `"feature=modid"`, for example `"launchers=somemod"` |
| `force_on` | empty | Features to keep on even when an overlapping mod is installed, for example `["launchers"]` |

The list is empty by default, since no other NeoForge 1.21.1 mod changes vanilla rockets this way. `/fireworks reload` reports how many features the guard turned off.

## limits.toml

Changes are picked up while the server runs.

| Section | Key | Default | Range | What it does |
| --- | --- | --- | --- | --- |
| `[display]` | `burst_scale` | 1.6 | 0.25-4.0 | How far the sparks spread, and the spacing between bursts. 1.0 = vanilla size |
| | `spark_scale` | 1.5 | 0.25-4.0 | Size of each spark. 1.0 = vanilla spark |
| `[shells]` | `max_bursts` | 8 | 1-8 | Most bursts one rocket can hold |
| | `max_delay_ticks` | 60 | 0-200 | Longest delay of one burst after the rocket pops |
| | `max_offset_blocks` | 12 | 0-32 | Furthest a burst can sit from the rocket |
| `[workbench]` | `preset_gunpowder_per_burst` | 1 | 0-8 | Extra gunpowder per burst when making a rocket preset |
| `[patterns]` | `max_sparks` | 256 | 16-256 | Most lit pixels in one drawn pattern |
| `[mystery]` | `pool` | empty | | Rocket presets a Mystery Rocket can turn into. Empty = all |
| `[shows]` | `max_launchers` | 32 | 1-256 | Most launchers linked to one Show Controller |
| | `max_cues` | 200 | 1-2000 | Most cues in one show |
| | `min_interval_ticks` | 5 | 1-200 | Shortest time between two shots from one launcher |
| | `max_shows_per_area` | 2 | 1-16 | Most shows running at once within 128 blocks of each other |
| | `link_range` | 48 | 4-128 | Furthest a launcher can be from its controller |

### Burst size

`burst_scale` and `spark_scale` change the size of every rocket on the server. Players get the new values while the server runs, no restart needed.

A rocket preset can also have its own `"scale"` (0.25-4.0) on top of these. See [Custom Presets and Patterns](Custom-Presets-and-Patterns).

Bigger bursts use more particles. Players on slower PCs can lower `quality` in their own `client.toml`.

## messages.toml

Every chat message the mod sends, with `&` color codes (`&a`, `&l`, `&#FFD700`). `{placeholders}` are filled in. Set a line to `""` to turn it off. Reload with `/fireworks reload`.

If you update the mod and a message is missing or out of date, delete the file and restart. The new default is written.

## patterns/

The server's own patterns, one JSON file each. Load them with `/fireworks reload`. See [Custom Presets and Patterns](Custom-Presets-and-Patterns).

## client.toml

Each player's own file. Only affects their screen.

| Key | Default | Range | What it does |
| --- | --- | --- | --- |
| `quality` | FULL | FULL, REDUCED, MINIMAL | REDUCED draws half the sparks, MINIMAL a quarter |
| `max_sparks_per_burst` | 400 | 16-1000 | Most sparks drawn for one burst |
| `max_sparks_total` | 12000 | 500-16000 | Most sparks on screen at once. New sparks are skipped above this |
| `volume` | 1.0 | 0.0-1.0 | Volume of this mod's firework sounds |
