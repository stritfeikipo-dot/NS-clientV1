package com.nsclient.module;

/**
 * Implemented by simple single-line HUD modules (FPS, CPS, ping, coords, watermark)
 * so they can be merged into a HudGroup pill instead of rendering as their own
 * floating element. See com.nsclient.hud.HudGroup / HudGroupManager.
 */
public interface TextStat {
    String getStatText();
    int getStatColor();
}
