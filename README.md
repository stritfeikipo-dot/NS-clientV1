# NS Client

Client-side Fabric PvP visuals mod for **Minecraft 1.21.11**, built for the Holyworld server.
No aimbot, no auto-attack, no combat automation — everything here is cosmetic/informational.

- Mod ID: `nsclient`
- Package: `com.nsclient`
- Build artifact: `NS-Client.jar`
- Theme: "Modern Purple" (dark/black/purple panels, neon purple accents)

## Features implemented

- **Mergeable HUD pills** (Soup-style): FPS, CPS, ping, coordinates and the watermark
  ("SN" short tag by default) can be dragged onto each other in the HUD Editor to merge
  into one pill with a small icon in front of each stat ("◆SN | 🖥91 FPS | 📶0 ms ..."),
  right-click a merged item to pull it back out, and the pill can use either each
  member's own color or one shared color (toggle in the editor). Rendered with a
  translucent "liquid glass" fill (soft top sheen + bright rim) — see the honest caveat
  about real blur below.
- **HUD**: FPS, CPS, ping, coordinates, keystrokes (WASD), watermark — all draggable,
  scalable (mouse wheel in the GUI) and color-configurable, position persisted.
- **Target HUD**: name + HP of whatever entity you're looking at.
- **Hitmarker**: cross / star / ring styles with a punch-scale + fade animation (duration
  and size configurable), not just a static flash.
- **Combo counter**: increments on repeated hits on the same entity within 2.5s, with a
  "punch" scale-up animation on every new hit.
- **Kill effects**: burst / ring / upward particle styles, configurable particle count.
- **Custom crosshair**: cross / dot / circle / T / X shapes (click "Shape: X >" in the
  GUI to cycle), adjustable size/thickness/color, optional hit-triggered pulse animation
  (strength + speed sliders); cancels the vanilla crosshair via a mixin.
- **Armor HUD** (durability %) and **potion effect HUD** (name, amplifier, time left).
- **ClickGUI v2** (Eclipse-style layout):
  - Top bar: search box that filters the module list live.
  - Icon sidebar: **Visuals** / **HUD** / **Misc** module categories, plus a **GENERAL**
    section with **Themes**, **HUD** (full-screen drag editor — drag elements onto each
    other to merge, right-click a merged one to split it back out), **Socials**,
    **Keybinds**, **Configs**.
  - **Visuals** has **Player / World** sub-tabs (Player: crosshair, hitmarker, combo
    counter, target render; World: kill effects).
  - Module rows show a title + one-line description, with a minimalist ring→pill
    animated toggle (off = plain ring, no track; on = filled purple pill + white knob).
  - **Themes**: 7 accent-color swatches, applied instantly to every module's color
    setting.
  - **Keybinds**: one flat list of every module's hotkey, click to rebind.
  - **Configs**: save/load/delete named profiles (`config/nsclient/profiles/<name>.json`)
    in addition to the always-persisted active config.
  - Rounded-corner cards throughout, eased slide-in on open, animated hover/expand.
  - Live HUD drag-and-drop + scroll-to-resize still works screen-wide in any section.
  - All panels now use the same translucent "liquid glass" fill as the HUD pills.
  - Per-module icons in the module *list* rows were intentionally skipped (lower
    priority than the above); icons do exist on the sidebar nav and the HUD pill segments.

**On "liquid glass":** there's no real background blur here — that needs a GPU
post-process shader, which is a much larger and riskier addition on a brand-new
Minecraft version I can't test. What's implemented is a translucent fill with a
gradient sheen and a bright top rim (`UIRenderUtil.fillGlass`), which gets the "glassy"
look without touching the rendering pipeline.
- **Config persistence**: `config/nsclient/config.json`, saved on GUI close, on
  disconnect, and on shutdown; loaded on launch.

## Building

This was written in a sandbox with **no network access**, so the Gradle build has not
been run here — the dependency versions below were checked against current Fabric/Modrinth
pages, but actually compiling against 1.21.11's real (obfuscated, Yarn-mapped) classes is
something only a real environment can confirm.

**On your machine / on GitHub Actions (`.github/workflows/build.yml` does this for you):**

```bash
gradle build
