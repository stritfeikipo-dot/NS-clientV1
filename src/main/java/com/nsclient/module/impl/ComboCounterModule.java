package com.nsclient.module.impl;

import com.nsclient.NSClientClient;
import com.nsclient.module.Category;
import com.nsclient.module.HudModule;
import com.nsclient.module.setting.ColorSetting;
import com.nsclient.util.UIRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ComboCounterModule extends HudModule {
    private final ColorSetting color = register(new ColorSetting("color", 0xFFB388FF));

    private int lastSeenCombo = 0;
    private long punchStartMs = -1;

    public ComboCounterModule() {
        super("combo_counter", "Combo Counter", "Counts consecutive hits on one target", Category.COMBAT, true, 5, 200);
    }

    @Override public int getPreviewWidth() { return 90; }
    @Override public int getPreviewHeight() { return 12; }

    @Override
    public void renderHud(DrawContext context, float tickDelta) {
        int combo = NSClientClient.MODULES.getCombatTracker().getCombo();
        if (combo < 2) { lastSeenCombo = combo; return; }

        if (combo != lastSeenCombo) {
            punchStartMs = System.currentTimeMillis();
            lastSeenCombo = combo;
        }

        float punchScale = 1f;
        if (punchStartMs > 0) {
            float elapsed = (System.currentTimeMillis() - punchStartMs) / 220f;
            if (elapsed < 1f) {
                punchScale = 1f + 0.35f * (1f - UIRenderUtil.easeOutBack(elapsed));
            }
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        String text = "Combo x" + combo;
        int textW = mc.textRenderer.getWidth(text);

        context.getMatrices().push();
        context.getMatrices().translate(getX() + textW / 2.0, getY() + 5, 0);
        context.getMatrices().scale((float) getScale() * punchScale, (float) getScale() * punchScale, 1f);
        context.getMatrices().translate(-textW / 2.0, -5, 0);
        context.drawTextWithShadow(mc.textRenderer, text, 0, 0, color.getValue());
        context.getMatrices().pop();
    }
}
