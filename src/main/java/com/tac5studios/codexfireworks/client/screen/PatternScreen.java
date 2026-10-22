package com.tac5studios.codexfireworks.client.screen;

import com.tac5studios.codexfireworks.network.OpenPatternPayload;
import com.tac5studios.codexfireworks.network.SavePatternPayload;
import com.tac5studios.codexfireworks.pattern.Pattern;
import com.tac5studios.codexfireworks.pattern.PatternText;
import com.tac5studios.codexfireworks.registry.ModComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Pattern design screen, opened by using a Pattern Card.
 * Left: the grid (left mouse draws, right mouse erases), text mode and the card name.
 * Right: colors, tools, the pattern options, ready-made patterns, Save and Cancel.
 */
public class PatternScreen extends Screen {

    private static final int W = 300, H = 240, GRID = 160;
    private enum Tool { DRAW, ERASE, FILL }

    /** Tool button positions and widths (Draw, Erase, Fill, Clear), sized so each label fits. */
    private static final int[] TOOL_X = {0, 29, 64, 86}, TOOL_W = {28, 34, 21, 30};

    private final OpenPatternPayload open;
    private final List<Integer> palette = new ArrayList<>(Pattern.dyePalette());
    private int gw = Pattern.DRAW_SIZE, gh = Pattern.DRAW_SIZE;
    private int[] pixels = new int[Pattern.DRAW_SIZE * Pattern.DRAW_SIZE];
    private int color = DyeColor.YELLOW.getId();
    private Tool tool = Tool.DRAW;
    private String size = "medium";
    private boolean faceViewer = true;
    private int yaw = 0;
    private boolean spin, sparkle;
    private int fade = -1;
    private int preset = 0;
    private int painting = -2; // -2 = not painting, otherwise the color index being painted (-1 erases)
    private int lastPainted = -1; // last pixel painted in this stroke, so fast drags draw a line with no gaps

    private int left, top;
    private EditBox nameBox, textBox, hexBox;
    private Button sizeButton, faceButton, spinButton, sparkleButton, fadeButton;
    private Component status = Component.empty();
    private int statusTicks;
    private String startName = "";

    public PatternScreen(OpenPatternPayload open) {
        super(Component.translatable("gui.codex_fireworks.pattern_title"));
        this.open = open;
        Arrays.fill(pixels, -1);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack card = player.getItemInHand(open.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            Pattern p = card.get(ModComponents.PATTERN.get());
            if (p != null) load(p);
            Component itemName = card.get(DataComponents.ITEM_NAME);
            if (itemName != null && p != null) startName = itemName.getString();
        }
    }

    /** Put a saved pattern into the editor. Its colors are added to the working palette. */
    private void load(Pattern p) {
        gw = p.width();
        gh = p.height();
        pixels = new int[gw * gh];
        for (int i = 0; i < pixels.length; i++) {
            int idx = p.colorIndex(i);
            pixels[i] = idx < 0 ? -1 : paletteIndex(p.palette().get(idx));
        }
        size = p.size();
        faceViewer = p.faceViewer();
        yaw = p.yaw();
        spin = p.spin();
        sparkle = p.sparkle();
        fade = p.fade();
    }

    private int paletteIndex(int rgb) {
        int i = palette.indexOf(rgb);
        if (i >= 0) return i;
        if (palette.size() >= Pattern.MAX_COLORS) return 0;
        palette.add(rgb);
        return palette.size() - 1;
    }

