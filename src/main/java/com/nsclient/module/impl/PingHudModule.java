package com.nsclient.module.impl;

import com.nsclient.module.Category;
import com.nsclient.module.HudModule;
import com.nsclient.module.TextStat;
import com.nsclient.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class PingHudModule extends HudModule implements TextStat {
    private final ColorSetting color = register(new ColorSetting("color", 0xFFB388FF));

    public PingHudModule() {
        super("ping_hud", "Ping", "Your connection latency", Category.HUD, true, 5, 40);
    }

    @Override public int getPreviewWidth() { return 70; }
    @Override public int getPreviewHeight() { return 10; }

    @Override
    public String getStatText() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int ping = 0;
        if (mc.getNetworkHandler() != null && mc.player != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }
        return ping + " ms";
    }

    @Override
    public int getStatColor() { return color.getValue(); }

    @Override
    public void renderHud(DrawContext context, float tickDelta) {
        if (isGrouped()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        context.getMatrices().push();
        context.getMatrices().translate(getX(), getY(), 0);
        context.getMatrices().scale((float) getScale(), (float) getScale(), 1f);
        context.drawTextWithShadow(mc.textRenderer, "Ping: " + getStatText(), 0, 0, color.getValue());
        context.getMatrices().pop();
    }
}
