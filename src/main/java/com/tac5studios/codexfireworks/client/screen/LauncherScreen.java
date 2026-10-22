package com.tac5studios.codexfireworks.client.screen;

import com.tac5studios.codexfireworks.launcher.LauncherBlockEntity;
import com.tac5studios.codexfireworks.launcher.LauncherMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/** Launcher screen: 3x3 rocket slots on the left, firing settings on the right. */
public class LauncherScreen extends AbstractContainerScreen<LauncherMenu> {

    private static final int RX = 70;
    private static final String[] LABELS = {"direction", "tilt", "spread", "count", "interval"};

    public LauncherScreen(LauncherMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 230;
        this.imageHeight = 178;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 85;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos, y = topPos;
        for (int i = 0; i < LABELS.length; i++) {
            int row = y + 16 + i * 13;
            int minus = i * 2, plus = i * 2 + 1;
            addRenderableWidget(Ui.button(x + RX + 80, row, 12, 12, "-", b -> click(minus)));
            addRenderableWidget(Ui.button(x + RX + 140, row, 12, 12, "+", b -> click(plus)));
        }
        addRenderableWidget(Ui.button(x + imageWidth - 50, y + 4, 42, 11, Component.translatable("gui.codex_fireworks.test_fire"), b -> click(10)));
    }

    private void click(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) mc.gameMode.handleInventoryButtonClick(menu.containerId, hasShiftDown() ? id + LauncherMenu.SHIFT : id);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float partial) {
        super.render(g, mx, my, partial);
        renderTooltip(g, mx, my);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partial, int mx, int my) {
        Ui.panel(g, leftPos, topPos, imageWidth, imageHeight);
        for (int i = 0; i < LauncherBlockEntity.SLOTS; i++) {
            Ui.slot(g, leftPos + LauncherMenu.SLOT_X + (i % 3) * 18, topPos + LauncherMenu.SLOT_Y + (i / 3) * 18);
        }
        Ui.inventory(g, leftPos + LauncherMenu.INV_X, topPos + LauncherMenu.INV_Y);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mx, int my) {
        g.drawString(font, title, titleLabelX, titleLabelY, Ui.TEXT, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, Ui.TEXT, false);
        for (int i = 0; i < LABELS.length; i++) {
            int row = 18 + i * 13;
            g.drawString(font, Component.translatable("gui.codex_fireworks.launcher." + LABELS[i]), RX, row, Ui.TEXT, false);
            g.drawCenteredString(font, value(i), RX + 116, row, Ui.TEXT_LIGHT);
        }
    }

    private String value(int i) {
        int v = menu.get(i);
        return switch (i) {
            case LauncherBlockEntity.YAW -> compass(v);
            case LauncherBlockEntity.ANGLE -> v == 0 ? "Up" : v + "°";
            case LauncherBlockEntity.SPREAD -> v == 0 ? "Off" : "±" + v + "°";
            case LauncherBlockEntity.INTERVAL -> String.format("%.2fs", v / 20.0);
            default -> String.valueOf(v);
        };
    }

    /** 0 = south, 90 = west, 180 = north, 270 = east (Minecraft's yaw). */
    private static String compass(int yaw) {
        String[] names = {"S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE"};
        return names[Math.floorMod(Math.round(yaw / 22.5f), 16)] + " " + yaw + "°";
    }
}
