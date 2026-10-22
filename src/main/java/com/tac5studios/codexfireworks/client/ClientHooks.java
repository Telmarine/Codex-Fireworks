package com.tac5studios.codexfireworks.client;

import com.tac5studios.codexfireworks.client.screen.PatternScreen;
import com.tac5studios.codexfireworks.client.screen.ShowScreen;
import com.tac5studios.codexfireworks.network.OpenPatternPayload;
import com.tac5studios.codexfireworks.network.OpenShowPayload;
import net.minecraft.client.Minecraft;

/** Opens this mod's screens when the server asks. Only called on the client. */
public final class ClientHooks {

    private ClientHooks() {}

    public static void openPattern(OpenPatternPayload p) {
        Minecraft.getInstance().setScreen(new PatternScreen(p));
    }

    /** Refreshes the open Show Controller screen, or opens a new one. */
    public static void openShow(OpenShowPayload p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof ShowScreen s && s.pos().equals(p.pos())) s.apply(p);
        else mc.setScreen(new ShowScreen(p));
    }
}
