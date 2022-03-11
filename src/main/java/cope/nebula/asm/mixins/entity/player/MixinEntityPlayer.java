package cope.nebula.asm.mixins.entity.player;

import cope.nebula.client.events.EntityPushedByWaterEvent;
import cope.nebula.util.Globals;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {
    @Inject(method = "isPushedByWater", at = @At("HEAD"), cancellable = true)
    public void isPushedByWater(CallbackInfoReturnable<Boolean> info) {
        EntityPushedByWaterEvent event = new EntityPushedByWaterEvent((EntityPlayer) (Object) this);
        Globals.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }
}
