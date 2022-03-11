package cope.nebula.client.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EntityPushedByWaterEvent extends Event {
    private final EntityPlayer player;

    public EntityPushedByWaterEvent(EntityPlayer player) {
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