    @Override
    protected void init() {
        left = (this.width - W) / 2;
        top = (this.height - H) / 2;
        int rx = left + 176;

        if (open.hex()) {
            hexBox = new EditBox(font, rx + 60, top + 20, 56, 14, Component.translatable("gui.codex_fireworks.hex"));
            hexBox.setMaxLength(7);
            hexBox.setHint(Component.literal("#FFAA00"));
            addRenderableWidget(hexBox);
            addRenderableWidget(Ui.button(rx + 60, top + 36, 56, 12, Component.translatable("gui.codex_fireworks.add_color"), b -> addHex()));
        }

        addRenderableWidget(Ui.button(rx + TOOL_X[0], top + 80, TOOL_W[0], 14, Component.translatable("gui.codex_fireworks.tool.draw"), b -> tool = Tool.DRAW));
        addRenderableWidget(Ui.button(rx + TOOL_X[1], top + 80, TOOL_W[1], 14, Component.translatable("gui.codex_fireworks.tool.erase"), b -> tool = Tool.ERASE));
        addRenderableWidget(Ui.button(rx + TOOL_X[2], top + 80, TOOL_W[2], 14, Component.translatable("gui.codex_fireworks.tool.fill"), b -> tool = Tool.FILL));
        addRenderableWidget(Ui.button(rx + TOOL_X[3], top + 80, TOOL_W[3], 14, Component.translatable("gui.codex_fireworks.tool.clear"), b -> clear()));

        sizeButton = addRenderableWidget(Ui.button(rx, top + 98, 116, 14, Component.empty(), b -> {
            size = Pattern.SIZES.get((Pattern.SIZES.indexOf(size) + 1) % Pattern.SIZES.size());
            refreshButtons();
        }));
        faceButton = addRenderableWidget(Ui.button(rx, top + 114, 116, 14, Component.empty(), b -> {
            if (faceViewer) {
                faceViewer = false;
                yaw = 0;
            } else if (yaw < 270) {
                yaw += 90;
            } else {
                faceViewer = true;
                yaw = 0;
            }
            refreshButtons();
        }));
        spinButton = addRenderableWidget(Ui.button(rx, top + 130, 50, 14, Component.empty(), b -> {
            spin = !spin;
            refreshButtons();
        }));
        sparkleButton = addRenderableWidget(Ui.button(rx + 51, top + 130, 65, 14, Component.empty(), b -> {
            sparkle = !sparkle;
            refreshButtons();
        }));
        fadeButton = addRenderableWidget(Ui.button(rx, top + 146, 116, 14, Component.empty(), b -> {
            fade = hasShiftDown() || fade == palette.get(color) ? -1 : palette.get(color);
            refreshButtons();
        }));

        if (!open.presets().isEmpty()) {
            addRenderableWidget(Ui.button(rx, top + 164, 12, 14, "<", b -> preset = Math.floorMod(preset - 1, open.presets().size())));
            addRenderableWidget(Ui.button(rx + 104, top + 164, 12, 14, ">", b -> preset = Math.floorMod(preset + 1, open.presets().size())));
            addRenderableWidget(Ui.button(rx, top + 180, 116, 14, Component.translatable("gui.codex_fireworks.load_preset"), b -> loadPreset()));
        }

        addRenderableWidget(Ui.button(rx, top + 204, 57, 16, Component.translatable("gui.codex_fireworks.save"), b -> save()));
        addRenderableWidget(Ui.button(rx + 59, top + 204, 57, 16, Component.translatable("gui.cancel"), b -> onClose()));

        if (open.textMode()) {
            textBox = new EditBox(font, left + 8, top + 186, 110, 14, Component.translatable("gui.codex_fireworks.text"));
            textBox.setMaxLength(24);
            textBox.setHint(Component.translatable("gui.codex_fireworks.text_hint"));
            addRenderableWidget(textBox);
            addRenderableWidget(Ui.button(left + 122, top + 186, 46, 14, Component.translatable("gui.codex_fireworks.apply"), b -> applyText()));
        }

        String keep = nameBox == null ? startName : nameBox.getValue();
        nameBox = new EditBox(font, left + 8, top + 204, GRID, 14, Component.translatable("gui.codex_fireworks.name"));
        nameBox.setMaxLength(32);
        nameBox.setHint(Component.translatable("gui.codex_fireworks.card_name_hint"));
        nameBox.setValue(keep);
        addRenderableWidget(nameBox);
        refreshButtons();
    }

    private void refreshButtons() {
        sizeButton.setMessage(Component.translatable("gui.codex_fireworks.size_value", Component.translatable("pattern.codex_fireworks.size." + size)));
        faceButton.setMessage(faceViewer ? Component.translatable("pattern.codex_fireworks.face_viewer")
                : Component.translatable("gui.codex_fireworks.face_value", Component.translatable("gui.codex_fireworks.yaw." + yaw)));
        spinButton.setMessage(Component.translatable(spin ? "gui.codex_fireworks.spin_on" : "gui.codex_fireworks.spin_off"));
        sparkleButton.setMessage(Component.translatable(sparkle ? "gui.codex_fireworks.sparkle_on" : "gui.codex_fireworks.sparkle_off"));
        fadeButton.setMessage(fade < 0 ? Component.translatable("gui.codex_fireworks.fade_none")
                : Component.translatable("gui.codex_fireworks.fade_value", Ui.hex(fade)));
    }

