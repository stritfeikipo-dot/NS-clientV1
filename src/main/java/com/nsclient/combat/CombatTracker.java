package com.nsclient.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks client-side hit/kill state purely for visual feedback:
 * hitmarker flash timing, combo counting, and kill-effect particle triggers.
 * This never alters or repeats any attack - it only observes AttackEntityCallback.
 */
public class CombatTracker {
    private static final long COMBO_TIMEOUT_MS = 2500;

    private int currentTargetId = -1;
    private int combo = 0;
    private long lastComboHitTime = 0;
    private long lastHitTimestamp = -1;

    private final Map<Integer, Vec3d> lastKnownPositions = new HashMap<>();
    private final Map<Integer, Boolean> wasAlive = new HashMap<>();
    private Vec3d pendingDeathEffect = null;

    public void onHit(Entity target) {
        if (target == null) return;
        long now = System.currentTimeMillis();
        lastHitTimestamp = now;

        if (target.getId() == currentTargetId && now - lastComboHitTime < COMBO_TIMEOUT_MS) {
            combo++;
        } else {
            currentTargetId = target.getId();
            combo = 1;
        }
        lastComboHitTime = now;

        if (target instanceof LivingEntity living) {
            lastKnownPositions.put(target.getId(), living.getPos());
            wasAlive.put(target.getId(), living.isAlive());
        }
    }

    public void tick(MinecraftClient client) {
        if (combo != 0 && System.currentTimeMillis() - lastComboHitTime > COMBO_TIMEOUT_MS) {
            combo = 0;
            currentTargetId = -1;
        }

        if (client.world == null) return;
        for (Map.Entry<Integer, Boolean> entry : new HashMap<>(wasAlive).entrySet()) {
            int id = entry.getKey();
            Entity entity = client.world.getEntityById(id);
            boolean wasAliveBefore = entry.getValue();
            boolean isAliveNow = entity instanceof LivingEntity living && living.isAlive();

            if (wasAliveBefore && !isAliveNow) {
                Vec3d pos = lastKnownPositions.get(id);
                if (pos != null) pendingDeathEffect = pos;
                wasAlive.remove(id);
                lastKnownPositions.remove(id);
            } else if (entity instanceof LivingEntity living) {
                lastKnownPositions.put(id, living.getPos());
                wasAlive.put(id, living.isAlive());
            }
        }
    }

    /** Returns and clears a pending death location for KillEffectsModule to consume (null if none). */
    public Vec3d pollDeathEffect() {
        Vec3d result = pendingDeathEffect;
        pendingDeathEffect = null;
        return result;
    }

    public int getCombo() { return combo; }
    public long getLastHitTimestamp() { return lastHitTimestamp; }
}
