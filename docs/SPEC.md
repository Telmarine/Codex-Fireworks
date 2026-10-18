# Codex: Fireworks — Spec

Firework mod for NeoForge 1.21.1 where players design their own rockets: the burst shape, the formation of several bursts, and timed shows. Part of the Codex family (TAC5 Studios / Telmarine).

Status: build phase 1 written (shapes, shell data, rocket presets, /fireworks give rocket, master control file, overlap guard). Phases 2-6 to come.

## Identity
- Name: Codex: Fireworks · mod ID `codex_fireworks` · package `com.tac5studios.codexfireworks`
- **Client + server** (like Title Scrolls). New burst shapes are drawn by the client, so players need the mod.
- NeoForge 21.1.x, Java 21, version 1.0.0
- Ranges: `minecraft_version_range=[1.21.1]`, `neo_version_range=[21.1.0,21.2)`
- Custom license (free, credit Telmarine, no resale). CurseForge + GitHub only.
- Works on its own. Elements: Claims and other claim mods are optional hooks.

## Background (research, Oct 2026)
- Vanilla has 5 shapes (small ball, large ball, star, creeper, burst), plus trail, twinkle, colors and fade colors. A rocket holds up to 256 explosions, flight duration 1–3.
- NeoForge 1.21.1 makes `FireworkExplosion.Shape` an extensible enum (networked both ways, so client and server must match) and adds `FireworkShapeFactoryRegistry` on the client. Its factory only receives shape, trail, twinkle and colors, so it can't carry a drawn pattern or a shell layout.
- Rockets that store an added Shape in vanilla's `fireworks` component can't be read once the mod is removed. Because of that, this mod does **not** extend the Shape enum (see "Data" below).
- Vanilla particle cap: 16,384 per particle render type (`ParticleEngine` constant). Large shows can reach it, so limits and a client quality setting are required.
- Other mods: The Fireworks (mod id `the_fireworks`, checked in the 0.6.0 jar; NeoForge 1.21.1, experimental, last update Oct 2024, CC BY-NC-ND 4.0; made with MCreator; uses its own mortar blocks, shell items, a working table and its own particles and entities — it never touches vanilla firework rockets), Super Firework (1.20.1 Fabric/Forge, GPL-3, own rocket entity), PixelWorks (Paper plugin, ASCII-grid shapes + 5×7 text). Real-world pyrotechnic names (peony, willow, palm…) are generic terms.

## Clean-room rule
- No code, assets, textures, sounds, shell designs or pattern art are taken from any other firework mod, plugin or datapack whose license doesn't allow it: The Fireworks (CC BY-NC-ND 4.0), Super Firework (GPL-3.0), PixelWorks (ARR), More Fireworks (CC BY 4.0 — not used either, to keep the mod fully our own).
- Other mods were only looked at to learn what exists; every shape renderer, preset, pattern and the 5×7 font are designed and drawn from scratch.
- Vanilla is only hooked through mixins and NeoForge APIs; no Mojang code is copied into the mod.
- Sound files are made by us; no sounds are taken from other mods.

## Data
- Every rocket made by this mod keeps normal vanilla firework stars in the vanilla `fireworks` component as a fallback.
- Our shapes, patterns and the shell layout live in the rocket's vanilla `custom_data`, under the key `codex_fireworks` (synced to players with the item). No new item component is registered, so removing the mod never breaks a stored rocket.
- With the mod installed, the client draws our version instead of the vanilla stars. Without the mod (or after removal) the rocket still flies and pops with its vanilla stars.
- Hook point (checked in vanilla 1.21.1 code): when a rocket explodes, the server sends entity event 17. On the client, `FireworkRocketEntity.handleEntityEvent(17)` reads the rocket's synced item (`DATA_ID_FIREWORKS_ITEM`), takes the vanilla `fireworks` explosions and calls `Level.createFireworks(...)`. A client mixin at that point reads the `codex_fireworks` data from the same item; if present, our renderer draws the shell and the vanilla call is skipped.