    private void say(Component text) {
        status = text;
        statusTicks = 80;
    }

    // ---- editing ----

    private void clear() {
        gw = Pattern.DRAW_SIZE;
        gh = Pattern.DRAW_SIZE;
        pixels = new int[gw * gh];
        Arrays.fill(pixels, -1);
    }

    private void addHex() {
        if (hexBox == null) return;
        String v = hexBox.getValue().trim();
        if (v.startsWith("#")) v = v.substring(1);
        if (!v.matches("[0-9a-fA-F]{6}")) {
            say(Component.translatable("gui.codex_fireworks.bad_hex"));
            return;
        }
        if (palette.size() >= Pattern.MAX_COLORS) {
            say(Component.translatable("gui.codex_fireworks.palette_full"));
            return;
        }
        color = paletteIndex(Integer.parseInt(v, 16));
        hexBox.setValue("");
    }

    private void applyText() {
        String[] err = new String[1];
        List<String> lines = PatternText.lines(textBox.getValue(), err);
        if (lines == null) {
            say(Component.translatable("gui.codex_fireworks.text_error." + (err[0].startsWith("char:") ? "char" : err[0]),
                    err[0].startsWith("char:") ? err[0].substring(5) : ""));
            return;
        }
        Pattern p = PatternText.render(lines, List.of(palette.get(color)), current());
        load(p);
    }

    private void loadPreset() {
        OpenPatternPayload.Preset p = open.presets().get(preset);
        load(p.pattern());
        if (nameBox.getValue().isEmpty()) nameBox.setValue(p.name());
        refreshButtons();
    }

    /** The picture as a Pattern (with the full working palette). */
    private Pattern current() {
        StringBuilder g = new StringBuilder(pixels.length);
        for (int px : pixels) g.append(px < 0 ? Pattern.OFF : Pattern.indexChar(px));
        return new Pattern(gw, gh, g.toString(), palette, size, faceViewer, yaw, spin, sparkle, fade);
    }

    private int lit() {
        int n = 0;
        for (int px : pixels) if (px >= 0) n++;
        return n;
    }

    private void save() {
        if (lit() > open.maxSparks()) {
            say(Component.translatable("gui.codex_fireworks.too_many_sparks", open.maxSparks()));
            return;
        }
        PacketDistributor.sendToServer(new SavePatternPayload(open.offhand(), current().compact(), nameBox.getValue()));
        onClose();
    }

    private int cell() {
        return Math.max(1, Math.min(GRID / gw, GRID / gh));
    }

    private int gridLeft() {
        return left + 8 + (GRID - cell() * gw) / 2;
    }

    private int gridTop() {
        return top + 20 + (GRID - cell() * gh) / 2;
    }

    /** Pixel index under the mouse, or -1. */
    private int pixelAt(double mx, double my) {
        int c = cell();
        int x = (int) Math.floor((mx - gridLeft()) / c), y = (int) Math.floor((my - gridTop()) / c);
        if (x < 0 || y < 0 || x >= gw || y >= gh) return -1;
        return y * gw + x;
    }

    private void fill(int start, int to) {
        int from = pixels[start];
        if (from == to) return;
        Deque<Integer> todo = new ArrayDeque<>();
        todo.add(start);
        while (!todo.isEmpty()) {
            int i = todo.pop();
            if (pixels[i] != from) continue;
            pixels[i] = to;
            int x = i % gw, y = i / gw;
            if (x > 0) todo.add(i - 1);
            if (x < gw - 1) todo.add(i + 1);
            if (y > 0) todo.add(i - gw);
            if (y < gh - 1) todo.add(i + gw);
        }
    }

