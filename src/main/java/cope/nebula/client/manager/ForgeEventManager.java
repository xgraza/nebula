package cope.nebula.client.manager;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.events.PopEvent;
import cope.nebula.client.feature.module.Module;
import cope.nebula.util.Globals;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
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
            for (Module module : getNebula().getModuleManager().getModules()) {
                if (module.isOn()) {
                    module.onTick();
                }
            }

            getNebula().getHoleManager().onTick();
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING)) {
            if (event.getPacket() instanceof SPacketEntityStatus) {
                SPacketEntityStatus packet = event.getPacket();
                if (packet.getOpCode() == 35 && packet.getEntity(mc.world) instanceof EntityPlayer) {
                    EVENT_BUS.post(new PopEvent((EntityPlayer) packet.getEntity(mc.world)));
                }
            }
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