## Core idea: three layers
1. **Burst** — one explosion. A built-in shape or a player-drawn pattern.
2. **Shell** — one rocket. Holds up to 8 bursts with offsets and delays, so one rocket can pop into a formation (ring of hearts, smiley over a burst, multi-break).
3. **Show** — a timed list of shells fired from launcher blocks.

## 1. Bursts
### Built-in shapes (new)
Peony, Chrysanthemum (trail), Willow (slow drooping trails), Palm (thick arms), Ring, Double Ring, Heart, Spiral, Crossette (splits into 4), Crackle (popping sparks), Strobe (blinking), Comet (single tail), Fish (wiggling sparks), Brocade (glittering gold).
- All keep vanilla colors, fade colors, trail and twinkle.
- Each shape is a client particle renderer registered by id, so later versions (or addon mods) can add more.

### Player patterns ("drawn bursts")
- A 16×16 pixel grid in a design screen (at most 256 sparks per burst). Each lit pixel is a spark, with one color per pixel from the 16 dye colors.
- Custom hex colors: staff only by default (`fireworks.pattern.hex`); a server switch opens them to everyone.
- Options per pattern: size (small/medium/large), face the launcher or face a fixed direction, spin, sparkle, fade color.
- Saved as a **Pattern Card** item (stores the grid in a data component). Cards can be copied, renamed and traded.
- Text mode: up to 12 characters (A–Z, 0–9, basic punctuation) in a built-in 5×7 pixel font, two lines on the 16-wide grid. A shortcut that fills the grid.

## 2. Shells (rocket formations)
- Built in the **Firework Workbench** (new block). Slots: rocket body (paper + gunpowder sets flight 1–3), up to 8 burst slots (firework stars or Pattern Cards), and a formation editor.
- Formation editor: place each burst on a 2D layout (top-down + height) with an offset in blocks and a delay in ticks. Presets: Single, Ring of N, Line, Cross, Stack (rising), Multi-break (bursts one after another), Random cluster.
- Live preview in the screen (client-side render of the shell).
- Output: a normal firework rocket item with vanilla fallback stars plus the `codex_fireworks` custom data. Works from hand, dispensers, crossbows and launchers.

### Built-in rocket presets
Ready-made shells, given by `/fireworks give rocket <id>` and craftable in the workbench as presets. Each is a file in the mod's data, so servers can override or disable it.
1. **Golden Willow:** one big gold Willow, slow drooping trails fading to dark orange.
2. **Rainbow Ring:** 7 Rings in a stacked line, one color each, 4 ticks apart, bottom to top.
3. **Crossette Storm:** a Peony, then 4 Crossettes around it a moment later.
4. **Heart Cluster:** a Ring of 6 small pink/red Hearts around one big Heart.
5. **Comet Fan:** 5 Comets in a fan.
6. **Strobe Field:** a Random cluster of 8 white Strobes after the main burst.
7. **Chrysanthemum Bloom:** a Chrysanthemum with late Brocade glitter.
8. **Triple Break:** Multi-break Peony → Ring → Crackle, each higher than the last.
9. **Spiral Tower:** a Stack of 4 Spirals, alternating spin.
10. **Fish School:** 6 Fish in a Line.
11. **Palm Grove:** 3 gold Palms in a Line with green fade.
12. **Grand Finale:** all 8 slots — Willow, 2 Crossettes, Crackle, Ring, 2 Strobes, Brocade — in a quick Multi-break.

### Built-in pattern presets (16×16, drawn by us)
Smiley, Star, Creeper Face, Sword, Crown, Skull, Pumpkin, Snowflake, Christmas Tree, Present; text presets "GG", "HAPPY NEW YEAR" (two lines), "WELCOME".

### Extras
- **Mystery Rocket:** item that fires a random built-in preset each launch. Preset pool set in `limits.toml`.
- **Rocket rarity:** Common / Rare / Epic tag on rockets (data component, shown in the item name color and tooltip). The mod adds no loot; servers put rockets in their own loot tables or crates.
- **Sounds:** a sound per shape (crackle pops, whistling comet, deep willow boom, strobe ticks). Sound files are our own; vanilla firework sound events are the fallback when a shape has no sound set. Client volume slider in `client.toml`.

