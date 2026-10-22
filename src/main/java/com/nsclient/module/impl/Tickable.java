package com.nsclient.module.impl;

import net.minecraft.client.MinecraftClient;

/** Implemented by modules that need per-client-tick logic (e.g. tracking clicks for CPS). */
public interface Tickable {
    void onTick(MinecraftClient client);
}
