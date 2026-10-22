package com.nsclient.module.impl;

import com.nsclient.module.Category;
import com.nsclient.module.HudModule;
import com.nsclient.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

public class TargetHudModule extends HudModule {
    private final ColorSetting color = register(new ColorSetting("color", 0xFFB388FF));

    public TargetHudModule() {
        super("target_hud", "Target Render", "Name and health of your crosshair target", Category.COMBAT, true, 100, 10);
    }

    @Override public int getPreviewWidth() { return 120; }
    @Override public int getPreviewHeight() { return 22; }

    @Override
    public void renderHud(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        LivingEntity target = getLookedAtLivingEntity(mc);
        if (target == null) return;

        context.getMatrices().push();
        context.getMatrices().translate(getX(), getY(), 0);
        context.getMatrices().scale((float) getScale(), (float) getScale(), 1f);

        String name = target.getName().getString();
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        context.drawTextWithShadow(mc.textRenderer, name, 0, 0, color.getValue());
        context.drawTextWithShadow(mc.textRenderer, String.format("%.1f / %.1f HP", health, maxHealth), 0, 11, color.getValue());

        context.getMatrices().pop();
    }

    public static LivingEntity getLookedAtLivingEntity(MinecraftClient mc) {
        Entity hovered = mc.targetedEntity;
        if (hovered instanceof LivingEntity living && living.isAlive()) {
            return living;
        }
        if (mc.crosshairTarget instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }
}
