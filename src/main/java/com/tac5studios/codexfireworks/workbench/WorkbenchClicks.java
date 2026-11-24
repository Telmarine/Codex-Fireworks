package com.tac5studios.codexfireworks.workbench;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.copy.RocketCopy;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.protect.Protection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Sneak-right-click on a Firework Workbench: with a rocket, it becomes the template the workbench copies;
 * with an empty hand, the template and everything in the workbench's stock come back.
 * Handled here because sneaking with an item in hand skips the block's own click.
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class WorkbenchClicks {

    private WorkbenchClicks() {}

    @SubscribeEvent
    @SuppressWarnings("unused") // called by the event bus
    public static void onRightClick(PlayerInteractEvent.RightClickBlock e) {
        Player player = e.getEntity();
        if (e.getHand() != InteractionHand.MAIN_HAND || !player.isSecondaryUseActive()) return;
        if (!(e.getLevel().getBlockState(e.getPos()).getBlock() instanceof WorkbenchBlock)) return;
        ItemStack held = e.getItemStack();
        if (!held.isEmpty() && !held.is(Items.FIREWORK_ROCKET)) return;
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.sidedSuccess(e.getLevel().isClientSide));
        if (!(player instanceof ServerPlayer sp) || !(e.getLevel().getBlockEntity(e.getPos()) instanceof WorkbenchBlockEntity be)) return;

        if (!Features.on(Feature.WORKBENCH_COPYING)) {
            Msg.send(sp, "workbench.copying_off");
            return;
        }
        if (!Perm.has(sp, Perm.DESIGN)) {
            Msg.send(sp, "general.no_permission");
            return;
        }
        if (!Protection.canEdit(sp, be.owner(), e.getLevel(), e.getPos())) {
            Msg.send(sp, "general.not_yours");
            return;
        }
        if (held.isEmpty()) {
            if (be.template().isEmpty() && be.stock().isEmpty()) {
                Msg.send(sp, "workbench.no_template");
                return;
            }
            for (ItemStack s : be.takeAll()) {
                if (!sp.getInventory().add(s)) sp.drop(s, false);
            }
            Msg.send(sp, "workbench.template_taken");
            return;
        }
        if (!be.template().isEmpty()) {
            Msg.send(sp, "workbench.has_template", "rocket", be.template().getHoverName().getString());
            return;
        }
        if (!RocketCopy.copyable(held)) {
            Msg.send(sp, "workbench.not_copyable");
            return;
        }
        be.setTemplate(held);
        if (!sp.getAbilities().instabuild) held.shrink(1);
        Msg.send(sp, "workbench.template_set", "rocket", be.template().getHoverName().getString());
    }
}
