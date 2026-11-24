# Commands and Permissions

## Commands

| Command | Permission | Default |
| --- | --- | --- |
| `/fireworks give rocket <id> [players] [amount]` | `fireworks.give` | OP 2 |
| `/fireworks give pattern <id> [players] [amount]` | `fireworks.give` | OP 2 |
| `/fireworks give mystery [players] [amount]` | `fireworks.give` | OP 2 |
| `/fireworks show start <name>` | `fireworks.show.command` | OP 2 |
| `/fireworks show stop <name>` | `fireworks.show.command` | OP 2 |
| `/fireworks show list` | `fireworks.show.command` | OP 2 |
| `/fireworks reload` | `fireworks.admin` | OP 3 |

- `[players]` defaults to yourself and `[amount]` to 1. The most is 64 rockets or 16 Pattern Cards.
- `<id>` is the bare preset id, for example `golden_willow` or `smiley`. Tab completion lists them.
- `/fireworks show list` shows every named show, where it is and whether it's running.
- `/fireworks reload` re-reads `messages.toml` and `config/codex_fireworks/patterns/`, checks installed mods again for the [overlap guard](Configuration#overlap-guard) and sends the switches to every player. Rocket presets and built-in patterns are datapack files and reload with `/reload`.

## Permissions

| Permission | What it allows | Default |
| --- | --- | --- |
| `fireworks.design` | Firework Workbench and Pattern Cards | everyone |
| `fireworks.pattern.hex` | Custom hex colors in patterns | OP 2 |
| `fireworks.launcher.use` | Opening launchers | everyone |
| `fireworks.show.edit` | Editing shows and linking launchers | everyone |
| `fireworks.show.command` | `/fireworks show` | OP 2 |
| `fireworks.give` | `/fireworks give` | OP 2 |
| `fireworks.bypass` | Changing anyone's launchers and controllers | OP 2 |
| `fireworks.admin` | `/fireworks reload` | OP 3 |

The nodes work with any permission mod that uses the NeoForge permission API. Without one, the defaults above apply.
