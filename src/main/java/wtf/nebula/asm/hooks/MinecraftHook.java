package wtf.nebula.asm.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.client.minecraft.GuiOpenedEvent;
import wtf.nebula.client.event.input.MiddleClickEvent;
import wtf.nebula.client.event.tick.TickEvent;
import wtf.nebula.client.event.tick.UnsafeTickEvent;

public class MinecraftHook {
    public static void runTick() {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player != null && mc.world != null) {
            Nebula.Companion.getBUS().post(new TickEvent());
        }

        else {
            Nebula.Companion.getBUS().post(new UnsafeTickEvent());
        }
    }

    public static void guiOpened(GuiScreen opened) {
        Nebula.Companion.getBUS().post(new GuiOpenedEvent(Minecraft.getMinecraft().currentScreen, opened));
    }

    public static void middleClickMouse() {
        Nebula.Companion.getBUS().post(new MiddleClickEvent());
    }
}
