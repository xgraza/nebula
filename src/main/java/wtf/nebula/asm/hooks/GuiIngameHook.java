package wtf.nebula.asm.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.render.RenderHUDEvent;

public class GuiIngameHook {
    public static void renderHUD() {
        Nebula.Companion.getBUS().post(new RenderHUDEvent(new ScaledResolution(Minecraft.getMinecraft())));
    }
}
