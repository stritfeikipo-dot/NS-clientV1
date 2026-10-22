package com.nsclient.module.impl;

import com.nsclient.module.Category;
import com.nsclient.module.HudModule;
import com.nsclient.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorHudModule extends HudModule {
    private final ColorSetting color = register(new ColorSetting("color", 0xFFB388FF));
    private static final EquipmentSlot[] SLOTS = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    public ArmorHudModule() {
        super("armor_hud", "Armor HUD", "Durability of each armor piece", Category.HUD, true, 5, 120);
    }

    @Override public int getPreviewWidth() { return 90; }
    @Override public int getPreviewHeight() { return 48; }

    @Override
    public void renderHud(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        context.getMatrices().push();
        context.getMatrices().translate(getX(), getY(), 0);
        context.getMatrices().scale((float) getScale(), (float) getScale(), 1f);

        int yOff = 0;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            String label = stack.isEmpty() ? slot.getName() + ": -" : slot.getName() + ": " + describeDurability(stack);
            context.drawTextWithShadow(mc.textRenderer, label, 0, yOff, color.getValue());
            yOff += 11;
        }

        context.getMatrices().pop();
    }

    private String describeDurability(ItemStack stack) {
        if (!stack.isDamageable()) return "OK";
        int max = stack.getMaxDamage();
        int cur = max - stack.getDamage();
        int pct = max == 0 ? 100 : (int) (100.0 * cur / max);
        return pct + "%";
    }
}
