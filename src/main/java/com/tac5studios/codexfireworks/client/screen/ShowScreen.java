package com.tac5studios.codexfireworks.client.screen;

import com.tac5studios.codexfireworks.network.OpenShowPayload;
import com.tac5studios.codexfireworks.network.SaveShowPayload;
import com.tac5studios.codexfireworks.network.ShowActionPayload;
import com.tac5studios.codexfireworks.show.Cue;
import com.tac5studios.codexfireworks.show.ShowData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Show Controller screen: the show name and switches, the cue list (click a row to select it),
 * the selected cue's time / launcher / slot, and the show buttons.
 * Every button that does something on the server saves the show first.
 */
public final class ShowScreen extends Screen {

    private static final int W = 300, H = 222, ROWS = 9, ROW_H = 11;

    private OpenShowPayload data;
    private final List<Cue> cues = new ArrayList<>();
    private boolean loop, redstoneStart;
    private int selected = -1;
    private int scroll = 0;

    private int left, top;
    private EditBox nameBox;
    private Button loopButton, redstoneButton;

    public ShowScreen(OpenShowPayload data) {
        super(Component.translatable("gui.codex_fireworks.show_title"));
        apply(data);
    }

    public BlockPos pos() {
        return data.pos();
    }

    /** New data from the server (after a button or when reopened). */
    public void apply(OpenShowPayload d) {
        this.data = d;
        cues.clear();
        cues.addAll(d.show().cues());
        loop = d.show().loop();
        redstoneStart = d.show().redstoneStart();
        if (selected >= cues.size()) selected = cues.size() - 1;
        if (nameBox != null) nameBox.setValue(d.show().name());
        refreshButtons();
    }

    @Override
    protected void init() {
        left = (this.width - W) / 2;
        top = (this.height - H) / 2;

        String keep = nameBox == null ? data.show().name() : nameBox.getValue();
        nameBox = new EditBox(font, left + 8, top + 18, 136, 14, Component.translatable("gui.codex_fireworks.show_name"));
        nameBox.setMaxLength(ShowData.MAX_NAME);
        nameBox.setHint(Component.translatable("gui.codex_fireworks.show_name_hint"));
        nameBox.setTooltip(Tooltip.create(Component.translatable("gui.codex_fireworks.show_name_tip")));
        nameBox.setValue(keep);
        addRenderableWidget(nameBox);
        loopButton = addRenderableWidget(Ui.button(left + 148, top + 18, 70, 14, Component.empty(), b -> {
            loop = !loop;
            refreshButtons();
        }));
        redstoneButton = addRenderableWidget(Ui.button(left + 222, top + 18, 70, 14, Component.empty(), b -> {
            redstoneStart = !redstoneStart;
            refreshButtons();
        }));

        // Selected cue
        int cy = top + 152;
        addRenderableWidget(Ui.button(left + 34, cy, 12, 12, "-", b -> editTime(hasShiftDown() ? -20 : -5)));
        addRenderableWidget(Ui.button(left + 80, cy, 12, 12, "+", b -> editTime(hasShiftDown() ? 20 : 5)));
        addRenderableWidget(Ui.button(left + 146, cy, 12, 12, "<", b -> editLauncher(-1)));
        addRenderableWidget(Ui.button(left + 192, cy, 12, 12, ">", b -> editLauncher(1)));
        addRenderableWidget(Ui.button(left + 234, cy, 12, 12, "<", b -> editSlot(-1)));
        addRenderableWidget(Ui.button(left + 276, cy, 12, 12, ">", b -> editSlot(1)));

        int by = top + 168;
        addRenderableWidget(Ui.button(left + 8, by, 70, 14, Component.translatable("gui.codex_fireworks.add_cue"), b -> addCue()));
        addRenderableWidget(Ui.button(left + 80, by, 70, 14, Component.translatable("gui.codex_fireworks.remove_cue"), b -> removeCue()));
        addRenderableWidget(Ui.button(left + 152, by, 70, 14, Component.translatable("gui.codex_fireworks.test_cue"), b -> {
            if (selected >= 0) send(ShowActionPayload.TEST_CUE, selected);
        }));
        addRenderableWidget(Ui.button(left + 224, by, 68, 14, Component.translatable("gui.codex_fireworks.link"), b -> {
            send(ShowActionPayload.LINK, 0);
            onClose();
        }));

        int ay = top + 186;
        addRenderableWidget(Ui.button(left + 8, ay, 46, 16, Component.translatable("gui.codex_fireworks.start"), b -> send(ShowActionPayload.START, 0)));
        addRenderableWidget(Ui.button(left + 56, ay, 46, 16, Component.translatable("gui.codex_fireworks.stop"), b -> send(ShowActionPayload.STOP, 0)));
        addRenderableWidget(Ui.button(left + 104, ay, 60, 16, Component.translatable("gui.codex_fireworks.write_card"), b -> send(ShowActionPayload.WRITE_CARD, 0)));
        addRenderableWidget(Ui.button(left + 166, ay, 60, 16, Component.translatable("gui.codex_fireworks.read_card"), b -> send(ShowActionPayload.READ_CARD, 0)));
        addRenderableWidget(Ui.button(left + 228, ay, 64, 16, Component.translatable("gui.codex_fireworks.save"), b -> {
            save();
            onClose();
        }));
        refreshButtons();
    }

