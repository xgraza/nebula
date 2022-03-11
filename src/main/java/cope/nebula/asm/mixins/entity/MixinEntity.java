package cope.nebula.asm.mixins.entity;

import cope.nebula.client.events.EntityCollisionEvent;
import cope.nebula.util.Globals;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {
    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    public void applyEntityCollision(Entity entityIn, CallbackInfo info) {
        EntityCollisionEvent event = new EntityCollisionEvent();
        Globals.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }
}
