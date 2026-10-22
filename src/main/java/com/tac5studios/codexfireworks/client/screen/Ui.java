package com.tac5studios.codexfireworks.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * Drawing helpers for this mod's screens. Everything is drawn with plain rectangles in the
 * vanilla grey style, so the screens need no texture files.
 */
public final class Ui {

    public static final int TEXT = 0x404040, TEXT_LIGHT = 0xFFFFFF, TEXT_DIM = 0x707070;
    private static final int BG = 0xFFC6C6C6, LIGHT = 0xFFFFFFFF, DARK = 0xFF555555, EDGE = 0xFF000000;
    private static final int SLOT = 0xFF8B8B8B, SLOT_DARK = 0xFF373737;

    private Ui() {}

    /** A raised grey panel with a black outline, like vanilla windows. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x + 1, y, x + w - 1, y + h, EDGE);
        g.fill(x, y + 1, x + w, y + h - 1, EDGE);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, BG);
        g.fill(x + 1, y + 1, x + w - 2, y + 3, LIGHT);
        g.fill(x + 1, y + 1, x + 3, y + h - 2, LIGHT);
        g.fill(x + 2, y + h - 3, x + w - 1, y + h - 1, DARK);
        g.fill(x + w - 3, y + 2, x + w - 1, y + h - 1, DARK);
        g.fill(x + 3, y + 3, x + w - 3, y + h - 3, BG);
    }

    /** A sunken box (for slots, the formation canvas and the pattern grid). */
    public static void sunken(GuiGraphics g, int x, int y, int w, int h, int fill) {
        g.fill(x, y, x + w, y + h, SLOT_DARK);
        g.fill(x + 1, y + 1, x + w, y + h, LIGHT);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }

    /** Background of an 18x18 item slot whose item sits at (x, y). */
    public static void slot(GuiGraphics g, int x, int y) {
        sunken(g, x - 1, y - 1, 18, 18, SLOT);
    }

    /** Slot backgrounds for a player inventory starting at (x, y), hotbar 58 lower. */
    public static void inventory(GuiGraphics g, int x, int y) {
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) slot(g, x + col * 18, y + row * 18);
        for (int col = 0; col < 9; col++) slot(g, x + col * 18, y + 58);
    }

    /** A small vanilla button. */
    public static Button button(int x, int y, int w, int h, String text, Button.OnPress press) {
        return Button.builder(Component.literal(text), press).bounds(x, y, w, h).build();
    }

    public static Button button(int x, int y, int w, int h, Component text, Button.OnPress press) {
        return Button.builder(text, press).bounds(x, y, w, h).build();
    }

    public static Button button(int x, int y, int w, int h, String text, Component tip, Button.OnPress press) {
        return Button.builder(Component.literal(text), press).bounds(x, y, w, h).tooltip(Tooltip.create(tip)).build();
    }

    /** Opaque version of a 0xRRGGBB color. */
    public static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    /** "#RRGGBB" */
    public static String hex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    /** Draws text at (x, y), shrunk to fit in width w when it is too long. */
    public static void fit(GuiGraphics g, Font font, Component text, int x, int y, int w, int color) {
        int tw = font.width(text);
        if (tw <= w) {
            g.drawString(font, text, x, y, color, false);
            return;
        }
        float s = (float) w / tw;
        g.pose().pushPose();
        g.pose().translate(x, y + (font.lineHeight * (1 - s)) / 2f, 0);
        g.pose().scale(s, s, 1);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }
}
