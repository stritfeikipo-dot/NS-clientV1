package com.nsclient.module.impl;

import com.nsclient.module.Category;
import com.nsclient.module.HudModule;
import com.nsclient.module.TextStat;
import com.nsclient.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class CoordsHudModule extends HudModule implements TextStat {
    private final ColorSetting color = register(new ColorSetting("color", 0xFFB388FF));

    public CoordsHudModule() {
        super("coords_hud", "Coordinates", "Your current X / Y / Z position", Category.HUD, true, 5, 51);
    }

    @Override public int getPreviewWidth() { return 110; }
    @Override public int getPreviewHeight() { return 10; }

    @Override
    public String getStatText() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return "-, -, -";
        return String.format("%.0f, %.0f, %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
    }

    @Override
    public int getStatColor() { return color.getValue(); }

    @Override
    public void renderHud(DrawContext context, float tickDelta) {
        if (isGrouped()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        context.getMatrices().push();
        context.getMatrices().translate(getX(), getY(), 0);
        context.getMatrices().scale((float) getScale(), (float) getScale(), 1f);
        context.drawTextWithShadow(mc.textRenderer, getStatText(), 0, 0, color.getValue());
        context.getMatrices().pop();
    }
}