    // ---- input ----

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int px = pixelAt(mx, my);
        if (px >= 0 && (button == 0 || button == 1)) {
            int paint = button == 1 || tool == Tool.ERASE ? -1 : color;
            if (tool == Tool.FILL && button == 0) {
                fill(px, paint);
            } else {
                painting = paint;
                pixels[px] = paint;
                lastPainted = px;
            }
            return true;
        }
        // Palette swatches
        int rx = left + 176;
        for (int i = 0; i < palette.size(); i++) {
            int sx = rx + (i % 4) * 14, sy = top + 20 + (i / 4) * 14;
            if (i >= 16) {
                sx = rx + 60 + ((i - 16) % 4) * 14;
                sy = top + 52 + ((i - 16) / 4) * 14;
            }
            if (Ui.inside(mx, my, sx, sy, 13, 13)) {
                color = i;
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (painting != -2) {
            int px = pixelAt(mx, my);
            if (px >= 0) {
                if (lastPainted >= 0) line(lastPainted % gw, lastPainted / gw, px % gw, px / gw, painting);
                else pixels[px] = painting;
            }
            lastPainted = px;
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    /** Paints every pixel on the line between two pixels (Bresenham). */
    private void line(int x0, int y0, int x1, int y1, int paint) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
        while (true) {
            pixels[y0 * gw + x0] = paint;
            if (x0 == x1 && y0 == y1) return;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        painting = -2;
        lastPainted = -1;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_ENTER && textBox != null && textBox.isFocused()) {
            applyText();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void tick() {
        if (statusTicks > 0 && --statusTicks == 0) status = Component.empty();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---- drawing ----

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float partial) {
        super.render(g, mx, my, partial);
        g.drawString(font, title, left + 8, top + 7, Ui.TEXT, false);

        // Grid
        Ui.sunken(g, left + 7, top + 19, GRID + 2, GRID + 2, 0xFF101826);
        int c = cell(), gx = gridLeft(), gy = gridTop();
        for (int y = 0; y < gh; y++) {
            for (int x = 0; x < gw; x++) {
                int px = pixels[y * gw + x];
                int x0 = gx + x * c, y0 = gy + y * c;
                if (px >= 0) g.fill(x0, y0, x0 + c, y0 + c, Ui.opaque(palette.get(px)));
                else if (c >= 4) g.fill(x0, y0, x0 + c - 1, y0 + c - 1, ((x + y) & 1) == 0 ? 0xFF1A2438 : 0xFF161F31);
            }
        }
        int hover = pixelAt(mx, my);
        if (hover >= 0) g.renderOutline(gx + (hover % gw) * c, gy + (hover / gw) * c, c, c, 0xFFFFFFFF);

        // Palette
        int rx = left + 176;
        for (int i = 0; i < palette.size(); i++) {
            int sx = rx + (i % 4) * 14, sy = top + 20 + (i / 4) * 14;
            if (i >= 16) {
                sx = rx + 60 + ((i - 16) % 4) * 14;
                sy = top + 52 + ((i - 16) / 4) * 14;
            }
            g.fill(sx, sy, sx + 13, sy + 13, 0xFF000000);
            g.fill(sx + 1, sy + 1, sx + 12, sy + 12, Ui.opaque(palette.get(i)));
            if (i == color) g.renderOutline(sx - 1, sy - 1, 15, 15, 0xFFFFD84A);
        }
        if (!open.hex()) {
            g.fill(rx + 60, top + 20, rx + 116, top + 48, Ui.opaque(palette.get(color)));
            g.drawCenteredString(font, Ui.hex(palette.get(color)), rx + 88, top + 30, Ui.TEXT_LIGHT);
        }

        // Tool marker, spark count, presets, status
        int t = tool.ordinal();
        g.fill(rx + TOOL_X[t], top + 95, rx + TOOL_X[t] + TOOL_W[t], top + 96, 0xFFFFD84A);

        int n = lit();
        g.drawString(font, Component.translatable("gui.codex_fireworks.sparks", n, open.maxSparks()), left + 8, top + 222,
                n > open.maxSparks() ? 0xFFAA2222 : Ui.TEXT, false);
        g.drawString(font, gw + "x" + gh, left + 140, top + 222, Ui.TEXT_DIM, false);
        if (!open.presets().isEmpty()) {
            String pn = font.plainSubstrByWidth(open.presets().get(preset).name(), 86);
            g.drawCenteredString(font, pn, rx + 58, top + 167, Ui.TEXT_LIGHT);
        }
        if (statusTicks > 0) g.drawCenteredString(font, status, left + W / 2, top + H + 4, 0xFFFF6060);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics g, int mx, int my, float partial) {
        super.renderBackground(g, mx, my, partial);
        Ui.panel(g, left, top, W, H);
    }
}
