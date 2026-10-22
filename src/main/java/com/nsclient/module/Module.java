package com.nsclient.module;

import com.google.gson.JsonObject;
import com.nsclient.module.setting.Setting;

import java.util.ArrayList;
import java.util.List;

/** Base class every feature of the client extends: HUD elements, combat visuals, etc. */
public abstract class Module {

    /** Which GUI sub-tab a module shows under inside the "Visuals" category (Eclipse-style Player/World split). */
    public enum Area { PLAYER, WORLD }

    private final String name;
    private final String displayName;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int keyCode = -1; // -1 = unbound
    private Area area = Area.PLAYER;
    private final List<Setting<?>> settings = new ArrayList<>();

    protected Module(String name, String displayName, String description, Category category, boolean enabledByDefault) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.enabled = enabledByDefault;
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Area getArea() { return area; }
    protected void setArea(Area area) { this.area = area; }
    public boolean isEnabled() { return enabled; }
    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }
    public List<Setting<?>> getSettings() { return settings; }

    protected <T, S extends Setting<T>> S register(S setting) {
        settings.add(setting);
        return setting;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void toggle() { setEnabled(!enabled); }

    protected void onEnable() {}
    protected void onDisable() {}

    public void writeConfig(JsonObject obj) {
        obj.addProperty("enabled", enabled);
        obj.addProperty("keyCode", keyCode);
        JsonObject settingsObj = new JsonObject();
        for (Setting<?> setting : settings) {
            settingsObj.add(setting.getName(), setting.toJson());
        }
        obj.add("settings", settingsObj);
    }

    public void readConfig(JsonObject obj) {
        if (obj.has("enabled")) this.enabled = obj.get("enabled").getAsBoolean();
        if (obj.has("keyCode")) this.keyCode = obj.get("keyCode").getAsInt();
        if (obj.has("settings")) {
            JsonObject settingsObj = obj.getAsJsonObject("settings");
            for (Setting<?> setting : settings) {
                if (settingsObj.has(setting.getName())) {
                    setting.fromJson(settingsObj.get(setting.getName()));
                }
            }
        }
    }
}
