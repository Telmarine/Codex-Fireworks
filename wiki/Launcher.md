# Launcher

A block that holds up to 9 stacks of rockets and fires them.

## Firing

- A redstone pulse fires a volley: the number of rockets set in the screen, the interval apart.
- A [Show Controller](Show-Controller) cue fires it too.
- **Test** in the screen fires one rocket.

Rockets are taken in turn from slot 1 to 9. A show cue can pick a slot instead.

## Settings

| Setting | Range | Step |
| --- | --- | --- |
| Direction | 0-345° (S, SSW, ...) | 15° |
| Tilt | Up (0°) to 60° | 5° |
| Spread | Off to ±30° random | 2° |
| Rockets | 1-9 per pulse | 1 |
| Interval | down to `min_interval_ticks`, up to 5 seconds | 1 tick, Shift = 10 |

`min_interval_ticks` in `limits.toml` is the shortest time between two shots of one launcher.

## Loading

Launchers are loaded by hand. They don't take items from hoppers, so rockets stay in the slots you put them in. Shows that fire a set slot depend on that.

Breaking a launcher drops its rockets.

## Who can use it

A launcher belongs to the player who placed it. See [Protection](Protection). The `fireworks.launcher.use` permission controls who can open launchers.

`launchers = false` in `features.toml` turns launchers off.
