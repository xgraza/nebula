package wtf.nebula.asm.hooks;

import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.render.RenderWorldEvent;

public class EntityRendererHook {
    public static void renderWorld() {
        Nebula.Companion.getBUS().post(new RenderWorldEvent());
    }
}
