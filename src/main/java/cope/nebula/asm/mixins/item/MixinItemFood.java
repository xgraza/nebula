package cope.nebula.asm.mixins.item;

import cope.nebula.client.events.ItemUsedEvent;
import cope.nebula.util.Globals;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFood.class)
public class MixinItemFood {
    @Inject(method = "onItemUseFinish", at = @At("HEAD"))
    public void onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving, CallbackInfoReturnable<ItemStack> info) {
        Globals.EVENT_BUS.post(new ItemUsedEvent(entityLiving, stack));
    }
}
