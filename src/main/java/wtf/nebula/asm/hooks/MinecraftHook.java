package wtf.nebula.asm.hooks;

import net.minecraft.client.Minecraft;
import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.TickEvent;
import wtf.nebula.client.event.UnsafeTickEvent;

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
}
