package cope.nebula.asm.mixins.world;

import cope.nebula.client.events.EntityAddedEvent;
import cope.nebula.client.events.EntityRemoveEvent;
import cope.nebula.util.Globals;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient extends World {
    protected MixinWorldClient(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Inject(method = "onEntityAdded", at = @At("HEAD"))
    public void onEntityAdded(Entity entityIn, CallbackInfo info) {
        Globals.EVENT_BUS.post(new EntityAddedEvent(entityIn));
    }

    @Inject(method = "onEntityRemoved", at = @At("HEAD"))
    public void onEntityRemoved(Entity entityIn, CallbackInfo info) {
        Globals.EVENT_BUS.post(new EntityRemoveEvent(entityIn));
    }
}
