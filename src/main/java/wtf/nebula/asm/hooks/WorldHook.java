package wtf.nebula.asm.hooks;

import net.minecraft.entity.Entity;
import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.world.EntityAddedEvent;
import wtf.nebula.client.event.world.EntityRemovedEvent;

public class WorldHook {
    public static void removeEntityEvent(Entity entity) {
        Nebula.Companion.getBUS().post(new EntityRemovedEvent(entity));
    }

    public static void addedEntityEvent(Entity entity) {
        Nebula.Companion.getBUS().post(new EntityAddedEvent(entity));
    }
}
