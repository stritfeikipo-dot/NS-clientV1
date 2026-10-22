package com.nsclient.hud;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nsclient.module.HudModule;
import com.nsclient.module.Module;
import com.nsclient.module.ModuleManager;
import com.nsclient.module.TextStat;
import com.nsclient.util.IconRenderer;
import com.nsclient.util.UIRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns every HudGroup (merged HUD pill) and the merge/split logic for dragging
 * TextStat-capable HudModules together or pulling a member back out on its own.
 * Rendered as a "liquid glass" pill: translucent + soft top sheen (see UIRenderUtil.fillGlass),
 * each member prefixed with a small icon, Soup-style.
 */
public class HudGroupManager {
    private static final int DIVIDER_COLOR = 0xFF6E6382;
    private static final int PILL_BG = 0x8C1B1026;
    private static final int PAD = 6;
    private static final int ROW_H = 14;
    private static final int ICON_SIZE = 8;
    private static final int ICON_GAP = 3;

    private static IconRenderer.Icon iconFor(String moduleName) {
        return switch (moduleName) {
            case "watermark_hud" -> IconRenderer.Icon.TAG;
            case "fps_hud" -> IconRenderer.Icon.FPS;
            case "cps_hud" -> IconRenderer.Icon.CPS;
            case "ping_hud" -> IconRenderer.Icon.PING;
            case "coords_hud" -> IconRenderer.Icon.PIN;
            default -> null;
        };
    }

    private final List<HudGroup> groups = new ArrayList<>();

    public List<HudGroup> getGroups() { return groups; }

    public HudGroup getGroupOf(String moduleName) {
        for (HudGroup g : groups) if (g.getMembers().contains(moduleName)) return g;
        return null;
    }

    /** Merges `draggedName` into whatever `targetName` belongs to (creating a new group if target is standalone). */
    public void merge(ModuleManager mm, String draggedName, String targetName) {
        if (draggedName.equals(targetName)) return;
        HudGroup existing = getGroupOf(targetName);
        if (existing != null) {
            if (!existing.getMembers().contains(draggedName)) existing.getMembers().add(draggedName);
        } else {
            HudGroup g = new HudGroup();
            if (mm.getByName(targetName) instanceof HudModule hm) {
                g.setX(hm.getX());
                g.setY(hm.getY());
            }
            g.getMembers().add(targetName);
            g.getMembers().add(draggedName);
            groups.add(g);
            setModuleGrouped(mm, targetName, true);
        }
        setModuleGrouped(mm, draggedName, true);
    }

    /** Pulls `moduleName` out of its group and makes it a free-floating HudModule again just below the group. */
    public void splitOut(ModuleManager mm, String moduleName) {
        HudGroup g = getGroupOf(moduleName);
        if (g == null) return;
        g.getMembers().remove(moduleName);
        setModuleGrouped(mm, moduleName, false);
        placeStandaloneNear(mm, moduleName, g.getX(), g.getY() + g.getLastHeight() + 4);

        if (g.getMembers().size() <= 1) {
            for (String remaining : new ArrayList<>(g.getMembers())) {
                setModuleGrouped(mm, remaining, false);
                placeStandaloneNear(mm, remaining, g.getX(), g.getY());
            }
            groups.remove(g);
        }
    }

    private void placeStandaloneNear(ModuleManager mm, String name, double x, double y) {
        if (mm.getByName(name) instanceof HudModule hm) {
            hm.setX(x);
            hm.setY(y);
        }
    }

    private void setModuleGrouped(ModuleManager mm, String name, boolean grouped) {
        if (mm.getByName(name) instanceof HudModule hm) hm.setGrouped(grouped);
    }

    /** Re-applies setGrouped(true) to every member after config load (groups themselves are loaded separately). */
    public void reapplyGroupedFlags(ModuleManager mm) {
        for (HudGroup g : groups) {
            for (String name : g.getMembers()) setModuleGrouped(mm, name, true);
        }
    }

