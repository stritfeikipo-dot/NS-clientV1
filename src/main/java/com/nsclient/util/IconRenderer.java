package com.nsclient.util;

import net.minecraft.client.gui.DrawContext;

/**
 * Minimalist geometric glyphs for the sidebar nav (no texture atlas needed).
 * Each icon is drawn inside a size x size box at (x, y) with the given color.
 */
public final class IconRenderer {

    public enum Icon { VISUALS, HUD, MISC, THEMES, HUD_EDIT, SOCIALS, KEYBINDS, CONFIGS, SEARCH, CHEVRON, PLAYER, WORLD,
        FPS, CPS, PING, PIN, TAG }

    private IconRenderer() {}

    public static void draw(DrawContext context, Icon icon, int x, int y, int size, int color) {
        switch (icon) {
            case VISUALS -> drawEye(context, x, y, size, color);
            case HUD -> drawMonitor(context, x, y, size, color);
            case MISC -> drawWrench(context, x, y, size, color);
            case THEMES -> drawPalette(context, x, y, size, color);
            case HUD_EDIT -> drawMonitor(context, x, y, size, color);
            case SOCIALS -> drawBubble(context, x, y, size, color);
            case KEYBINDS -> drawKeyboard(context, x, y, size, color);
            case CONFIGS -> drawFolder(context, x, y, size, color);
            case SEARCH -> drawMagnifier(context, x, y, size, color);
            case CHEVRON -> drawChevron(context, x, y, size, color);
            case PLAYER -> drawPerson(context, x, y, size, color);
            case WORLD -> drawGlobe(context, x, y, size, color);
            case FPS -> drawSpeedometer(context, x, y, size, color);
            case CPS -> drawCursor(context, x, y, size, color);
            case PING -> drawSignalBars(context, x, y, size, color);
            case PIN -> drawPin(context, x, y, size, color);
            case TAG -> drawTag(context, x, y, size, color);
        }
    }

    private static void drawSpeedometer(DrawContext context, int x, int y, int s, int c) {
        int cx = x + s / 2, cy = y + s / 2 + 1;
        int r = s / 2 - 1;
        int segments = 10;
        for (int i = 0; i <= segments; i++) {
            double a = Math.PI + Math.PI * i / segments; // bottom half-circle arc
            int px = cx + (int) (Math.cos(a) * r);
            int py = cy + (int) (Math.sin(a) * r);
            context.fill(px, py, px + 1, py + 1, c);
        }
        // needle pointing to upper-right (high reading)
        double needleAngle = Math.PI + Math.PI * 0.72;
        int nx = cx + (int) (Math.cos(needleAngle) * (r - 1));
        int ny = cy + (int) (Math.sin(needleAngle) * (r - 1));
        drawLine(context, cx, cy, nx, ny, c);
        context.fill(cx - 1, cy - 1, cx + 1, cy + 1, c);
    }

    private static void drawCursor(DrawContext context, int x, int y, int s, int c) {
        for (int i = 0; i < s; i++) {
            context.fill(x + i / 2, y + i, x + i / 2 + 2, y + i + 1, c);
        }
        context.fill(x + s / 3, y + s - 4, x + s / 3 + 3, y + s, c);
    }

    private static void drawSignalBars(DrawContext context, int x, int y, int s, int c) {
        int bars = 3;
        int barW = Math.max(1, s / 5);
        int gap = Math.max(1, s / 8);
        for (int i = 0; i < bars; i++) {
            int barH = (s * (i + 1)) / bars;
            int bx = x + i * (barW + gap);
            int by = y + s - barH;
            context.fill(bx, by, bx + barW, y + s, c);
        }
    }

