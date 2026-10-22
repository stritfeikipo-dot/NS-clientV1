package com.nsclient.hud;

import java.util.ArrayList;
import java.util.List;

/** A draggable pill that renders several TextStat modules merged into one row ("SN | 91 FPS | 0 ms ..."). */
public class HudGroup {

    /** Absolute screen-space bounds of one rendered member segment, refreshed every render call for hit-testing. */
    public record MemberBounds(String moduleName, double x1, double y1, double x2, double y2) {}

    private final List<String> members = new ArrayList<>();
    private double x;
    private double y;
    private double scale = 1.0;
    private boolean useGroupColor = false;
    private int groupColor = 0xFFB388FF;

    private List<MemberBounds> lastBounds = new ArrayList<>();
    private double lastWidth = 0;
    private double lastHeight = 0;

    public HudGroup() {}

    public HudGroup(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public List<String> getMembers() { return members; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = Math.max(0.5, Math.min(3.0, scale)); }
    public boolean isUseGroupColor() { return useGroupColor; }
    public void setUseGroupColor(boolean useGroupColor) { this.useGroupColor = useGroupColor; }
    public int getGroupColor() { return groupColor; }
    public void setGroupColor(int groupColor) { this.groupColor = groupColor; }

    public List<MemberBounds> getLastBounds() { return lastBounds; }
    public void setLastBounds(List<MemberBounds> lastBounds) { this.lastBounds = lastBounds; }
    public double getLastWidth() { return lastWidth; }
    public void setLastWidth(double lastWidth) { this.lastWidth = lastWidth; }
    public double getLastHeight() { return lastHeight; }
    public void setLastHeight(double lastHeight) { this.lastHeight = lastHeight; }

    public boolean containsPoint(double px, double py) {
        return px >= x && px <= x + lastWidth && py >= y && py <= y + lastHeight;
    }
}
