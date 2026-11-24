# Firework Workbench

Right-click the workbench to open it. Items left in it go back to you when you close the screen.

## Slots

- **Paper:** 1 per rocket.
- **Gunpowder:** the flight, 1-3. Set it with the Flight - / + buttons.
- **Bursts:** up to 8 slots for firework stars or Pattern Cards. Each slot holds up to 16.

## Editing a burst

Click a burst slot, or its dot on the formation view, to select it.

- **< / >** picks the shape. A Pattern Card always uses its own pattern.
- **Size** and **Delay** (in ticks, 20 = 1 second). Shift makes the delay steps bigger.
- **X / Y / Z** move the burst, or drag its dot on the formation view. Shift makes the steps finer.
- **Copy to all** copies the selected burst's size, and its shape to every firework star slot.
- **Reset** puts the selected burst back to its defaults: star shape, size 1.0, no offset, no delay.

The furthest offset and longest delay are set in `limits.toml` (`max_offset_blocks`, `max_delay_ticks`).

## Formations

Pick a formation with < / >, set the **Spacing**, then press **Apply** to place every burst at once:

Single, Ring, Line, Cross, Stack, Multi-break and Random cluster.

## Preview and Craft

- **Preview** pops the rocket in the sky in front of you without using anything. The view tilts up while it plays and goes back after. Click or press a key to end it early.
- Type a **Rocket name** if you want one.
- **Craft** uses 1 paper, the flight's gunpowder and one of each firework star. Pattern Cards are not used up.

## Rocket presets

The bottom of the screen lists the [rocket presets](Rocket-Presets). Pick one with < / >, **Preview** it, or press **Make**.

Making a preset costs 1 paper, the preset's flight in gunpowder, plus 1 gunpowder per burst. The extra per burst is `preset_gunpowder_per_burst` in `limits.toml`.

## Switches

- `shells = false` in `features.toml` turns custom rockets off. The workbench still makes presets.
- The `fireworks.design` permission controls who can use the workbench.
