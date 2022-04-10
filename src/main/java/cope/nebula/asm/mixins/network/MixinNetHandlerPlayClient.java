package cope.nebula.asm.mixins.network;

import cope.nebula.client.events.DeathEvent;
import cope.nebula.util.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements INetHandlerPlayClient {
    @Shadow private Minecraft client;

    @Inject(method = "handleEntityMetadata", at = @At("RETURN"))
    public void handleEntityMetadata(SPacketEntityMetadata packetIn, CallbackInfo info) {
        if (client.world != null && client.player != null) {
            Entity entity = client.world.getEntityByID(packetIn.getEntityId());
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (player.getHealth() <= 0.0f || player.isDead) {
                    Globals.EVENT_BUS.post(new DeathEvent(player));
                }
            }
        }
    }
}