## 3. Shows
- **Launcher** block: holds rockets, fires on redstone or when a show cue says so. Settings: angle, spread, fire count, interval.
- **Show Controller** block: a timeline of cues (time → launcher id → rocket slot). Launchers link to a controller with a linking tool (or shift-click), within a configurable range.
- Show Card item stores a show so it can be copied between controllers. Loop, start on redstone, start by command.
- Staff command `/fireworks show start|stop <name>` for server events (New Year, launch day).

## Recipes
- Firework Workbench: crafting table + 4 paper + 4 gunpowder
- Launcher: dispenser + iron ingots + gunpowder
- Show Controller: Launcher + redstone + clock
- Pattern Card: paper + any dye
- All recipes are datapack JSON, so servers can change them.

## Size
- `limits.toml` `[display]`: `burst_scale` (spread and spacing between bursts, default 1.6) and `spark_scale` (spark size, default 1.5), 1.0 = vanilla firework size. Sent to every player on join and when the file changes.
- Each rocket file can set `"scale"` (0.25-4.0, default 1.0) on top of those; it's saved on the rocket item.

## Limits and performance
- Server config (`limits.toml`): max bursts per shell (8), max sparks per pattern (256), max launchers per controller, max cues per show, min interval between launches, max shows running at once per area.
- Client config: particle quality (full/reduced/minimal), max sparks drawn per burst, so low-end players can turn it down. Keep a full show under the 16,384 particle cap at "full".
- Bursts only send small packets (pattern id or grid + colors); the client builds the particles.

## Protection
- Launchers and controllers record their owner. Only the owner, the land owner and staff can change them.
- Claim checks (optional, by reflection): Elements: Claims via ClaimsAPI first, then OPAC / FTB Chunks if installed.
- Firework damage: vanilla damage stays on by default; `features.toml` switch to turn off firework damage to players.

## Permissions (NeoForge permission API, `fireworks.<path>`)
- `design` (workbench + pattern cards), `launcher.use`, `show.edit` — default everyone
- `pattern.hex` (custom hex colors), `give` (`/fireworks give`), `show.command`, `bypass` (edit anyone's launchers), `admin` (reload) — default OP 2/3

## Config `config/codex_fireworks/`
- `features.toml` (master control file): master switch, one switch per shape, patterns, text mode, hex for everyone, shells, rocket presets, pattern presets, Mystery Rocket, rarity, shape sounds, launchers, shows, damage, claims, overlap guard + `force_on`
- `limits.toml`: server limits above
- `client.toml`: particle quality (client side)
- `patterns/`: optional shared server patterns, one file per pattern (same idea as Armor Stands presets), loaded with `/fireworks reload`
- `messages.toml`: every message, `&` colors

## Overlap guard
- The Fireworks (`the_fireworks`) doesn't overlap: it has its own mortars and shells and leaves vanilla rockets alone, so both mods can run together and nothing is turned off for it.
- The guard list stays in `features.toml` (`"feature=modid"`), so a firework mod that does change vanilla rockets can be added later without a rebuild.

## Commands
- `/fireworks reload` (admin), `/fireworks give pattern <id>` and `/fireworks give rocket <id>` (staff), `/fireworks show start|stop|list`

## Icon
- Codex style: illustration, green code rain, "CODEX" label (same family look as Title Scrolls and Armor Stands).

## Build order (when development starts)
1. Built-in shapes (client renderers) + the shell data on vanilla rockets with vanilla fallback stars + the 12 rocket presets and `/fireworks give rocket` (needed to test the shapes).
2. Firework Workbench + formation editor + preview.
3. Player patterns + Pattern Cards + text mode, built-in rocket and pattern presets, Mystery Rocket, rarity, shape sounds.
4. Launcher block.
5. Show Controller + Show Cards + command.
6. Limits, client quality, protection, permissions.
