package wtf.nebula.asm.hooks;

import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.render.HurtCameraRenderEvent;
import wtf.nebula.client.event.render.RenderItemActivationEvent;
import wtf.nebula.client.event.render.RenderWorldEvent;
import wtf.nebula.client.registry.impl.ModuleRegistry;

public class EntityRendererHook {
    public static void renderWorld() {
        Nebula.Companion.getBUS().post(new RenderWorldEvent());
    }

    public static boolean renderHurtcam() {
        HurtCameraRenderEvent event = new HurtCameraRenderEvent();
        Nebula.Companion.getBUS().post(event);
        return event.getCancelled();
    }

    public static boolean displayItemActivation() {
        RenderItemActivationEvent event = new RenderItemActivationEvent();
        Nebula.Companion.getBUS().post(event);
        return event.getCancelled();
    }

    public static boolean shouldRenderTag() {
        return ModuleRegistry.Companion.getINSTANCE().isOn("Nametags");
    }
}
