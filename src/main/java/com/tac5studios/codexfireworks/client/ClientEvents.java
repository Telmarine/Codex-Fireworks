package com.tac5studios.codexfireworks.client;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.shell.Burst;
import com.tac5studios.codexfireworks.shell.Shape;
import com.tac5studios.codexfireworks.shell.Shell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Client setup: the spark particle, rocket tooltips, and clearing the server's switches on leave. */
@SuppressWarnings("unused") // nested classes are found by @EventBusSubscriber
public final class ClientEvents {

    private ClientEvents() {}

    @EventBusSubscriber(modid = CodexFireworks.MOD_ID, value = Dist.CLIENT)
    public static final class ModBus {

        private ModBus() {}

        @SubscribeEvent
        @SuppressWarnings("unused") // called by the event bus
        public static void onParticles(RegisterParticleProvidersEvent e) {
            e.registerSpriteSet(CodexFireworks.SPARK.get(), sprites -> {
                CodexSpark.sprites = sprites;
                return (type, level, x, y, z, dx, dy, dz) -> new CodexSpark(level, x, y, z, dx, dy, dz, 0xFFFFFF);
            });
        }
    }

    @EventBusSubscriber(modid = CodexFireworks.MOD_ID, value = Dist.CLIENT)
    public static final class GameBus {

        private GameBus() {}

        @SubscribeEvent
        @SuppressWarnings("unused") // called by the event bus
        public static void onTooltip(ItemTooltipEvent e) {
            ItemStack stack = e.getItemStack();
            if (!stack.is(Items.FIREWORK_ROCKET)) return;
            Optional<Shell> shell = Shell.read(stack);
            if (shell.isEmpty()) return;
            List<Component> lines = e.getToolTip();

            Fireworks fw = stack.get(DataComponents.FIREWORKS);
            if (fw != null) lines.add(line("tooltip.codex_fireworks.flight", Component.literal(String.valueOf(fw.flightDuration()))));
            lines.add(line("tooltip.codex_fireworks.bursts", Component.literal(String.valueOf(shell.get().bursts().size()))));

            Set<String> seen = new LinkedHashSet<>();
            for (Burst b : shell.get().bursts()) seen.add(b.shape());
            MutableComponent shapes = Component.empty();
            boolean first = true;
            for (String id : seen) {
                if (!first) shapes.append(", ");
                Shape s = Shape.byId(id);
                shapes.append(s == null ? Component.literal(id) : Component.translatable(s.langKey()));
                first = false;
            }
            lines.add(line("tooltip.codex_fireworks.shapes", shapes));
        }

        @SubscribeEvent
        @SuppressWarnings("unused") // called by the event bus
        public static void onLeave(ClientPlayerNetworkEvent.LoggingOut e) {
            ClientSettings.reset();
        }

        private static Component line(String key, Component value) {
            return Component.translatable(key, value.copy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY);
        }
    }
}
