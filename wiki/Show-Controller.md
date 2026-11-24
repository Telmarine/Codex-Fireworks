# Show Controller

Runs a timed show across linked [launchers](Launcher).

## Linking launchers

1. Shift-right-click the controller, or press **Link** in its screen.
2. Shift-right-click each launcher to add it. Shift-right-click a linked launcher again to remove it.
3. Shift-right-click the controller again to finish.

- Works with an item in your hand.
- Linking ends by itself after 2 minutes.
- Launchers must be within `link_range` blocks (default 48) and in the same dimension.
- Launchers are numbered #1, #2, ... in the order they were linked.
- Up to `max_launchers` per controller (default 32).

## Editing the show

Right-click the controller to open it.

- **Add cue** adds a cue half a second after the selected one. **Remove** deletes the selected cue.
- Each cue has:
  - **Time:** when it fires, in steps of 0.25 s (Shift: 1 s).
  - **Launcher:** one launcher (#1, #2, ...) or All.
  - **Slot:** a rocket slot (1-9) or Next for the next rocket in turn.
- **Test cue** fires the selected cue right away.
- **Save** stores the show and closes the screen.
- Up to `max_cues` cues per show (default 200).

## Running

- **Start** and **Stop** in the screen.
- **Redstone: on** starts the show on a redstone pulse.
- **Loop: on** starts the show over when it ends. A looping show stops by itself once all its launchers are empty.
- Give the show a **name** to start it with `/fireworks show start <name>`. Names are unique per server.
- Up to `max_shows_per_area` shows (default 2) can run within 128 blocks of each other.

Shows don't keep running through a server restart.

## Show Cards

A Show Card stores a show so it can be copied between controllers.

- Hold a Show Card and press **Write card** to save the show onto it. Writing from a stack of blank cards writes one card and puts it in your inventory.
- Hold a written card (in either hand) and press **Read card** to load its show. Cues for a launcher number this controller doesn't have go to its last linked launcher.

## Who can use it

A controller belongs to the player who placed it. See [Protection](Protection). The `fireworks.show.edit` permission controls who can edit shows and link launchers.

`shows = false` in `features.toml` turns the Show Controller, Show Cards and `/fireworks show` off.
