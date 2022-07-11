package wtf.nebula.asm.hooks;

import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.player.motion.StepEvent;

public class EntityHook {
    public static void stepEvent() {
        Nebula.Companion.getBUS().post(new StepEvent());
    }
}
