package com.tac5studios.codexfireworks.client.screen;

import com.tac5studios.codexfireworks.client.ClientSettings;
import com.tac5studios.codexfireworks.client.ShellStarter;
import com.tac5studios.codexfireworks.network.CraftPresetPayload;
import com.tac5studios.codexfireworks.network.CraftRocketPayload;
import com.tac5studios.codexfireworks.pattern.Pattern;
import com.tac5studios.codexfireworks.registry.ModComponents;
import com.tac5studios.codexfireworks.registry.ModItems;
import com.tac5studios.codexfireworks.shell.Burst;
import com.tac5studios.codexfireworks.shell.RocketPreset;
import com.tac5studios.codexfireworks.shell.Shape;
import com.tac5studios.codexfireworks.shell.Shell;
import com.tac5studios.codexfireworks.workbench.Formation;
import com.tac5studios.codexfireworks.workbench.SlotLayout;
import com.tac5studios.codexfireworks.workbench.WorkbenchCrafting;
import com.tac5studios.codexfireworks.workbench.WorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Firework Workbench screen.
 * Left: paper, gunpowder and the 8 burst slots, and the settings of the selected burst.
 * Right: the formation seen from the front (drag bursts to move them), formation presets,
 * the rocket name, Preview and Craft, and the rocket presets.
 */
