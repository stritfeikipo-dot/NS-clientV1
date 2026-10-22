package com.nsclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public JsonElement toJson() { return new JsonPrimitive(getValue()); }

    @Override
    public void fromJson(JsonElement element) { setValue(element.getAsBoolean()); }
}
