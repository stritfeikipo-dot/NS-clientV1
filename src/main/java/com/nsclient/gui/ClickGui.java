package com.nsclient.gui;

import com.nsclient.NSClientClient;
import com.nsclient.config.ConfigManager;
import com.nsclient.module.Category;
import com.nsclient.module.HudModule;
import com.nsclient.module.Module;
import com.nsclient.module.setting.BooleanSetting;
import com.nsclient.module.setting.ColorSetting;
import com.nsclient.module.setting.Setting;
import com.nsclient.module.setting.SliderSetting;
import com.nsclient.module.impl.CrosshairModule;
import com.nsclient.module.impl.HitmarkerModule;
import com.nsclient.module.impl.KillEffectsModule;
import com.nsclient.module.TextStat;
import com.nsclient.hud.HudGroup;
import com.nsclient.hud.HudGroupManager;
import com.nsclient.util.Animator;
import com.nsclient.util.IconRenderer;
import com.nsclient.util.UIRenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * NS Client ClickGUI v2 - Eclipse-style layout: top search bar, icon sidebar with
 * "Visuals / HUD / Misc" module categories plus a GENERAL section (Themes, HUD editor,
 * Socials, Keybinds, Configs), Player/World sub-tabs inside Visuals, minimalist
 * ring-to-pill animated toggles, rounded cards throughout.
 */
public class ClickGui extends Screen {

    private enum Section { VISUALS, HUD, MISC, THEMES, HUD_EDIT, SOCIALS, KEYBINDS, CONFIGS }

    private record NavItem(Section section, String label, IconRenderer.Icon icon) {}

    private static final NavItem[] NAV_MODULES = {
            new NavItem(Section.VISUALS, "Visuals", IconRenderer.Icon.VISUALS),
            new NavItem(Section.HUD, "HUD", IconRenderer.Icon.HUD),
            new NavItem(Section.MISC, "Misc", IconRenderer.Icon.MISC),
    };
    private static final NavItem[] NAV_GENERAL = {
            new NavItem(Section.THEMES, "Themes", IconRenderer.Icon.THEMES),
            new NavItem(Section.HUD_EDIT, "HUD", IconRenderer.Icon.HUD_EDIT),
            new NavItem(Section.SOCIALS, "Socials", IconRenderer.Icon.SOCIALS),
            new NavItem(Section.KEYBINDS, "Keybinds", IconRenderer.Icon.KEYBINDS),
            new NavItem(Section.CONFIGS, "Configs", IconRenderer.Icon.CONFIGS),
    };

    private static final int[] THEME_SWATCHES = {
            0xFFB388FF, 0xFF4DA6FF, 0xFFFF5C5C, 0xFF4DFF88, 0xFFFFA94D, 0xFF4DFFE0, 0xFFFF4DD8,
    };

    // Edit these to point at your own community links.
    private static final String SOCIAL_DISCORD = "discord.gg/your-invite";
    private static final String SOCIAL_GITHUB = "github.com/your-org/NS-Client";

    private static final int COL_BG = 0xE60C0712;
    private static final int COL_SIDEBAR = 0xCC140B1E;
    private static final int COL_PANEL = 0xCC1B1026;
    private static final int COL_PANEL_ALT = 0xFF221433;
    private static final int COL_ACCENT = 0xFFB388FF;
    private static final int COL_ACCENT_DIM = 0xFF6E3FCE;
    private static final int COL_TEXT = 0xFFEDE6F7;
    private static final int COL_TEXT_DIM = 0xFF9A8CB5;

    private static final int CARD_RADIUS = 8;
    private static final int ROW_RADIUS = 5;

    private static final int SIDEBAR_W = 120;
    private static final int PANEL_X = 140;
    private static final int PANEL_W = 260;
    private static final int TOP_BAR_Y = 8;
    private static final int CONTENT_Y = 40;
    private static final int NAV_ROW_H = 24;
    private static final int MODULE_ROW_H = 30;
    private static final int SETTING_SLIDER_H = 21;
    private static final int SETTING_BOOL_H = 16;
    private static final int SETTING_COLOR_H = 14;
    private static final int SEARCH_BOX_W = 150;

    private final Animator anim = new Animator();
    private long openedAtNanos = -1;

    private Section section = Section.VISUALS;
    private Module.Area selectedArea = Module.Area.PLAYER;
    private String search = "";
    private boolean searchFocused = false;

