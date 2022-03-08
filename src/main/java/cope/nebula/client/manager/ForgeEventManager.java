package cope.nebula.client.manager;

import cope.nebula.util.Globals;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

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
}
