package com.nsclient.module.impl;

import com.nsclient.module.Category;
import com.nsclient.module.HudModule;
import com.nsclient.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionHudModule extends HudModule {
    private final ColorSetting color = register(new ColorSetting("color", 0xFFB388FF));
    private static final String[] ROMAN = { "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };

    public PotionHudModule() {
        super("potion_hud", "Potion Effects", "Active effects and time remaining", Category.HUD, true, 5, 180);
    }

    @Override public int getPreviewWidth() { return 110; }
    @Override public int getPreviewHeight() { return 44; }

    @Override
    public void renderHud(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        context.getMatrices().push();
        context.getMatrices().translate(getX(), getY(), 0);
        context.getMatrices().scale((float) getScale(), (float) getScale(), 1f);

        int yOff = 0;
        for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            int amplifier = effect.getAmplifier() + 1;
            String roman = amplifier >= 0 && amplifier < ROMAN.length ? ROMAN[amplifier] : String.valueOf(amplifier);
            String text = name + " " + roman + " (" + formatTime(effect.getDuration()) + ")";
            context.drawTextWithShadow(mc.textRenderer, text, 0, yOff, color.getValue());
            yOff += 11;
        }

        context.getMatrices().pop();
    }

    private String formatTime(int ticks) {
        int seconds = ticks / 20;
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }
}
