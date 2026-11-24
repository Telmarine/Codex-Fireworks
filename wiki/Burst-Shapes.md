# Burst Shapes

| Shape | What it looks like |
| --- | --- |
| Peony | Round burst of evenly spread sparks |
| Chrysanthemum | Round burst where every spark leaves a trail |
| Willow | Slow sparks with long trails that droop down |
| Palm | A few thick arms that rise and curve out |
| Ring | One flat ring of sparks |
| Double Ring | Two rings crossing each other |
| Heart | A heart that faces the player watching |
| Spiral | Arms that curl outward in a spiral |
| Crossette | A few sparks that each split into four |
| Crackle | Sparks that end in small popping flashes |
| Strobe | Slow sparks that blink on and off |
| Comet | One bright spark with a long tail |
| Fish | Sparks that wiggle as they fly |
| Brocade | Dense glittering burst with fine trails |
| Pattern | A picture or text from a [Pattern Card](Pattern-Cards) |

Every shape keeps the vanilla options: colors, fade colors, trail and twinkle. Hearts, rings and spirals face the player watching them.

## Sounds

Crackle, Comet, Willow and Strobe have their own sounds. The other shapes use the vanilla firework sounds.

- `shape_sounds = false` in `features.toml` turns the shape sounds off for everyone.
- Each player can set the volume with `volume` in their own `client.toml`.

## Turning shapes off

Each shape has its own switch under `[shapes]` in `features.toml`. A shape that's switched off is drawn as a peony, and `/fireworks give rocket` refuses presets that use it.