    public void render(DrawContext context, ModuleManager mm) {
        MinecraftClient client = MinecraftClient.getInstance();
        for (HudGroup g : groups) {
            List<String> texts = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            List<String> names = new ArrayList<>();
            List<IconRenderer.Icon> icons = new ArrayList<>();

            for (String name : g.getMembers()) {
                Module m = mm.getByName(name);
                if (m == null || !m.isEnabled() || !(m instanceof TextStat stat)) continue;
                texts.add(stat.getStatText());
                colors.add(g.isUseGroupColor() ? g.getGroupColor() : stat.getStatColor());
                names.add(name);
                icons.add(iconFor(name));
            }

            if (texts.isEmpty()) {
                g.setLastWidth(0);
                g.setLastHeight(0);
                g.setLastBounds(new ArrayList<>());
                continue;
            }

            int dividerW = client.textRenderer.getWidth(" | ");
            List<Integer> segStartX = new ArrayList<>(); // start of the icon (or text, if no icon)
            List<Integer> textStartX = new ArrayList<>(); // start of the text itself, after its icon
            int cursor = PAD;
            for (int i = 0; i < texts.size(); i++) {
                segStartX.add(cursor);
                if (icons.get(i) != null) cursor += ICON_SIZE + ICON_GAP;
                textStartX.add(cursor);
                cursor += client.textRenderer.getWidth(texts.get(i));
                if (i < texts.size() - 1) cursor += dividerW;
            }
            int totalW = cursor + PAD;

            double scale = g.getScale();
            context.getMatrices().push();
            context.getMatrices().translate(g.getX(), g.getY(), 0);
            context.getMatrices().scale((float) scale, (float) scale, 1f);

            UIRenderUtil.fillGlass(context, 0, 0, totalW, ROW_H, 4, PILL_BG);
            List<HudGroup.MemberBounds> bounds = new ArrayList<>();
            for (int i = 0; i < texts.size(); i++) {
                IconRenderer.Icon icon = icons.get(i);
                if (icon != null) {
                    IconRenderer.draw(context, icon, segStartX.get(i), (ROW_H - ICON_SIZE) / 2, ICON_SIZE, colors.get(i));
                }
                int tx = textStartX.get(i);
                context.drawTextWithShadow(client.textRenderer, texts.get(i), tx, 3, colors.get(i));
                int tw = client.textRenderer.getWidth(texts.get(i));
                bounds.add(new HudGroup.MemberBounds(names.get(i),
                        g.getX() + segStartX.get(i) * scale, g.getY(), g.getX() + (tx + tw) * scale, g.getY() + ROW_H * scale));
                if (i < texts.size() - 1) {
                    context.drawTextWithShadow(client.textRenderer, "|", tx + tw + dividerW / 2 - 1, 3, DIVIDER_COLOR);
                }
            }
            context.getMatrices().pop();

            g.setLastWidth(totalW * scale);
            g.setLastHeight(ROW_H * scale);
            g.setLastBounds(bounds);
        }
    }

    // ---------------------------------------------------------------- persistence

    public JsonArray toJson() {
        JsonArray arr = new JsonArray();
        for (HudGroup g : groups) {
            JsonObject obj = new JsonObject();
            JsonArray members = new JsonArray();
            for (String m : g.getMembers()) members.add(m);
            obj.add("members", members);
            obj.addProperty("x", g.getX());
            obj.addProperty("y", g.getY());
            obj.addProperty("scale", g.getScale());
            obj.addProperty("useGroupColor", g.isUseGroupColor());
            obj.addProperty("groupColor", g.getGroupColor());
            arr.add(obj);
        }
        return arr;
    }

    public void fromJson(JsonArray arr, ModuleManager mm) {
        groups.clear();
        for (var el : arr) {
            JsonObject obj = el.getAsJsonObject();
            HudGroup g = new HudGroup();
            if (obj.has("members")) {
                for (var m : obj.getAsJsonArray("members")) g.getMembers().add(m.getAsString());
            }
            if (obj.has("x")) g.setX(obj.get("x").getAsDouble());
            if (obj.has("y")) g.setY(obj.get("y").getAsDouble());
            if (obj.has("scale")) g.setScale(obj.get("scale").getAsDouble());
            if (obj.has("useGroupColor")) g.setUseGroupColor(obj.get("useGroupColor").getAsBoolean());
            if (obj.has("groupColor")) g.setGroupColor(obj.get("groupColor").getAsInt());
            groups.add(g);
        }
        reapplyGroupedFlags(mm);
    }
}
