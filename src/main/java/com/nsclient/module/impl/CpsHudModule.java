package com.nsclient.module.impl;

import com.nsclient.module.Category;
import com.nsclient.module.HudModule;
import com.nsclient.module.TextStat;
import com.nsclient.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsHudModule extends HudModule implements Tickable, TextStat {
    private final ColorSetting color = register(new ColorSetting("color", 0xFFB388FF));
    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean prevAttackDown;
    private boolean prevUseDown;

    public CpsHudModule() {
        super("cps_hud", "CPS", "Clicks per second (left / right)", Category.HUD, true, 5, 29);
    }

    @Override public int getPreviewWidth() { return 70; }
    @Override public int getPreviewHeight() { return 10; }

    @Override
    public void onTick(MinecraftClient client) {
        long now = System.currentTimeMillis();
        if (client.options != null) {
            boolean attack = client.options.attackKey.isPressed();
            if (attack && !prevAttackDown) leftClicks.add(now);
            prevAttackDown = attack;

            boolean use = client.options.useKey.isPressed();
            if (use && !prevUseDown) rightClicks.add(now);
            prevUseDown = use;
        }
        leftClicks.removeIf(t -> now - t > 1000);
        rightClicks.removeIf(t -> now - t > 1000);
    }

    @Override
    public String getStatText() { return "CPS: " + leftClicks.size() + " / " + rightClicks.size(); }

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
