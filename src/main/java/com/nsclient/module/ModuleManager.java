package com.nsclient.module;

import com.nsclient.combat.CombatTracker;
import com.nsclient.hud.HudGroupManager;
import com.nsclient.module.impl.ArmorHudModule;
import com.nsclient.module.impl.ComboCounterModule;
import com.nsclient.module.impl.CoordsHudModule;
import com.nsclient.module.impl.CpsHudModule;
import com.nsclient.module.impl.CrosshairModule;
import com.nsclient.module.impl.FpsHudModule;
import com.nsclient.module.impl.HitmarkerModule;
import com.nsclient.module.impl.KeystrokesHudModule;
import com.nsclient.module.impl.KillEffectsModule;
import com.nsclient.module.impl.OverlayRenderer;
import com.nsclient.module.impl.PingHudModule;
import com.nsclient.module.impl.PotionHudModule;
import com.nsclient.module.impl.Tickable;
import com.nsclient.module.impl.TargetHudModule;
import com.nsclient.module.impl.WatermarkHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final CombatTracker combatTracker = new CombatTracker();
    private final HudGroupManager groupManager = new HudGroupManager();
    private final Map<Module, Boolean> keyWasDown = new HashMap<>();

    public ModuleManager() {
        modules.add(new WatermarkHudModule());
        modules.add(new FpsHudModule());
        modules.add(new CpsHudModule());
        modules.add(new PingHudModule());
        modules.add(new CoordsHudModule());
        modules.add(new KeystrokesHudModule());
        modules.add(new ArmorHudModule());
        modules.add(new PotionHudModule());
        modules.add(new TargetHudModule());
        modules.add(new ComboCounterModule());
        modules.add(new CrosshairModule());
        modules.add(new HitmarkerModule());
        modules.add(new KillEffectsModule());
    }

    public List<Module> getModules() { return modules; }
    public CombatTracker getCombatTracker() { return combatTracker; }
    public HudGroupManager getGroupManager() { return groupManager; }

    public Module getByName(String name) {
        for (Module m : modules) if (m.getName().equals(name)) return m;
        return null;
    }

    public void tick(MinecraftClient client) {
        combatTracker.tick(client);
        for (Module m : modules) {
            if (m instanceof Tickable tickable) {
                tickable.onTick(client);
            }
        }
    }

    /** Polls user-assigned module hotkeys (set via the ClickGUI, not vanilla KeyBindings). */
    public void handleKeybinds(MinecraftClient client) {
        if (client.currentScreen != null || client.getWindow() == null) return;
        for (Module m : modules) {
            if (m.getKeyCode() == -1) continue;
            boolean down = GLFW.glfwGetKey(client.getWindow().getHandle(), m.getKeyCode()) == GLFW.GLFW_PRESS;
            if (down && !keyWasDown.getOrDefault(m, false)) {
                m.toggle();
            }
            keyWasDown.put(m, down);
        }
    }

    public void renderHud(DrawContext context, float tickDelta) {
        for (Module m : modules) {
            if (!m.isEnabled()) continue;
            if (m instanceof HudModule hudModule) {
                hudModule.renderHud(context, tickDelta);
            } else if (m instanceof OverlayRenderer overlay) {
                overlay.renderOverlay(context, tickDelta);
            }
        }
        groupManager.render(context, this);
    }
}
