# Pattern Cards

A Pattern Card holds a picture that bursts in the sky. Craft a blank card from paper and any dye, then use it (right-click) to open the design screen.

Using a stack of cards draws on one card and puts it in your inventory.

## Drawing

- Left mouse draws, right mouse erases. Drag to draw lines.
- **Draw**, **Erase**, **Fill** and **Clear** tools.
- The 16 dye colors are always there.
- **Hex color:** type a color like `#FFAA00` and press **Add**. Needs `fireworks.pattern.hex`, unless `hex_for_everyone = true` in `features.toml`.
- The spark count shows at the bottom. A pattern can have up to 256 lit pixels (`max_sparks` in `limits.toml`).

## Options

| Option | Values |
| --- | --- |
| Size | Small, Medium, Large |
| Facing | Faces the viewer, or a fixed direction (south, west, north, east) |
| Spin | On / off |
| Sparkle | On / off |
| Fade | None, or the selected color. Shift-click to clear it |

## Text mode

Type in the text box and press **Apply** to fill the grid with text.

- Up to 12 characters, up to 8 per line.
- `|` starts the second line, for example `HAPPY|2026`.
- Letters A-Z, numbers 0-9, space and `! ? . , - ' : + / & * # < > ( ) =`.
- The grid grows to fit the text, up to 48 wide.

`text_mode = false` in `features.toml` turns text mode off.

## Ready-made patterns

Pick one with < / > and press **Load pattern**: Smiley, Star, Ghost, Sword, Crown, Skull, Pumpkin, Snowflake, Christmas Tree, Present, GG, Happy New Year and Welcome.

They can also be given with `/fireworks give pattern <id>`.

## Saving

Type a **Card name** if you want one, then press **Save**. Saving an empty grid clears the card.

## Using a card

Put the card in a burst slot of the [Firework Workbench](Firework-Workbench). The card is not used up when you craft.

## Copying a card

Put a drawn card and blank cards in a crafting grid. You get one copy per blank card, and the original stays.
