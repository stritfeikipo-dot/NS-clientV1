package com.nsclient.module.impl;

import com.nsclient.module.Category;
import com.nsclient.module.HudModule;
import com.nsclient.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

public class KeystrokesHudModule extends HudModule {
    private final ColorSetting activeColor = register(new ColorSetting("active_color", 0xFFB388FF));
    private final ColorSetting idleColor = register(new ColorSetting("idle_color", 0x55FFFFFF));

    public KeystrokesHudModule() {
        super("keystrokes_hud", "Keystrokes", "Shows WASD key presses", Category.HUD, true, 5, 65);
    }

    @Override public int getPreviewWidth() { return 62; }
    @Override public int getPreviewHeight() { return 42; }

    @Override
    public void renderHud(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return;
        context.getMatrices().push();
        context.getMatrices().translate(getX(), getY(), 0);
        context.getMatrices().scale((float) getScale(), (float) getScale(), 1f);

        int size = 18;
        drawKey(context, mc, mc.options.forwardKey, size, 0, size, size, "W");
        drawKey(context, mc, mc.options.leftKey, 0, size, size, size, "A");
        drawKey(context, mc, mc.options.backKey, size, size, size, size, "S");
        drawKey(context, mc, mc.options.rightKey, size * 2, size, size, size, "D");

        context.getMatrices().pop();
    }

    private void drawKey(DrawContext context, MinecraftClient mc, KeyBinding key, int x, int y, int w, int h, String label) {
        boolean down = key.isPressed();
        int bg = down ? (0xAA000000 | (activeColor.getValue() & 0xFFFFFF)) : 0x55000000;
        context.fill(x, y, x + w, y + h, bg);
        int textColor = down ? activeColor.getValue() : idleColor.getValue();
        int tw = mc.textRenderer.getWidth(label);
        context.drawTextWithShadow(mc.textRenderer, label, x + (w - tw) / 2, y + (h - 9) / 2, textColor);
    }
}
