package com.nsclient.module.impl;

import com.google.gson.JsonObject;
import com.nsclient.NSClientClient;
import com.nsclient.module.Category;
import com.nsclient.module.Module;
import com.nsclient.module.setting.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class KillEffectsModule extends Module implements Tickable {
    private final SliderSetting particleCount = register(new SliderSetting("particle_count", 24.0, 6.0, 64.0, 2.0));
    private final Random random = new Random();

    public enum Style { BURST, RING, UPWARD }
    private Style style = Style.BURST;

    public KillEffectsModule() {
        super("kill_effects", "Kill Effects", "Particle burst where a target dies", Category.COMBAT, true);
        setArea(Area.WORLD);
    }

    public Style getStyle() { return style; }
    public void cycleStyle() { style = Style.values()[(style.ordinal() + 1) % Style.values().length]; }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled()) return;
        Vec3d pos = NSClientClient.MODULES.getCombatTracker().pollDeathEffect();
        if (pos == null || client.world == null) return;

        int count = particleCount.getValue().intValue();
        switch (style) {
            case BURST -> spawnBurst(client, pos, count);
            case RING -> spawnRing(client, pos, count);
            case UPWARD -> spawnUpward(client, pos, count);
        }
    }

    private void spawnBurst(MinecraftClient client, Vec3d pos, int count) {
        for (int i = 0; i < count; i++) {
            double dx = (random.nextDouble() - 0.5) * 0.6;
            double dy = random.nextDouble() * 0.6;
            double dz = (random.nextDouble() - 0.5) * 0.6;
            client.world.addParticle(ParticleTypes.WITCH, pos.x, pos.y + 1, pos.z, dx, dy, dz);
            client.world.addParticle(ParticleTypes.CRIT, pos.x, pos.y + 1, pos.z, dx, dy, dz);
        }
    }

    private void spawnRing(MinecraftClient client, Vec3d pos, int count) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double dx = Math.cos(angle) * 0.4;
            double dz = Math.sin(angle) * 0.4;
            client.world.addParticle(ParticleTypes.CRIT, pos.x, pos.y + 1, pos.z, dx, 0.1, dz);
            client.world.addParticle(ParticleTypes.WITCH, pos.x + dx * 0.5, pos.y + 1, pos.z + dz * 0.5, 0, 0.05, 0);
        }
    }

    private void spawnUpward(MinecraftClient client, Vec3d pos, int count) {
        for (int i = 0; i < count; i++) {
            double dx = (random.nextDouble() - 0.5) * 0.2;
            double dz = (random.nextDouble() - 0.5) * 0.2;
            double dy = 0.3 + random.nextDouble() * 0.4;
            client.world.addParticle(ParticleTypes.CRIT, pos.x, pos.y, pos.z, dx, dy, dz);
            client.world.addParticle(ParticleTypes.WITCH, pos.x, pos.y, pos.z, dx * 0.5, dy * 0.8, dz * 0.5);
        }
    }

    @Override
    public void writeConfig(JsonObject obj) {
        super.writeConfig(obj);
        obj.addProperty("style", style.name());
    }

    @Override
    public void readConfig(JsonObject obj) {
        super.readConfig(obj);
        if (obj.has("style")) {
            try { style = Style.valueOf(obj.get("style").getAsString()); } catch (IllegalArgumentException ignored) {}
        }
    }
}