    private Module expanded = null;
    private Module bindingModule = null;
    private SliderSetting draggingSlider = null;
    private HudModule draggingHud = null;
    private double dragOffsetX, dragOffsetY;
    private HudGroup draggingGroup = null;
    private double groupDragOffsetX, groupDragOffsetY;
    private HudGroup selectedGroup = null;

    private String configNameBuffer = "";
    private boolean configNameFocused = false;
    private List<String> profileCache = new ArrayList<>();
    private boolean profileCacheDirty = true;

    public ClickGui() {
        super(Text.literal("NS Client"));
    }

    @Override
    protected void init() {
        super.init();
        openedAtNanos = System.nanoTime();
    }

    private float openProgress() {
        if (openedAtNanos < 0) return 1f;
        float elapsed = (System.nanoTime() - openedAtNanos) / 1_000_000_000f;
        return UIRenderUtil.easeOutCubic(Math.min(1f, elapsed / 0.22f));
    }

    // ================================================================== render

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        anim.beginFrame();
        float t = openProgress();

        int bgAlpha = (int) (0xE6 * t);
        context.fill(0, 0, this.width, this.height, (bgAlpha << 24) | (COL_BG & 0xFFFFFF));

        renderLivePreview(context);

        context.getMatrices().push();
        context.getMatrices().translate((1 - t) * -36, 0, 0);
        renderTopBar(context, mouseX, mouseY, t);
        renderSidebar(context, mouseX, mouseY, t);
        context.getMatrices().pop();

        context.getMatrices().push();
        context.getMatrices().translate(0, (1 - t) * -24, 0);
        renderContent(context, mouseX, mouseY, t);
        context.getMatrices().pop();

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderTopBar(DrawContext context, int mouseX, int mouseY, float t) {
        context.drawTextWithShadow(this.textRenderer, "NS CLIENT", 10, TOP_BAR_Y + 4, withAlpha(COL_ACCENT, t));

        int boxX = PANEL_X + PANEL_W - SEARCH_BOX_W;
        boolean hovered = isInside(mouseX, mouseY, boxX, TOP_BAR_Y, SEARCH_BOX_W, 18);
        int border = searchFocused ? COL_ACCENT : (hovered ? COL_ACCENT_DIM : 0x00000000);
        if (border != 0) UIRenderUtil.fillRounded(context, boxX - 1, TOP_BAR_Y - 1, boxX + SEARCH_BOX_W + 1, TOP_BAR_Y + 19, 5, withAlpha(border, t));
        UIRenderUtil.fillRounded(context, boxX, TOP_BAR_Y, boxX + SEARCH_BOX_W, TOP_BAR_Y + 18, 4, withAlpha(COL_PANEL_ALT, t));
        IconRenderer.draw(context, IconRenderer.Icon.SEARCH, boxX + 6, TOP_BAR_Y + 4, 10, withAlpha(COL_TEXT_DIM, t));
        String shown = search.isEmpty() ? "Search modules" : search;
        context.drawTextWithShadow(this.textRenderer, shown, boxX + 20, TOP_BAR_Y + 5, withAlpha(search.isEmpty() ? COL_TEXT_DIM : COL_TEXT, t));
    }

    private void renderSidebar(DrawContext context, int mouseX, int mouseY, float t) {
        UIRenderUtil.fillGlass(context, 10, CONTENT_Y, 10 + SIDEBAR_W, this.height - 10, CARD_RADIUS, withAlpha(COL_SIDEBAR, t));

        int y = CONTENT_Y + 10;
        for (NavItem item : NAV_MODULES) {
            y = renderNavRow(context, item, y, mouseX, mouseY, t);
        }
        y += 8;
        context.drawTextWithShadow(this.textRenderer, "GENERAL", 20, y, withAlpha(0xFF5E5272, t));
        y += 14;
        for (NavItem item : NAV_GENERAL) {
            y = renderNavRow(context, item, y, mouseX, mouseY, t);
        }
    }

