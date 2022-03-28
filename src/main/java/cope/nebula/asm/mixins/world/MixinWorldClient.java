package cope.nebula.asm.mixins.world;

import cope.nebula.client.events.EntityRemoveEvent;
import cope.nebula.util.Globals;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldClient.class)
public class MixinWorldClient {
    @Inject(method = "onEntityRemoved", at = @At("HEAD"))
    public void onEntityRemoved(Entity entityIn, CallbackInfo info) {
        Globals.EVENT_BUS.post(new EntityRemoveEvent(entityIn));
    }
}
