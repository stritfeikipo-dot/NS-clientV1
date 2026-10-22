package com.nsclient.module.impl;

import com.google.gson.JsonObject;
import com.nsclient.NSClientClient;
import com.nsclient.module.Category;
import com.nsclient.module.Module;
import com.nsclient.module.setting.ColorSetting;
import com.nsclient.module.setting.SliderSetting;
import com.nsclient.util.UIRenderUtil;
import net.minecraft.client.gui.DrawContext;

public class HitmarkerModule extends Module implements OverlayRenderer {
    private final ColorSetting color = register(new ColorSetting("color", 0xFFFFFFFF));
    private final SliderSetting durationMs = register(new SliderSetting("duration_ms", 300.0, 100.0, 800.0, 25.0));
    private final SliderSetting baseSize = register(new SliderSetting("size", 6.0, 3.0, 14.0, 1.0));

    public enum Style { CROSS, STAR, RING }
    private Style style = Style.CROSS;

    public HitmarkerModule() {
        super("hitmarker", "Hitmarker", "Flash and animation on a successful hit", Category.COMBAT, true);
    }

    public Style getStyle() { return style; }
    public void cycleStyle() { style = Style.values()[(style.ordinal() + 1) % Style.values().length]; }

    @Override
    public void renderOverlay(DrawContext context, float tickDelta) {
        long last = NSClientClient.MODULES.getCombatTracker().getLastHitTimestamp();
        if (last < 0) return;
        long elapsed = System.currentTimeMillis() - last;
        float durMs = durationMs.getValue().floatValue();
        if (elapsed > durMs) return;

        float progress = elapsed / durMs;
        // Punch out fast, then fade: scale overshoots slightly before settling, alpha fades the whole time.
        float scale = UIRenderUtil.easeOutBack(Math.min(1f, progress * 2.2f));
        float alpha = 1f - UIRenderUtil.easeOutCubic(progress);
        int argb = (Math.round(255 * UIRenderUtil.clamp01(alpha)) << 24) | (color.getValue() & 0xFFFFFF);

        int cx = context.getScaledWindowWidth() / 2;
        int cy = context.getScaledWindowHeight() / 2;
        int len = Math.max(1, Math.round(baseSize.getValue().floatValue() * scale));
        int gap = Math.max(1, len / 2);

        switch (style) {
            case CROSS -> {
                context.fill(cx - gap - len, cy - gap - len, cx - gap, cy - gap, argb);
                context.fill(cx + gap, cy - gap - len, cx + gap + len, cy - gap, argb);
                context.fill(cx - gap - len, cy + gap, cx - gap, cy + gap + len, argb);
                context.fill(cx + gap, cy + gap, cx + gap + len, cy + gap + len, argb);
            }
            case STAR -> drawStar(context, cx, cy, len + gap, argb);
            case RING -> drawRing(context, cx, cy, len + gap, argb);
        }
    }

    private void drawStar(DrawContext context, int cx, int cy, int radius, int argb) {
        int points = 8;
        for (int i = 0; i < points; i++) {
            double a = 2 * Math.PI * i / points;
            int x1 = cx + (int) (Math.cos(a) * radius * 0.4);
            int y1 = cy + (int) (Math.sin(a) * radius * 0.4);
            int x2 = cx + (int) (Math.cos(a) * radius);
            int y2 = cy + (int) (Math.sin(a) * radius);
            drawLine(context, x1, y1, x2, y2, argb);
        }
    }

    private void drawRing(DrawContext context, int cx, int cy, int radius, int argb) {
        int segments = 20;
        for (int i = 0; i < segments; i++) {
            double a = 2 * Math.PI * i / segments;
            int x = cx + (int) (Math.cos(a) * radius);
            int y = cy + (int) (Math.sin(a) * radius);
            context.fill(x, y, x + 1, y + 1, argb);
        }
    }

    private void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int argb) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)) + 1;
        for (int i = 0; i <= steps; i++) {
            float t = steps == 0 ? 0 : (float) i / steps;
            int x = Math.round(x1 + (x2 - x1) * t);
            int y = Math.round(y1 + (y2 - y1) * t);
            context.fill(x, y, x + 1, y + 1, argb);
        }
    }

    @Override
    public void writeConfig(JsonObject obj) {
        super.writeConfig(obj);
        obj.addProperty("style", style.name());
    }

    @Override
    public void readConfig(JsonObject obj) {
        super.readConfig(obj);
        if (obj.has("style")) {
            try { style = Style.valueOf(obj.get("style").getAsString()); } catch (IllegalArgumentException ignored) {}
        }
    }
}
