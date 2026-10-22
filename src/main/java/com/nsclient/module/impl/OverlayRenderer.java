package com.nsclient.module.impl;

import net.minecraft.client.gui.DrawContext;

/** Implemented by modules that draw a fixed-position overlay (crosshair, hitmarker) rather than a draggable HudModule. */
public interface OverlayRenderer {
    void renderOverlay(DrawContext context, float tickDelta);
}
