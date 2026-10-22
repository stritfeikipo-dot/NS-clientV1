package com.nsclient.module.impl;

import com.google.gson.JsonObject;
import com.nsclient.NSClientClient;
import com.nsclient.module.Category;
import com.nsclient.module.Module;
import com.nsclient.module.setting.BooleanSetting;
import com.nsclient.module.setting.ColorSetting;
import com.nsclient.module.setting.SliderSetting;
import com.nsclient.util.UIRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Custom crosshair. When enabled, an injected mixin (InGameHudMixin) cancels the vanilla crosshair. */
public class CrosshairModule extends Module implements OverlayRenderer {
    private final ColorSetting color = register(new ColorSetting("color", 0xFFB388FF));
    private final SliderSetting size = register(new SliderSetting("size", 8.0, 4.0, 20.0, 1.0));
    private final SliderSetting thickness = register(new SliderSetting("thickness", 1.0, 1.0, 4.0, 1.0));
    private final BooleanSetting animateOnHit = register(new BooleanSetting("animate_on_hit", true));
    private final SliderSetting pulseStrength = register(new SliderSetting("pulse_strength", 0.6, 0.0, 1.5, 0.05));
    private final SliderSetting pulseSpeed = register(new SliderSetting("pulse_speed", 12.0, 4.0, 24.0, 1.0));

    public enum Shape { CROSS, DOT, CIRCLE, T, X }
    private Shape shape = Shape.CROSS;

    public CrosshairModule() {
        super("custom_crosshair", "Custom Crosshair", "A custom-shaped, animated crosshair", Category.COMBAT, false);
    }

    public Shape getShape() { return shape; }
    public void cycleShape() { shape = Shape.values()[(shape.ordinal() + 1) % Shape.values().length]; }

    @Override
    public void renderOverlay(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null) return;
        int cx = context.getScaledWindowWidth() / 2;
        int cy = context.getScaledWindowHeight() / 2;

        // Pulse factor: a quick decaying sine burst that kicks off on every registered hit.
        float pulse = 0f;
        if (animateOnHit.getValue()) {
            long elapsedMs = System.currentTimeMillis() - NSClientClient.MODULES.getCombatTracker().getLastHitTimestamp();
            if (elapsedMs >= 0 && elapsedMs < 400) {
                float t = elapsedMs / 400f;
                float decay = 1f - UIRenderUtil.easeOutCubic(t);
                pulse = (float) Math.sin(t * pulseSpeed.getValue()) * decay * pulseStrength.getValue().floatValue();
            }
        }

        int s = Math.max(1, Math.round((float) size.getValue() * (1f + pulse)));
        int t = Math.max(1, (int) thickness.getValue().doubleValue());
        int argb = color.getValue();

        switch (shape) {
            case CROSS -> {
                context.fill(cx - s, cy - t / 2, cx + s, cy - t / 2 + t, argb);
                context.fill(cx - t / 2, cy - s, cx - t / 2 + t, cy + s, argb);
            }
            case DOT -> context.fill(cx - t, cy - t, cx + t, cy + t, argb);
            case CIRCLE -> drawCircleOutline(context, cx, cy, s, argb);
            case T -> {
                context.fill(cx - s, cy - t / 2, cx + s, cy - t / 2 + t, argb);
                context.fill(cx - t / 2, cy - t / 2, cx - t / 2 + t, cy + s, argb);
            }
            case X -> drawDiagonalCross(context, cx, cy, s, t, argb);
        }
    }

    private void drawCircleOutline(DrawContext context, int cx, int cy, int radius, int argb) {
        int segments = 24;
        for (int i = 0; i < segments; i++) {
            double a = 2 * Math.PI * i / segments;
            int x = cx + (int) (Math.cos(a) * radius);
            int y = cy + (int) (Math.sin(a) * radius);
            context.fill(x, y, x + 1, y + 1, argb);
        }
    }

    private void drawDiagonalCross(DrawContext context, int cx, int cy, int s, int t, int argb) {
        int steps = s;
        for (int i = -steps; i <= steps; i++) {
            context.fill(cx + i, cy + i, cx + i + t, cy + i + t, argb);
            context.fill(cx + i, cy - i, cx + i + t, cy - i + t, argb);
        }
    }

    @Override
    public void writeConfig(JsonObject obj) {
        super.writeConfig(obj);
        obj.addProperty("shape", shape.name());
    }

    @Override
    public void readConfig(JsonObject obj) {
        super.readConfig(obj);
        if (obj.has("shape")) {
            try { shape = Shape.valueOf(obj.get("shape").getAsString()); } catch (IllegalArgumentException ignored) {}
        }
    }
}
