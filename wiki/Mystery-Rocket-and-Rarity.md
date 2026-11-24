# Mystery Rocket and Rarity

## Mystery Rocket

A Mystery Rocket turns into a random [rocket preset](Rocket-Presets) when it's launched, from the hand, a dispenser, a crossbow or a [Launcher](Launcher).

- Give it with `/fireworks give mystery [players] [amount]`.
- The mod adds no loot. Put it in your own loot tables or crates.
- `pool` under `[mystery]` in `limits.toml` sets which presets it can turn into. Empty means every preset.
- `mystery_rocket = false` in `features.toml` turns it off.

## Rarity

Rocket presets can be Common, Rare or Epic. The name shows in white, aqua or light purple, and the tooltip shows the rarity.

Set it with `"rarity"` in a preset file (see [Custom Presets and Patterns](Custom-Presets-and-Patterns)). `rarity = false` in `features.toml` turns it off.
