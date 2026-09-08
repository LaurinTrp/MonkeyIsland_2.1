# Monkey Island 2.1

A small Java Swing prototype inspired by classic Monkey Island: start menu, room background with foreground props, and Guybrush walking in alibi-3D on a marked floor.

## Requirements

- Java 17+ (project uses modern language features)
- Working directory = project root so `assets/` resolves

Optional: set env `MonkeyIslandAssets` to an absolute path to the `assets` folder if you run from elsewhere.

## Run

### Eclipse

1. Import the project (existing Eclipse project).
2. Run `main.Main`.
3. Ensure the run configuration working directory is the project root (`MonkeyIsland_2.1`).

### Command line

```bash
javac -encoding UTF-8 -d bin $(find src -name '*.java')
java -cp bin main.Main
```

Run those from the project root.

## Controls

| Input | Action |
|--------|--------|
| **WASD** / **Arrow keys** | Walk (left / right / into room / toward camera) |
| **Back** (UI) | Return to start screen |

Movement is clamped to the red walk outline in `Background_bounding.jpg`. Sprite scale and walk speed increase as Guybrush moves toward the camera.

## Screens

1. **Start** — animated `StartMenu.gif`, then Start / Settings / Exit  
2. **Game** — room scene with Guybrush  
3. **Settings** — placeholder  

## Assets

Under `assets/`:

| Path | Role |
|------|------|
| `pictures/StartMenu.gif` | Start menu animation |
| `pictures/Background_closed.jpg` | Game background |
| `pictures/Background_open.jpg` | Alternate room (unused in play yet) |
| `pictures/Background_bounding.jpg` | Same view with **bright red** walk edges |
| `pictures/Foreground.png` | Drawn above Guybrush (props in front) |
| `pictures/guybrush/` | Walk sheets, idles (`idle.png` = left/right idle) |
| `pictures/Guybrush_Sprite.png` | Source sheet for sprite extraction |
| `fonts/` | UI display font |

Walkable area = on or below the red line (toward the bottom of the screen). Marker red is near `#FE0000` so it is not confused with warm wood or fire.

## Packages

```
src/
  main/       Entry + game loop
  game/       Screens, Guybrush, WalkBounds, fonts
  gui/        Window, panels, start/game screens, widgets
  assets/     Resource paths, image/GIF/sprite loading
  input/      Menu mouse handling
```

## Notes

- Window opens fullscreen-ish on the default monitor (screen size minus insets).
- On Hyprland/Wayland, an undecorated XWayland window may need a float/center window rule if it appears off-screen.
- Debug green/red walk overlay can be drawn from `WalkBounds.drawDebug` in `GameScreen` (toggle in code if you do not want it visible).
