package com.nsclient.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-screen exponential-smoothing animator: call beginFrame() once per render(),
 * then animate(key, target, speed) for every value you want to ease toward a target
 * instead of snapping (toggle knobs, hover highlights, expand/collapse reveals, etc).
 * Higher "speed" = snappier; ~8-14 feels good for UI.
 */
public class Animator {
    private final Map<String, Float> values = new HashMap<>();
    private long lastNanos = -1;
    private float dt;

    public void beginFrame() {
        long now = System.nanoTime();
        if (lastNanos < 0) lastNanos = now;
        dt = Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;
    }

    public float animate(String key, float target, float speed) {
        float current = values.getOrDefault(key, target);
        float factor = 1f - (float) Math.exp(-speed * dt);
        float next = current + (target - current) * factor;
        if (Math.abs(next - target) < 0.0015f) next = target;
        values.put(key, next);
        return next;
    }

    public float peek(String key, float fallback) {
        return values.getOrDefault(key, fallback);
    }
}