    private int renderNavRow(DrawContext context, NavItem item, int y, int mouseX, int mouseY, float t) {
        boolean selected = item.section() == section;
        boolean hovered = isInside(mouseX, mouseY, 10, y, SIDEBAR_W, NAV_ROW_H);
        float hoverT = anim.animate("nav_hover_" + item.label() + item.section(), (selected || hovered) ? 1f : 0f, 12f);

        if (hoverT > 0.01f) {
            int bg = selected
                    ? UIRenderUtil.lerpArgb(0x00000000, COL_PANEL_ALT, hoverT)
                    : UIRenderUtil.lerpArgb(0x00000000, 0x33FFFFFF, hoverT);
            UIRenderUtil.fillRounded(context, 10, y, 10 + SIDEBAR_W, y + NAV_ROW_H, ROW_RADIUS, withAlpha(bg, t));
        }
        if (selected) context.fill(10, y + 4, 13, y + NAV_ROW_H - 4, withAlpha(COL_ACCENT, t));

        int iconColor = UIRenderUtil.lerpArgb(COL_TEXT_DIM, COL_ACCENT, selected ? 1f : hoverT * 0.4f);
        IconRenderer.draw(context, item.icon(), 18, y + (NAV_ROW_H - 10) / 2, 10, withAlpha(iconColor, t));
        context.drawTextWithShadow(this.textRenderer, item.label(), 34, y + 8, withAlpha(iconColor, t));

        return y + NAV_ROW_H + 2;
    }

    private void renderContent(DrawContext context, int mouseX, int mouseY, float t) {
        switch (section) {
            case VISUALS, HUD, MISC -> renderModulesSection(context, mouseX, mouseY, t);
            case THEMES -> renderThemesSection(context, mouseX, mouseY, t);
            case HUD_EDIT -> renderHudEditSection(context, mouseX, mouseY, t);
            case SOCIALS -> renderSocialsSection(context, t);
            case KEYBINDS -> renderKeybindsSection(context, mouseX, mouseY, t);
            case CONFIGS -> renderConfigsSection(context, mouseX, mouseY, t);
        }
    }

    // ---------------------------------------------------------------- modules section

    private Category sectionCategory() {
        return switch (section) {
            case VISUALS -> Category.COMBAT;
            case HUD -> Category.HUD;
            case MISC -> Category.MISC;
            default -> null;
        };
    }

    private List<Module> getVisibleModules() {
        List<Module> result = new ArrayList<>();
        Category cat = sectionCategory();
        for (Module m : NSClientClient.MODULES.getModules()) {
            if (m.getCategory() != cat) continue;
            if (section == Section.VISUALS && m.getArea() != selectedArea) continue;
            if (!search.isEmpty() && !m.getDisplayName().toLowerCase().contains(search.toLowerCase())) continue;
            result.add(m);
        }
        return result;
    }

    private void renderModulesSection(DrawContext context, int mouseX, int mouseY, float t) {
        UIRenderUtil.fillGlass(context, PANEL_X, CONTENT_Y, PANEL_X + PANEL_W, this.height - 10, CARD_RADIUS, withAlpha(COL_PANEL, t));

        int y = CONTENT_Y + 8;
        if (section == Section.VISUALS) {
            y = renderAreaTabs(context, mouseX, mouseY, y, t);
        }

        List<Module> modules = getVisibleModules();
        if (modules.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, "No modules here yet.", PANEL_X + 14, y + 4, withAlpha(COL_TEXT_DIM, t));
            return;
        }

