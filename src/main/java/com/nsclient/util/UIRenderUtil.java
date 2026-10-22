package com.nsclient.util;

import net.minecraft.client.gui.DrawContext;

/** Small rendering helpers shared by the ClickGUI and HUD: rounded panels + easing for animations. */
public final class UIRenderUtil {

    private UIRenderUtil() {}

    /**
     * Fills a rectangle with rounded corners. Implemented without a texture: the straight
     * edges use normal fills, and each corner is filled pixel-row-by-pixel inside its
     * quarter-circle so it works on any DrawContext without extra GPU state.
     */
    public static void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int radius, int color) {
        if (x2 <= x1 || y2 <= y1) return;
        radius = Math.max(0, Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2)));
        if (radius == 0) {
            context.fill(x1, y1, x2, y2, color);
            return;
        }

        // Center cross (covers everything except the four corner squares)
        context.fill(x1 + radius, y1, x2 - radius, y2, color);
        context.fill(x1, y1 + radius, x1 + radius, y2 - radius, color);
        context.fill(x2 - radius, y1 + radius, x2, y2 - radius, color);

        fillCorner(context, x1 + radius, y1 + radius, radius, color, true, true);
        fillCorner(context, x2 - radius, y1 + radius, radius, color, false, true);
        fillCorner(context, x1 + radius, y2 - radius, radius, color, true, false);
        fillCorner(context, x2 - radius, y2 - radius, radius, color, false, false);
    }

    /**
     * "Liquid glass" panel fill: a translucent rounded base plus a soft white sheen that
     * fades from the top edge downward (brightest at the top-left, like light catching a
     * curved glass surface) and a faint bright rim along the top edge. No real blur (that
     * needs a post-process shader this environment can't safely add) - this is the closest
     * approximation using plain translucent fills.
     */
    public static void fillGlass(DrawContext context, int x1, int y1, int x2, int y2, int radius, int baseColor) {
        fillRounded(context, x1, y1, x2, y2, radius, baseColor);
        if (x2 <= x1 || y2 <= y1) return;

        int h = y2 - y1;
        int sheenH = Math.max(2, Math.min(h, h / 2));
        for (int i = 0; i < sheenH; i++) {
            float t = 1f - (float) i / sheenH;
            int extraA = Math.round(36 * t * t);
            if (extraA <= 0) continue;
            int rowY = y1 + i;
            int insetX1 = x1;
            int insetX2 = x2;
            if (i < radius) {
                int dy = radius - 1 - i;
                int dx = (int) Math.round(Math.sqrt(Math.max(0, radius * radius - dy * dy)));
                insetX1 = x1 + radius - dx;
                insetX2 = x2 - radius + dx;
            }
            if (insetX2 > insetX1) {
                context.fill(insetX1, rowY, insetX2, rowY + 1, (extraA << 24) | 0xFFFFFF);
            }
        }

        if (x2 - radius > x1 + radius) {
            context.fill(x1 + radius, y1, x2 - radius, y1 + 1, 0x40FFFFFF);
        }
    }

    /** Draws a 1px rounded border; same corner technique as fillRounded but outline-only via two fillRounded calls. */
    public static void drawRoundedOutline(DrawContext context, int x1, int y1, int x2, int y2, int radius, int thickness, int color) {
        fillRounded(context, x1, y1, x2, y2, radius, color);
        if (x2 - x1 > thickness * 2 && y2 - y1 > thickness * 2) {
            fillRounded(context, x1 + thickness, y1 + thickness, x2 - thickness, y2 - thickness, Math.max(0, radius - thickness), 0x00000000);
        }
    }

    private static void fillCorner(DrawContext context, int cx, int cy, int radius, int color, boolean left, boolean top) {
        for (int dy = 0; dy < radius; dy++) {
            int dx = (int) Math.round(Math.sqrt(Math.max(0, radius * radius - dy * dy)));
            int rowY = top ? cy - 1 - dy : cy + dy;
            int startX = left ? cx - dx : cx;
            int endX = left ? cx : cx + dx;
            if (endX > startX) context.fill(startX, rowY, endX, rowY + 1, color);
        }
    }

    // ---------------------------------------------------------------- easing

    public static float easeOutCubic(float t) {
        t = clamp01(t);
        float f = t - 1;
        return f * f * f + 1;
    }

    public static float easeOutBack(float t) {
        t = clamp01(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        float f = t - 1;
        return 1 + c3 * f * f * f + c1 * f * f;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * clamp01(t);
    }

    public static int lerpArgb(int from, int to, float t) {
        t = clamp01(t);
        int fa = (from >> 24) & 0xFF, fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int ta = (to >> 24) & 0xFF, tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int a = (int) lerp(fa, ta, t), r = (int) lerp(fr, tr, t), g = (int) lerp(fg, tg, t), b = (int) lerp(fb, tb, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
}