    private void refreshButtons() {
        if (loopButton == null) return;
        loopButton.setMessage(Component.translatable(loop ? "gui.codex_fireworks.loop_on" : "gui.codex_fireworks.loop_off"));
        redstoneButton.setMessage(Component.translatable(redstoneStart ? "gui.codex_fireworks.redstone_on" : "gui.codex_fireworks.redstone_off"));
    }

    private ShowData current() {
        return new ShowData(nameBox == null ? data.show().name() : nameBox.getValue(), loop, redstoneStart, cues);
    }

    private void save() {
        PacketDistributor.sendToServer(new SaveShowPayload(data.pos(), current()));
    }

    /** Save first, so the server works with what's on screen. */
    private void send(int action, int arg) {
        save();
        PacketDistributor.sendToServer(new ShowActionPayload(data.pos(), action, arg));
    }

    // ---- cue editing ----

    private void addCue() {
        if (cues.size() >= data.maxCues()) return;
        Cue base = selected >= 0 ? cues.get(selected) : cues.isEmpty() ? null : cues.getLast();
        Cue c = base == null ? new Cue(0, 0, 0) : new Cue(base.time() + 10, base.launcher(), base.slot());
        cues.add(c);
        sortKeep(c);
    }

    private void removeCue() {
        if (selected < 0 || selected >= cues.size()) return;
        cues.remove(selected);
        if (selected >= cues.size()) selected = cues.size() - 1;
    }

    private void editTime(int d) {
        if (selected < 0) return;
        Cue c = cues.get(selected);
        Cue n = new Cue(c.time() + d, c.launcher(), c.slot());
        cues.set(selected, n);
        sortKeep(n);
    }

    private void editLauncher(int d) {
        if (selected < 0) return;
        Cue c = cues.get(selected);
        int count = data.launchers().size();
        cues.set(selected, new Cue(c.time(), Math.floorMod(c.launcher() + d, count + 1), c.slot()));
    }

    private void editSlot(int d) {
        if (selected < 0) return;
        Cue c = cues.get(selected);
        cues.set(selected, new Cue(c.time(), c.launcher(), Math.floorMod(c.slot() + d, 10)));
    }

    /** Keep the list sorted by time and keep this cue selected (and visible). */
    private void sortKeep(Cue c) {
        cues.sort(Comparator.comparingInt(Cue::time));
        selected = cues.indexOf(c);
        if (selected < scroll) scroll = selected;
        if (selected >= scroll + ROWS) scroll = selected - ROWS + 1;
    }