    private static void drawPin(DrawContext context, int x, int y, int s, int c) {
        int r = s / 3;
        int cx = x + s / 2;
        int cy = y + r;
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
            context.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, c);
        }
        int[] xs = { x + s / 2 - r, x + s / 2 + r, x + s / 2 };
        int[] ys = { cy + r / 2, cy + r / 2, y + s };
        triangle(context, xs, ys, c);
    }

    private static void drawTag(DrawContext context, int x, int y, int s, int c) {
        int cx = x + s / 2, cy = y + s / 2;
        int r = s / 2;
        int[] xs = { cx, cx + r, cx, cx - r };
        int[] ys = { cy - r, cy, cy + r, cy };
        drawLine(context, xs[0], ys[0], xs[1], ys[1], c);
        drawLine(context, xs[1], ys[1], xs[2], ys[2], c);
        drawLine(context, xs[2], ys[2], xs[3], ys[3], c);
        drawLine(context, xs[3], ys[3], xs[0], ys[0], c);
        context.fill(cx - 1, cy - 1, cx + 1, cy + 1, c);
    }

    private static void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int c) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)) + 1;
        for (int i = 0; i <= steps; i++) {
            float t = steps == 0 ? 0 : (float) i / steps;
            int x = Math.round(x1 + (x2 - x1) * t);
            int y = Math.round(y1 + (y2 - y1) * t);
            context.fill(x, y, x + 1, y + 1, c);
        }
    }

    private static void drawEye(DrawContext context, int x, int y, int s, int c) {
        int midY = y + s / 2;
        context.fill(x, midY - 1, x + s, midY + 1, c);
        int pupil = Math.max(2, s / 3);
        context.fill(x + (s - pupil) / 2, midY - pupil / 2, x + (s - pupil) / 2 + pupil, midY + pupil / 2, c);
    }

    private static void drawMonitor(DrawContext context, int x, int y, int s, int c) {
        int h = (int) (s * 0.7);
        outline(context, x, y, s, h, c);
        context.fill(x + s / 2 - 1, y + h, x + s / 2 + 1, y + h + 2, c);
        context.fill(x + s / 4, y + h + 2, x + s - s / 4, y + h + 3, c);
    }

    private static void drawWrench(DrawContext context, int x, int y, int s, int c) {
        context.fill(x, y + s - 2, x + s - 2, y, c);
        context.fill(x, y, x + 3, y + 3, c);
        context.fill(x + s - 4, y + s - 3, x + s - 1, y + s, c);
    }

    private static void drawPalette(DrawContext context, int x, int y, int s, int c) {
        drawCircleFilled(context, x + s / 2, y + s / 2, s / 2, c);
        context.fill(x + s / 2 - 1, y + s / 2 - 1, x + s / 2 + 2, y + s / 2 + 2, 0xFF000000);
    }

    private static void drawBubble(DrawContext context, int x, int y, int s, int c) {
        int h = (int) (s * 0.75);
        context.fill(x, y, x + s, y + h, c);
        int[] xs = {x + 2, x + 6, x + 2};
        int[] ys = {y + h, y + h, y + h + 4};
        triangle(context, xs, ys, c);
    }

    private static void drawKeyboard(DrawContext context, int x, int y, int s, int c) {
        outline(context, x, y, s, (int) (s * 0.6), c);
        int keyW = Math.max(1, s / 5);
        for (int i = 0; i < 4; i++) {
            context.fill(x + 1 + i * (keyW + 1), y + 2, x + 1 + i * (keyW + 1) + keyW, y + 2 + keyW, c);
        }
    }

    private static void drawFolder(DrawContext context, int x, int y, int s, int c) {
        context.fill(x, y + s / 4, x + s, y + s, c);
        context.fill(x, y + s / 4 - 2, x + s / 2, y + s / 4, c);
    }

    private static void drawMagnifier(DrawContext context, int x, int y, int s, int c) {
        int r = s / 2 - 1;
        drawCircleOutline(context, x + r, y + r, r, c);
        context.fill(x + s - 2, y + s - 2, x + s, y + s, c);
    }

    private static void drawChevron(DrawContext context, int x, int y, int s, int c) {
        for (int i = 0; i < s / 2; i++) {
            context.fill(x + i, y + i, x + i + 1, y + i + 2, c);
            context.fill(x + i, y + s - i - 2, x + i + 1, y + s - i, c);
        }
    }

    private static void drawPerson(DrawContext context, int x, int y, int s, int c) {
        drawCircleFilled(context, x + s / 2, y + s / 4, s / 4, c);
        context.fill(x + s / 4, y + s / 2, x + s - s / 4, y + s, c);
    }

    private static void drawGlobe(DrawContext context, int x, int y, int s, int c) {
        drawCircleOutline(context, x + s / 2, y + s / 2, s / 2 - 1, c);
        context.fill(x, y + s / 2, x + s, y + s / 2 + 1, c);
    }

    private static void outline(DrawContext context, int x, int y, int w, int h, int c) {
        context.fill(x, y, x + w, y + 1, c);
        context.fill(x, y + h - 1, x + w, y + h, c);
        context.fill(x, y, x + 1, y + h, c);
        context.fill(x + w - 1, y, x + w, y + h, c);
    }

    private static void triangle(DrawContext context, int[] xs, int[] ys, int c) {
        int minX = Math.min(xs[0], Math.min(xs[1], xs[2]));
        int maxX = Math.max(xs[0], Math.max(xs[1], xs[2]));
        int minY = Math.min(ys[0], Math.min(ys[1], ys[2]));
        int maxY = Math.max(ys[0], Math.max(ys[1], ys[2]));
        context.fill(minX, minY, maxX + 1, maxY + 1, c);
    }

    private static void drawCircleFilled(DrawContext context, int cx, int cy, int r, int c) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
            context.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, c);
        }
    }

    private static void drawCircleOutline(DrawContext context, int cx, int cy, int r, int c) {
        int segments = 16;
        for (int i = 0; i < segments; i++) {
            double a = 2 * Math.PI * i / segments;
            int x = cx + (int) (Math.cos(a) * r);
            int y = cy + (int) (Math.sin(a) * r);
            context.fill(x, y, x + 1, y + 1, c);
        }
    }
          }
