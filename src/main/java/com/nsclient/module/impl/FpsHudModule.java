package com.nsclient.module.impl;

import com.nsclient.module.Category;
import com.nsclient.module.HudModule;
import com.nsclient.module.TextStat;
import com.nsclient.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FpsHudModule extends HudModule implements TextStat {
    private final ColorSetting color = register(new ColorSetting("color", 0xFFB388FF));

    public FpsHudModule() {
        super("fps_hud", "FPS", "Current frames per second", Category.HUD, true, 5, 18);
    }

    @Override public int getPreviewWidth() { return 60; }
    @Override public int getPreviewHeight() { return 10; }

    @Override
    public String getStatText() { return MinecraftClient.getInstance().getCurrentFps() + " FPS"; }

    @Override
    public int getStatColor() { return color.getValue(); }

    @Override
    public void renderHud(DrawContext context, float tickDelta) {
        if (isGrouped()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        context.getMatrices().push();
        context.getMatrices().translate(getX(), getY(), 0);
        context.getMatrices().scale((float) getScale(), (float) getScale(), 1f);
        context.drawTextWithShadow(mc.textRenderer, getStatText(), 0, 0, color.getValue());
        context.getMatrices().pop();
    }
}