    // ---- input ----

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int lx = left + 8, ly = top + 48;
        if (Ui.inside(mx, my, lx, ly, W - 16, ROWS * ROW_H)) {
            int row = (int) ((my - ly) / ROW_H) + scroll;
            if (row < cues.size()) selected = row;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int max = Math.max(0, cues.size() - ROWS);
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(sy)));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---- drawing ----

    @Override
    public void renderBackground(@NotNull GuiGraphics g, int mx, int my, float partial) {
        super.renderBackground(g, mx, my, partial);
        Ui.panel(g, left, top, W, H);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float partial) {
        super.render(g, mx, my, partial);
        g.drawString(font, title, left + 8, top + 7, Ui.TEXT, false);
        Component state = Component.translatable(data.running() ? "gui.codex_fireworks.running" : "gui.codex_fireworks.stopped");
        g.drawString(font, state, left + W - 8 - font.width(state), top + 7, data.running() ? 0xFF2E8B2E : Ui.TEXT_DIM, false);

        // Header and list
        int lx = left + 8, ly = top + 48;
        g.drawString(font, "#", lx + 2, top + 37, Ui.TEXT, false);
        g.drawString(font, Component.translatable("gui.codex_fireworks.time"), lx + 30, top + 37, Ui.TEXT, false);
        g.drawString(font, Component.translatable("gui.codex_fireworks.launcher_short"), lx + 90, top + 37, Ui.TEXT, false);
        g.drawString(font, Component.translatable("gui.codex_fireworks.slot"), lx + 200, top + 37, Ui.TEXT, false);
        Ui.sunken(g, lx - 1, ly - 1, W - 14, ROWS * ROW_H + 2, 0xFF101826);
        for (int r = 0; r < ROWS; r++) {
            int i = r + scroll;
            if (i >= cues.size()) break;
            Cue c = cues.get(i);
            int y = ly + r * ROW_H;
            if (i == selected) g.fill(lx, y, lx + W - 16, y + ROW_H, 0xFF2C3A55);
            g.drawString(font, String.valueOf(i + 1), lx + 2, y + 2, Ui.TEXT_LIGHT, false);
            g.drawString(font, seconds(c.time()), lx + 30, y + 2, Ui.TEXT_LIGHT, false);
            g.drawString(font, launcherName(c.launcher()), lx + 90, y + 2, Ui.TEXT_LIGHT, false);
            g.drawString(font, slotName(c.slot()), lx + 200, y + 2, Ui.TEXT_LIGHT, false);
        }
        if (cues.isEmpty()) g.drawCenteredString(font, Component.translatable("gui.codex_fireworks.no_cues"), left + W / 2, ly + 40, Ui.TEXT_DIM);

        // Selected cue
        int cy = top + 154;
        g.drawString(font, Component.translatable("gui.codex_fireworks.time"), left + 8, cy, Ui.TEXT, false);
        g.drawString(font, Component.translatable("gui.codex_fireworks.launcher_short"), left + 98, cy, Ui.TEXT, false);
        g.drawString(font, Component.translatable("gui.codex_fireworks.slot"), left + 210, cy, Ui.TEXT, false);
        if (selected >= 0 && selected < cues.size()) {
            Cue c = cues.get(selected);
            g.drawCenteredString(font, seconds(c.time()), left + 63, cy, Ui.TEXT_LIGHT);
            g.drawCenteredString(font, c.launcher() == 0 ? Component.translatable("gui.codex_fireworks.all_launchers").getString() : "#" + c.launcher(), left + 175, cy, Ui.TEXT_LIGHT);
            g.drawCenteredString(font, c.slot() == 0 ? Component.translatable("gui.codex_fireworks.next_rocket").getString() : String.valueOf(c.slot()), left + 261, cy, Ui.TEXT_LIGHT);
        }

        Component info = Component.translatable("gui.codex_fireworks.show_info", data.launchers().size(), cues.size(), data.maxCues(),
                seconds(cues.isEmpty() ? 0 : cues.getLast().time()));
        g.drawString(font, info, left + 8, top + 208, Ui.TEXT_DIM, false);
    }

    private static String seconds(int ticks) {
        return String.format("%.2fs", ticks / 20.0);
    }

    private String launcherName(int i) {
        if (i == 0) return Component.translatable("gui.codex_fireworks.all_launchers").getString();
        if (i > data.launchers().size()) return "#" + i + " ?";
        BlockPos p = data.launchers().get(i - 1);
        return "#" + i + "  " + p.getX() + " " + p.getY() + " " + p.getZ();
    }

    private static String slotName(int slot) {
        return slot == 0 ? Component.translatable("gui.codex_fireworks.next_rocket").getString()
                : Component.translatable("gui.codex_fireworks.slot_n", slot).getString();
    }
}
