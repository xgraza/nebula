package cope.nebula.client.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ItemUsedEvent extends Event {
    private final EntityLivingBase entityLiving;
    private final ItemStack stack;

    public ItemUsedEvent(EntityLivingBase entityLiving, ItemStack stack) {
        this.entityLiving = entityLiving;
        this.stack = stack;
    }

    public EntityLivingBase getEntityLiving() {
        return entityLiving;
    }

    public ItemStack getStack() {
        return stack;
    }
}