public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {

    private static final int CANVAS_X = 176, CANVAS_Y = 18, CANVAS_W = 136, CANVAS_H = 88;

    private final List<SlotLayout> layouts = new ArrayList<>();
    private int selected = 0;
    private int flight = 1;
    private int formation = 1; // Ring
    private float spacing = 5f;
    private int preset = 0;
    private boolean dragging;
    private EditBox name;
    /** Ticks left of an in-world preview. While it runs the screen draws nothing so the sky is visible. */
    private int previewTicks;
    /** The player's pitch before a preview tilted the view up to the sky (NaN when no preview is tilting it). */
    private float pitchBeforePreview = Float.NaN;

    public WorkbenchScreen(WorkbenchMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 320;
        this.imageHeight = 236;
        this.inventoryLabelY = 143;
        for (int i = 0; i < WorkbenchMenu.BURSTS; i++) layouts.add(SlotLayout.DEFAULT);
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos, y = topPos;

        // Flight
        addRenderableWidget(Ui.button(x + 112, y + 20, 12, 12, "-", b -> flight = Math.max(1, flight - 1)));
        addRenderableWidget(Ui.button(x + 158, y + 20, 12, 12, "+", b -> flight = Math.min(3, flight + 1)));

        // Selected burst
        addRenderableWidget(Ui.button(x + 52, y + 84, 12, 12, "<", b -> cycleShape(-1)));
        addRenderableWidget(Ui.button(x + 158, y + 84, 12, 12, ">", b -> cycleShape(1)));
        addRenderableWidget(Ui.button(x + 34, y + 99, 12, 12, "-", b -> edit(l -> l.withSize(round(l.size() - 0.1f)))));
        addRenderableWidget(Ui.button(x + 70, y + 99, 12, 12, "+", b -> edit(l -> l.withSize(round(l.size() + 0.1f)))));
        addRenderableWidget(Ui.button(x + 120, y + 99, 12, 12, "-", b -> edit(l -> l.withDelay(l.delay() - (hasShiftDown() ? 10 : 1)))));
        addRenderableWidget(Ui.button(x + 158, y + 99, 12, 12, "+", b -> edit(l -> l.withDelay(Math.min(menu.maxDelay, l.delay() + (hasShiftDown() ? 10 : 1))))));
        addRenderableWidget(Ui.button(x + 18, y + 114, 12, 12, "-", b -> move(-1, 0, 0)));
        addRenderableWidget(Ui.button(x + 44, y + 114, 12, 12, "+", b -> move(1, 0, 0)));
        addRenderableWidget(Ui.button(x + 74, y + 114, 12, 12, "-", b -> move(0, -1, 0)));
        addRenderableWidget(Ui.button(x + 100, y + 114, 12, 12, "+", b -> move(0, 1, 0)));
        addRenderableWidget(Ui.button(x + 130, y + 114, 12, 12, "-", b -> move(0, 0, -1)));
        addRenderableWidget(Ui.button(x + 156, y + 114, 12, 12, "+", b -> move(0, 0, 1)));
        addRenderableWidget(Ui.button(x + 8, y + 128, 80, 12, Component.translatable("gui.codex_fireworks.copy_to_all"), b -> copyToAll()));
        addRenderableWidget(Ui.button(x + 90, y + 128, 80, 12, Component.translatable("gui.codex_fireworks.reset"), b -> resetSelected()));

        // Formation presets
        addRenderableWidget(Ui.button(x + CANVAS_X, y + 110, 12, 12, "<", b -> formation = Math.floorMod(formation - 1, Formation.values().length)));
        addRenderableWidget(Ui.button(x + CANVAS_X + 82, y + 110, 12, 12, ">", b -> formation = Math.floorMod(formation + 1, Formation.values().length)));
        addRenderableWidget(Ui.button(x + CANVAS_X + 96, y + 110, 40, 12, Component.translatable("gui.codex_fireworks.apply"), b -> applyFormation()));
        addRenderableWidget(Ui.button(x + CANVAS_X + 56, y + 124, 12, 12, "-", b -> spacing = Math.max(1f, spacing - 1f)));
        addRenderableWidget(Ui.button(x + CANVAS_X + 82, y + 124, 12, 12, "+", b -> spacing = Math.min(menu.maxOffset, spacing + 1f)));

        name = new EditBox(font, x + CANVAS_X, y + 140, CANVAS_W, 14, Component.translatable("gui.codex_fireworks.name"));
        name.setMaxLength(WorkbenchCrafting.MAX_NAME);
        name.setHint(Component.translatable("gui.codex_fireworks.name_hint"));
        addRenderableWidget(name);

        Button preview = addRenderableWidget(Ui.button(x + CANVAS_X, y + 158, 66, 16, Component.translatable("gui.codex_fireworks.preview"), b -> preview()));
        Button craft = addRenderableWidget(Ui.button(x + CANVAS_X + 70, y + 158, 66, 16, Component.translatable("gui.codex_fireworks.craft"), b -> craft()));
        preview.active = menu.shellsOn;
        craft.active = menu.shellsOn;

        if (menu.presetsOn && !menu.presets.isEmpty()) {
            addRenderableWidget(Ui.button(x + CANVAS_X, y + 188, 12, 12, "<", b -> preset = Math.floorMod(preset - 1, menu.presets.size())));
            addRenderableWidget(Ui.button(x + CANVAS_X + 124, y + 188, 12, 12, ">", b -> preset = Math.floorMod(preset + 1, menu.presets.size())));
            addRenderableWidget(Ui.button(x + CANVAS_X, y + 202, 66, 14, Component.translatable("gui.codex_fireworks.preview"), b -> previewPreset()));
            addRenderableWidget(Ui.button(x + CANVAS_X + 70, y + 202, 66, 14, Component.translatable("gui.codex_fireworks.make"), b -> craftPreset()));
        }
    }

    // ---- editing ----

    private interface Edit {
        SlotLayout apply(SlotLayout l);
    }

    private void edit(Edit e) {
        layouts.set(selected, e.apply(layouts.get(selected)));
    }

    private void move(float x, float y, float z) {
        float step = hasShiftDown() ? 0.5f : 1f;
        int max = menu.maxOffset;
        edit(l -> l.withOffset(clamp(l.dx() + x * step, max), clamp(l.dy() + y * step, max), clamp(l.dz() + z * step, max)));
    }

    private static float clamp(float v, int max) {
        return Math.max(-max, Math.min(max, v));
    }

    private static float round(float v) {
        return Math.round(v * 10) / 10f;
    }

    private void cycleShape(int dir) {
        ItemStack stack = menu.burstItem(selected);
        if (!stack.is(Items.FIREWORK_STAR)) return;
        List<Shape> shapes = new ArrayList<>();
        for (Shape s : Shape.builtIn()) if (!ClientSettings.shapeOff(s.id)) shapes.add(s);
        if (shapes.isEmpty()) return;
        Shape now = Shape.byId(shapeOf(selected));
        int i = Math.max(0, shapes.indexOf(now));
        edit(l -> l.withShape(shapes.get(Math.floorMod(i + dir, shapes.size())).id));
    }

    private void copyToAll() {
        SlotLayout src = layouts.get(selected);
        for (int i = 0; i < layouts.size(); i++) {
            if (i == selected) continue;
            SlotLayout l = layouts.get(i);
            layouts.set(i, l.withSize(src.size()).withShape(menu.burstItem(i).is(Items.FIREWORK_STAR) ? src.shape() : l.shape()));
        }
    }

    private void resetSelected() {
        layouts.set(selected, SlotLayout.DEFAULT);
    }

    private void applyFormation() {
        List<Integer> filled = filledSlots();
        if (filled.isEmpty()) return;
        List<SlotLayout> old = new ArrayList<>();
        for (int i : filled) old.add(layouts.get(i));
        List<SlotLayout> placed = Formation.values()[formation].apply(old, filled.size(), spacing, System.nanoTime());
        for (int k = 0; k < filled.size(); k++) {
            SlotLayout p = placed.get(k);
            layouts.set(filled.get(k), p.withOffset(clamp(p.dx(), menu.maxOffset), clamp(p.dy(), menu.maxOffset), clamp(p.dz(), menu.maxOffset))
                    .withDelay(Math.min(menu.maxDelay, p.delay())));
        }
    }

    private List<Integer> filledSlots() {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < WorkbenchMenu.BURSTS; i++) if (!menu.burstItem(i).isEmpty()) out.add(i);
        return out;
    }

    /** The shape of burst slot i as it will be made. */
    private String shapeOf(int i) {
        ItemStack stack = menu.burstItem(i);
        if (stack.is(ModItems.PATTERN_CARD.get())) return Shape.PATTERN.id;
        String s = layouts.get(i).shape();
        return s.isEmpty() ? WorkbenchCrafting.defaultShape(stack) : s;
    }

    // ---- actions ----

    private void craft() {
        PacketDistributor.sendToServer(new CraftRocketPayload(menu.containerId, List.copyOf(layouts), flight, name.getValue()));
    }

    private void craftPreset() {
        if (menu.presets.isEmpty()) return;
        PacketDistributor.sendToServer(new CraftPresetPayload(menu.containerId, menu.presets.get(preset).id()));
    }

    private void preview() {
        String[] err = new String[1];
        List<Burst> bursts = WorkbenchCrafting.bursts(menu.container(), layouts, menu.maxOffset, menu.maxDelay, err);
        if (bursts == null) return;
        startPreview(new Shell(bursts, 1.0f));
    }

    private void previewPreset() {
        if (menu.presets.isEmpty()) return;
        RocketPreset p = menu.presets.get(preset).preset();
        startPreview(new Shell(p.bursts(), p.scale()));
    }

    /** Pop the shell in the sky in front of the player, and hide the screen while it plays. */
    private void startPreview(Shell shell) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;
        Vec3 look = player.getLookAngle().multiply(1, 0, 1);
        if (look.lengthSqr() < 1.0E-4) look = new Vec3(0, 0, 1);
        Vec3 at = player.getEyePosition().add(look.normalize().scale(32)).add(0, 14, 0);
        // Look up a little so the burst is in view even when the player was looking down at the workbench.
        if (Float.isNaN(pitchBeforePreview)) pitchBeforePreview = player.getXRot();
        setPitch(player, PREVIEW_PITCH);
        mc.particleEngine.add(new ShellStarter(mc.level, at.x, at.y, at.z, shell));
        int last = 0;
        for (Burst b : shell.bursts()) last = Math.max(last, b.delay());
        previewTicks = last + 80;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (previewTicks > 0 && --previewTicks == 0) endPreview();
    }

    private static final float PREVIEW_PITCH = -18f;

    private static void setPitch(LocalPlayer player, float pitch) {
        player.setXRot(pitch);
        player.xRotO = pitch;
    }

    /** Ends a preview early or on time, and puts the view back where it was. */
    private void endPreview() {
        previewTicks = 0;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && !Float.isNaN(pitchBeforePreview)) setPitch(player, pitchBeforePreview);
        pitchBeforePreview = Float.NaN;
    }

    @Override
    public void removed() {
        endPreview();
        super.removed();
    }

    // ---- mouse and keys ----

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (previewTicks > 0) {
            endPreview();
            return true;
        }
        int cx = leftPos + CANVAS_X, cy = topPos + CANVAS_Y;
        if (button == 0 && Ui.inside(mx, my, cx, cy, CANVAS_W, CANVAS_H)) {
            int best = -1;
            double bestDist = 36;
            for (int i : filledSlots()) {
                double d = Math.pow(mx - dotX(i), 2) + Math.pow(my - dotY(i), 2);
                if (d < bestDist) {
                    bestDist = d;
                    best = i;
                }
            }
            if (best >= 0) selected = best;
            if (!menu.burstItem(selected).isEmpty()) {
                dragging = true;
                dragTo(mx, my);
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            dragTo(mx, my);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    private void dragTo(double mx, double my) {
        double k = scale();
        float x = clamp(Math.round((mx - canvasCenterX()) / k * 2) / 2f, menu.maxOffset);
        float y = clamp(Math.round((canvasCenterY() - my) / k * 2) / 2f, menu.maxOffset);
        edit(l -> l.withOffset(x, y, l.dz()));
    }

    @Override
    protected void slotClicked(@NotNull Slot slot, int id, int button, @NotNull ClickType type) {
        if (slot.container == menu.container() && slot.getContainerSlot() >= WorkbenchMenu.FIRST_BURST) {
            selected = slot.getContainerSlot() - WorkbenchMenu.FIRST_BURST;
        }
        super.slotClicked(slot, id, button, type);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (previewTicks > 0) {
            endPreview();
            return true;
        }
        if (name != null && name.isFocused() && key != GLFW.GLFW_KEY_ESCAPE) {
            return name.keyPressed(key, scan, mods) || name.canConsumeInput() || super.keyPressed(key, scan, mods);
        }
        return super.keyPressed(key, scan, mods);
    }

    // ---- drawing ----

    private double scale() {
        double max = Math.max(1, menu.maxOffset);
        return Math.min((CANVAS_W / 2.0 - 6) / max, (CANVAS_H / 2.0 - 6) / max);
    }

    private double canvasCenterX() {
        return leftPos + CANVAS_X + CANVAS_W / 2.0;
    }

    private double canvasCenterY() {
        return topPos + CANVAS_Y + CANVAS_H / 2.0;
    }

    private double dotX(int i) {
        return canvasCenterX() + layouts.get(i).dx() * scale();
    }

    private double dotY(int i) {
        return canvasCenterY() - layouts.get(i).dy() * scale();
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float partial) {
        if (previewTicks > 0) {
            g.drawCenteredString(font, Component.translatable("gui.codex_fireworks.previewing"), width / 2, height - 40, Ui.TEXT_LIGHT);
            return;
        }
        super.render(g, mx, my, partial);
        renderTooltip(g, mx, my);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partial, int mx, int my) {
        int x = leftPos, y = topPos;
        Ui.panel(g, x, y, imageWidth, imageHeight);

        Ui.slot(g, x + WorkbenchMenu.PAPER_X, y + WorkbenchMenu.PAPER_Y);
        Ui.slot(g, x + WorkbenchMenu.GUNPOWDER_X, y + WorkbenchMenu.GUNPOWDER_Y);
        if (menu.container().getItem(WorkbenchMenu.PAPER).isEmpty()) ghost(g, Items.PAPER.getDefaultInstance(), x + WorkbenchMenu.PAPER_X, y + WorkbenchMenu.PAPER_Y);
        if (menu.container().getItem(WorkbenchMenu.GUNPOWDER).isEmpty()) ghost(g, Items.GUNPOWDER.getDefaultInstance(), x + WorkbenchMenu.GUNPOWDER_X, y + WorkbenchMenu.GUNPOWDER_Y);
        for (int i = 0; i < WorkbenchMenu.BURSTS; i++) {
            int sx = x + WorkbenchMenu.BURST_X + (i % 4) * 18, sy = y + WorkbenchMenu.BURST_Y + (i / 4) * 18;
            Ui.slot(g, sx, sy);
            if (i == selected) g.renderOutline(sx - 1, sy - 1, 18, 18, 0xFFFFD84A);
        }
        Ui.inventory(g, x + WorkbenchMenu.INV_X, y + WorkbenchMenu.INV_Y);

        // Formation canvas, seen from the front: right = +X, up = +Y.
        int cx = x + CANVAS_X, cy = y + CANVAS_Y;
        Ui.sunken(g, cx, cy, CANVAS_W, CANVAS_H, 0xFF101826);
        int mxc = (int) canvasCenterX(), myc = (int) canvasCenterY();
        g.fill(cx + 1, myc, cx + CANVAS_W - 1, myc + 1, 0xFF2C3A55);
        g.fill(mxc, cy + 1, mxc + 1, cy + CANVAS_H - 1, 0xFF2C3A55);
        for (int i : filledSlots()) {
            int dx = (int) Math.round(dotX(i)), dy = (int) Math.round(dotY(i));
            int r = 2 + Math.round(layouts.get(i).size() * 2);
            g.fill(dx - r, dy - r, dx + r + 1, dy + r + 1, Ui.opaque(colorOf(i)));
            if (i == selected) g.renderOutline(dx - r - 1, dy - r - 1, 2 * r + 3, 2 * r + 3, 0xFFFFFFFF);
        }
    }

    private void ghost(GuiGraphics g, ItemStack stack, int x, int y) {
        g.renderFakeItem(stack, x, y);
        g.fill(x, y, x + 16, y + 16, 0x998B8B8B);
    }

    /** First color of the item in burst slot i, for its dot on the canvas. */
    private int colorOf(int i) {
        ItemStack stack = menu.burstItem(i);
        if (stack.is(ModItems.PATTERN_CARD.get())) {
            Pattern p = stack.get(ModComponents.PATTERN.get());
            return p == null || p.isEmpty() ? 0x888888 : p.usedColors().getFirst();
        }
        FireworkExplosion fx = stack.get(DataComponents.FIREWORK_EXPLOSION);
        return fx == null || fx.colors().isEmpty() ? 0xFFFFFF : fx.colors().getInt(0);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mx, int my) {
        g.drawString(font, title, titleLabelX, titleLabelY, Ui.TEXT, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, Ui.TEXT, false);

        g.drawString(font, Component.translatable("gui.codex_fireworks.flight"), 50, 22, Ui.TEXT, false);
        g.drawCenteredString(font, String.valueOf(flight), 141, 22, Ui.TEXT_LIGHT);
        g.drawString(font, Component.translatable("gui.codex_fireworks.bursts_hint"), 84, 50, Ui.TEXT_DIM, false);

        // Selected burst
        SlotLayout l = layouts.get(selected);
        ItemStack stack = menu.burstItem(selected);
        g.drawString(font, "#" + (selected + 1), 8, 86, Ui.TEXT, false);
        Component shapeName = stack.isEmpty() ? Component.translatable("gui.codex_fireworks.empty")
                : Component.translatable("shape.codex_fireworks." + shapeOf(selected));
        g.drawCenteredString(font, shapeName, 111, 86, Ui.TEXT_LIGHT);
        g.drawString(font, Component.translatable("gui.codex_fireworks.size"), 8, 101, Ui.TEXT, false);
        g.drawCenteredString(font, String.format("%.1f", l.size()), 58, 101, Ui.TEXT_LIGHT);
        g.drawString(font, Component.translatable("gui.codex_fireworks.delay"), 86, 101, Ui.TEXT, false);
        g.drawCenteredString(font, l.delay() + "t", 145, 101, Ui.TEXT_LIGHT);
        g.drawString(font, "X", 8, 116, Ui.TEXT, false);
        g.drawCenteredString(font, fmt(l.dx()), 37, 116, Ui.TEXT_LIGHT);
        g.drawString(font, "Y", 64, 116, Ui.TEXT, false);
        g.drawCenteredString(font, fmt(l.dy()), 93, 116, Ui.TEXT_LIGHT);
        g.drawString(font, "Z", 120, 116, Ui.TEXT, false);
        g.drawCenteredString(font, fmt(l.dz()), 149, 116, Ui.TEXT_LIGHT);

        // Formation
        g.drawCenteredString(font, Component.translatable(Formation.values()[formation].langKey()), CANVAS_X + 47, 112, Ui.TEXT_LIGHT);
        g.drawString(font, Component.translatable("gui.codex_fireworks.spacing"), CANVAS_X, 126, Ui.TEXT, false);
        g.drawCenteredString(font, String.valueOf((int) spacing), CANVAS_X + 75, 126, Ui.TEXT_LIGHT);

        if (!menu.shellsOn) {
            Ui.fit(g, font, Component.translatable("gui.codex_fireworks.shells_off"), CANVAS_X, 176, CANVAS_W, Ui.TEXT_DIM);
        }
        if (menu.presetsOn && !menu.presets.isEmpty()) {
            WorkbenchMenu.PresetEntry e = menu.presets.get(preset);
            g.drawString(font, Component.translatable("gui.codex_fireworks.presets"), CANVAS_X, 178, Ui.TEXT, false);
            String n = font.plainSubstrByWidth(e.preset().name(), 108);
            g.drawCenteredString(font, n, CANVAS_X + 68, 190, Ui.TEXT_LIGHT);
            int cost = WorkbenchCrafting.presetCost(e.preset(), menu.presetGunpowder);
            Ui.fit(g, font, Component.translatable("gui.codex_fireworks.preset_cost", cost), CANVAS_X, 220, CANVAS_W, Ui.TEXT_DIM);
        }
    }

    private static String fmt(float v) {
        return v == Math.round(v) ? String.valueOf(Math.round(v)) : String.format("%.1f", v);
    }
}