        for (Module m : modules) {
            boolean hovered = isInside(mouseX, mouseY, PANEL_X + 6, y, PANEL_W - 12, MODULE_ROW_H);
            float hoverT = anim.animate("row_hover_" + m.getName(), hovered ? 1f : 0f, 14f);
            if (hoverT > 0.01f) {
                UIRenderUtil.fillRounded(context, PANEL_X + 6, y, PANEL_X + PANEL_W - 6, y + MODULE_ROW_H, ROW_RADIUS,
                        withAlpha(UIRenderUtil.lerpArgb(0x00000000, COL_PANEL_ALT, hoverT), t));
            }

            context.drawTextWithShadow(this.textRenderer, m.getDisplayName(), PANEL_X + 14, y + 6,
                    withAlpha(m.isEnabled() ? COL_TEXT : COL_TEXT_DIM, t));
            context.drawTextWithShadow(this.textRenderer, m.getDescription(), PANEL_X + 14, y + 17,
                    withAlpha(0xFF6E6382, t));

            float toggleT = anim.animate("toggle_" + m.getName(), m.isEnabled() ? 1f : 0f, 16f);
            drawToggle(context, PANEL_X + PANEL_W - 40, y + 8, toggleT, t);

            y += MODULE_ROW_H + 2;

            float expandT = anim.animate("expand_" + m.getName(), expanded == m ? 1f : 0f, 10f);
            if (expandT > 0.01f) {
                y = renderModuleSettings(context, m, y, expandT, t);
            }
        }
    }

    private int renderAreaTabs(DrawContext context, int mouseX, int mouseY, int y, float t) {
        int tabW = 90;
        int gap = 8;
        int startX = PANEL_X + 10;
        Module.Area[] areas = { Module.Area.PLAYER, Module.Area.WORLD };
        IconRenderer.Icon[] icons = { IconRenderer.Icon.PLAYER, IconRenderer.Icon.WORLD };
        for (int i = 0; i < areas.length; i++) {
            int x = startX + i * (tabW + gap);
            boolean selected = selectedArea == areas[i];
            boolean hovered = isInside(mouseX, mouseY, x, y, tabW, 16);
            float hoverT = anim.animate("area_hover_" + areas[i], (selected || hovered) ? 1f : 0f, 14f);
            int bg = selected ? COL_ACCENT_DIM : UIRenderUtil.lerpArgb(0x00000000, COL_PANEL_ALT, hoverT);
            UIRenderUtil.fillRounded(context, x, y, x + tabW, y + 16, 8, withAlpha(bg, t));
            IconRenderer.draw(context, icons[i], x + 8, y + 3, 10, withAlpha(selected ? COL_TEXT : COL_TEXT_DIM, t));
            context.drawTextWithShadow(this.textRenderer, areas[i] == Module.Area.PLAYER ? "Player" : "World",
                    x + 22, y + 4, withAlpha(selected ? COL_TEXT : COL_TEXT_DIM, t));
        }
        return y + 16 + 10;
    }

    private int renderModuleSettings(DrawContext context, Module m, int y, float revealT, float globalAlpha) {
        float alpha = revealT * globalAlpha;
        int slideY = (int) ((1 - revealT) * -6);

        context.fill(PANEL_X + 10, y + slideY, PANEL_X + PANEL_W - 10, y + 2 + slideY, withAlpha(COL_ACCENT_DIM, alpha));
        y += 6;

        for (Setting<?> setting : m.getSettings()) {
            if (setting instanceof SliderSetting slider) {
                context.drawTextWithShadow(this.textRenderer,
                        slider.getName() + ": " + String.format("%.2f", slider.getValue()),
                        PANEL_X + 14, y + slideY, withAlpha(COL_TEXT_DIM, alpha));
                y += 11;
                int barX = PANEL_X + 14;
                int barW = PANEL_W - 40;
                UIRenderUtil.fillRounded(context, barX, y + slideY, barX + barW, y + 4 + slideY, 2, withAlpha(COL_PANEL_ALT, alpha));
                double pct = (slider.getValue() - slider.getMin()) / (slider.getMax() - slider.getMin());
                int fillW = (int) (barW * clamp01(pct));
                if (fillW > 0) {
                    UIRenderUtil.fillRounded(context, barX, y + slideY, barX + fillW, y + 4 + slideY, 2, withAlpha(COL_ACCENT, alpha));
                }
                y += 10;
            } else if (setting instanceof ColorSetting color) {
                context.drawTextWithShadow(this.textRenderer, color.getName(), PANEL_X + 14, y + slideY, withAlpha(COL_TEXT_DIM, alpha));
                UIRenderUtil.fillRounded(context, PANEL_X + PANEL_W - 34, y - 2 + slideY, PANEL_X + PANEL_W - 14, y + 10 + slideY, 3,
                        withAlpha(color.getValue() | 0xFF000000, alpha));
                y += SETTING_COLOR_H;
            } else if (setting instanceof BooleanSetting bool) {
                float boolT = anim.animate("bool_" + m.getName() + "_" + bool.getName(), bool.getValue() ? 1f : 0f, 16f);
                drawToggle(context, PANEL_X + PANEL_W - 40, y + slideY, boolT, alpha);
                context.drawTextWithShadow(this.textRenderer, bool.getName(), PANEL_X + 14, y + 2 + slideY, withAlpha(COL_TEXT_DIM, alpha));
                y += SETTING_BOOL_H;
            }
        }

        String modeLabel = modeRowLabel(m);
        if (modeLabel != null) {
            context.drawTextWithShadow(this.textRenderer, modeLabel + "  >", PANEL_X + 14, y + slideY, withAlpha(COL_ACCENT, alpha));
            y += SETTING_BOOL_H;
        }

        return y + 6;
    }

    private static String modeRowLabel(Module m) {
        if (m instanceof CrosshairModule ch) return "Shape: " + ch.getShape().name();
        if (m instanceof HitmarkerModule hm) return "Style: " + hm.getStyle().name();
        if (m instanceof KillEffectsModule ke) return "Style: " + ke.getStyle().name();
        return null;
    }

    private static void cycleMode(Module m) {
        if (m instanceof CrosshairModule ch) ch.cycleShape();
        else if (m instanceof HitmarkerModule hm) hm.cycleStyle();
        else if (m instanceof KillEffectsModule ke) ke.cycleStyle();
    }

    // ---------------------------------------------------------------- themes section

    private void renderThemesSection(DrawContext context, int mouseX, int mouseY, float t) {
        UIRenderUtil.fillGlass(context, PANEL_X, CONTENT_Y, PANEL_X + PANEL_W, this.height - 10, CARD_RADIUS, withAlpha(COL_PANEL, t));
        context.drawTextWithShadow(this.textRenderer, "Accent Theme", PANEL_X + 14, CONTENT_Y + 10, withAlpha(COL_TEXT, t));
        context.drawTextWithShadow(this.textRenderer, "Applies to every module's color setting", PANEL_X + 14, CONTENT_Y + 22, withAlpha(COL_TEXT_DIM, t));

        int size = 22;
        int gap = 8;
        int startX = PANEL_X + 14;
        int rowY = CONTENT_Y + 38;
        for (int i = 0; i < THEME_SWATCHES.length; i++) {
            int x = startX + i * (size + gap);
            boolean hovered = isInside(mouseX, mouseY, x, rowY, size, size);
            float hoverT = anim.animate("swatch_hover_" + i, hovered ? 1f : 0f, 16f);
            int ringPad = (int) (hoverT * 3);
            if (ringPad > 0) {
                UIRenderUtil.fillRounded(context, x - ringPad, rowY - ringPad, x + size + ringPad, rowY + size + ringPad, 6, withAlpha(COL_ACCENT_DIM, t));
            }
            UIRenderUtil.fillRounded(context, x, rowY, x + size, rowY + size, 5, withAlpha(THEME_SWATCHES[i] | 0xFF000000, t));
        }
    }

    private void applyTheme(int rgbOpaque) {
        for (Module m : NSClientClient.MODULES.getModules()) {
            for (Setting<?> setting : m.getSettings()) {
                if (setting instanceof ColorSetting color) {
                    color.setValue(rgbOpaque);
                }
            }
        }
    }

    // ---------------------------------------------------------------- HUD editor section

    private void renderHudEditSection(DrawContext context, int mouseX, int mouseY, float t) {
        int cardBottom = CONTENT_Y + 46;
        if (selectedGroup != null) cardBottom += selectedGroup.isUseGroupColor() ? 72 : 48;

        UIRenderUtil.fillGlass(context, PANEL_X, CONTENT_Y, PANEL_X + PANEL_W, cardBottom, CARD_RADIUS, withAlpha(COL_PANEL, t));
        context.drawTextWithShadow(this.textRenderer, "HUD Editor", PANEL_X + 14, CONTENT_Y + 10, withAlpha(COL_TEXT, t));
        context.drawTextWithShadow(this.textRenderer, "Drag an element onto another to merge them.", PANEL_X + 14, CONTENT_Y + 22, withAlpha(COL_TEXT_DIM, t));
        context.drawTextWithShadow(this.textRenderer, "Right-click a merged one to pull it back out.", PANEL_X + 14, CONTENT_Y + 33, withAlpha(COL_TEXT_DIM, t));

        if (selectedGroup == null) return;
        int y = CONTENT_Y + 50;
        context.drawTextWithShadow(this.textRenderer, "Selected group", PANEL_X + 14, y, withAlpha(COL_TEXT, t));
        y += 14;

        boolean use = selectedGroup.isUseGroupColor();
        boolean toggleHovered = isInside(mouseX, mouseY, PANEL_X + PANEL_W - 40, y, 28, 14);
        drawToggle(context, PANEL_X + PANEL_W - 40, y, use ? 1f : (toggleHovered ? 0.15f : 0f), t);
        context.drawTextWithShadow(this.textRenderer, "Shared color for whole pill", PANEL_X + 14, y + 3, withAlpha(COL_TEXT_DIM, t));
        y += 20;

        if (use) {
            int size = 16, gap = 6, startX = PANEL_X + 14;
            for (int i = 0; i < THEME_SWATCHES.length; i++) {
                int x = startX + i * (size + gap);
                boolean isCurrent = (THEME_SWATCHES[i] | 0xFF000000) == selectedGroup.getGroupColor();
                if (isCurrent) UIRenderUtil.fillRounded(context, x - 2, y - 2, x + size + 2, y + size + 2, 5, withAlpha(COL_ACCENT, t));
                UIRenderUtil.fillRounded(conte
