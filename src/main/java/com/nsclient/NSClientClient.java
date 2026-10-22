package com.nsclient;

import com.nsclient.config.ConfigManager;
import com.nsclient.gui.ClickGui;
import com.nsclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

/**
 * NS Client - client-side PvP visuals mod for the Holyworld server.
 * No combat automation of any kind lives here: everything below is purely
 * cosmetic/informational (HUD, hit feedback, ClickGUI).
 */
public class NSClientClient implements ClientModInitializer {

    public static ModuleManager MODULES;
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        MODULES = new ModuleManager();
        ConfigManager.load(MODULES);

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nsclient.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.nsclient"
        ));

        HudRenderCallback.EVENT.register((context, tickCounter) ->
                MODULES.renderHud(context, tickCounter.getTickDelta(true)));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MODULES.tick(client);
            MODULES.handleKeybinds(client);
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGui());
                }
            }
        });

        // Visual-only: triggers hitmarker/combo/kill-effect feedback. Does not
        // modify, cancel, or repeat the attack in any way.
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            MODULES.getCombatTracker().onHit(entity);
            return ActionResult.PASS;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ConfigManager.save(MODULES));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ConfigManager.save(MODULES)));
    }
}
