# Custom Presets and Patterns

## Rocket presets

Rocket presets are datapack files in `data/<namespace>/rocket_presets/<id>.json`. Reload with `/reload`.

- A file with the same path as a built-in preset replaces it, for example `data/codex_fireworks/rocket_presets/golden_willow.json`.
- `"enabled": false` hides a preset.
- The id is the file name. `/fireworks give rocket <id>` uses it.

```json
{
  "name": "Golden Willow",
  "flight": 3,
  "rarity": "rare",
  "scale": 1.5,
  "bursts": [
    { "shape": "willow", "colors": ["#FFC83D", "#FFD700"], "fades": ["#B5651D"], "size": 1.4 }
  ]
}
```

### Rocket fields

| Field | Values | Default |
| --- | --- | --- |
| `name` | Display name (required) | |
| `flight` | 1-3 | 2 |
| `enabled` | true / false | true |
| `rarity` | `common`, `rare`, `epic` | none |
| `scale` | 0.25-4.0. Makes this rocket bigger or smaller: spread, burst spacing and spark size | 1.0 |
| `bursts` | 1-8 bursts (required) | |

### Burst fields

| Field | Values | Default |
| --- | --- | --- |
| `shape` | `peony`, `chrysanthemum`, `willow`, `palm`, `ring`, `double_ring`, `heart`, `spiral`, `crossette`, `crackle`, `strobe`, `comet`, `fish`, `brocade` (required) | |
| `colors` | 1-8 colors (required) | |
| `fades` | 0-8 colors | none |
| `trail` | true / false | false |
| `twinkle` | true / false | false |
| `size` | 0.25-2.0 | 1.0 |
| `dx`, `dy`, `dz` | Offset from the rocket, in blocks | 0 |
| `delay` | 0-200 ticks after the rocket pops (20 = 1 second) | 0 |

Colors can be a dye name (`"light_blue"`) or a hex color (`"#FFD700"`).

## Patterns

Built-in patterns are datapack files in `data/<namespace>/pattern_presets/<id>.json` and reload with `/reload`.

A server can also put its own in `config/codex_fireworks/patterns/<id>.json` and load them with `/fireworks reload`. These show in the Pattern Card screen and work with `/fireworks give pattern <id>`.

### Drawn pattern

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

- `rows`: up to 48 wide and 16 high. `.` is off; any other letter is a color from `colors`.
- `colors`: letter to color, as a dye name or hex.

### Text pattern

```json
{
  "name": "GG",
  "text": "GG",
  "colors": ["yellow", "orange"]
}
```

- `text`: up to 12 characters, `|` starts the second line, up to 8 per line.
- `colors`: the color of line 1 and line 2.

### Options for both

| Field | Values | Default |
| --- | --- | --- |
| `size` | `small`, `medium`, `large` | `medium` |
| `face_viewer` | true / false | true |
| `yaw` | 0, 90, 180, 270 (only when `face_viewer` is false) | 0 |
| `spin` | true / false | false |
| `sparkle` | true / false | false |
| `fade` | A color the sparks fade to | none |
