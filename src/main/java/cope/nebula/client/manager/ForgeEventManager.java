package cope.nebula.client.manager;

import cope.nebula.util.Globals;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Text;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import static org.lwjgl.opengl.GL11.GL_FLAT;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;

/**
 * A manager that listens for events and will either emit custom events or call methods
 *
 * @author aesthetical
 * @since 3/7/22
 */
public class ForgeEventManager implements Globals {
    public ForgeEventManager() {
        EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (!nullCheck()) {
            getNebula().getModuleManager().getModules().forEach((module) -> {
                if (module.isOn()) {
                    mc.profiler.startSection("module_clienttick_" + module.getName());
                    module.onTick();
                    mc.profiler.endSection();
                }
            });

            getNebula().getHoleManager().onTick();
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (!nullCheck() && event.getEntityLiving() != null && event.getEntityLiving().equals(mc.player)) {
            getNebula().getModuleManager().getModules().forEach((module) -> {
                if (module.isOn()) {
                    mc.profiler.startSection("module_livingupdate_" + module.getName());
                    module.onUpdate();
                    mc.profiler.endSection();
                }
            });
        }
    }

    @SubscribeEvent
    public void onRenderText(Text event) {
        getNebula().getModuleManager().getModules().forEach((module) -> {
            if (module.isOn()) {
                mc.profiler.startSection("module_renderhud_" + module.getName());
                module.onRender2d();
                mc.profiler.endSection();
            }
        });
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.shadeModel(GL_SMOOTH);
        GlStateManager.disableDepth();

        getNebula().getModuleManager().getModules().forEach((module) -> {
            if (module.isOn()) {
                mc.profiler.startSection("module_renderworld_" + module.getName());
                module.onRender3d();
                mc.profiler.endSection();
            }
        });

        GlStateManager.glLineWidth(1.0f);
        GlStateManager.shadeModel(GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
    }
}
