package wtf.nebula.asm.hooks;

import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.player.motion.StepEvent;
import wtf.nebula.client.event.world.PushOutOfBlocksEvent;

public class EntityHook {
    public static void stepEvent() {
        Nebula.Companion.getBUS().post(new StepEvent());
    }

    public static boolean pushEntityOutOfBlocks() {
        PushOutOfBlocksEvent event = new PushOutOfBlocksEvent();
        Nebula.Companion.getBUS().post(event);
        System.out.println(event.getCancelled());
        return event.getCancelled();
    }
}
