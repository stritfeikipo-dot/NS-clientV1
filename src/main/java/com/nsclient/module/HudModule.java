package com.nsclient.module;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;

/** A Module that draws a draggable element on screen (position/scale persisted + editable in the ClickGUI). */
public abstract class HudModule extends Module {
    private double x;
    private double y;
    private double scale = 1.0;
    private boolean grouped = false; // true while merged into a HudGroup pill; its own renderHud is skipped

    protected HudModule(String name, String displayName, String description, Category category, boolean enabledByDefault,
                         double defaultX, double defaultY) {
        super(name, displayName, description, category, enabledByDefault);
        this.x = defaultX;
        this.y = defaultY;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = Math.max(0.5, Math.min(3.0, scale)); }
    public boolean isGrouped() { return grouped; }
    public void setGrouped(boolean grouped) { this.grouped = grouped; }

    /** Approximate footprint at scale 1.0, used for drag hit-testing and the GUI preview box. */
    public abstract int getPreviewWidth();
    public abstract int getPreviewHeight();

    public abstract void renderHud(DrawContext context, float tickDelta);

    @Override
    public void writeConfig(JsonObject obj) {
        super.writeConfig(obj);
        obj.addProperty("x", x);
        obj.addProperty("y", y);
        obj.addProperty("scale", scale);
    }

    @Override
    public void readConfig(JsonObject obj) {
        super.readConfig(obj);
        if (obj.has("x")) x = obj.get("x").getAsDouble();
        if (obj.has("y")) y = obj.get("y").getAsDouble();
        if (obj.has("scale")) scale = obj.get("scale").getAsDouble();
    }
}
