# Protection

Launchers and Show Controllers belong to the player who placed them. Only these players can open, change or link them:

- the player who placed it
- the owner of the land it stands on, if a claim mod is installed
- staff with `fireworks.bypass`

Anyone else gets "This belongs to someone else."

Blocks placed with commands (`/setblock`) have no owner, so anyone with the normal permission can use them.

## Claim mods

Land owners are read from these mods, if installed. None of them is needed.

- Elements: Claims
- Open Parties and Claims
- FTB Chunks

`claims = false` under `[rules]` in `features.toml` turns the land owner check off. The placer and staff still can.

## Firework damage

`player_damage = false` under `[rules]` in `features.toml` makes firework explosions harmless to players. Mobs still take damage.
