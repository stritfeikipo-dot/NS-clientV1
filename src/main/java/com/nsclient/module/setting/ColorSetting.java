package com.nsclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Stores an ARGB packed color (0xAARRGGBB). */
public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, int argbDefault) {
        super(name, argbDefault);
    }

    @Override
    public JsonElement toJson() { return new JsonPrimitive(getValue()); }

    @Override
    public void fromJson(JsonElement element) { setValue(element.getAsInt()); }
}
