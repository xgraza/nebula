package cope.nebula.asm.mixins.network;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.util.Globals;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void channelRead0(ChannelHandlerContext ctx, Packet<?> packetIn, CallbackInfo info) {
        PacketEvent event = new PacketEvent(packetIn, Direction.INCOMING);
        Globals.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void sendPacket(Packet<?> packetIn, CallbackInfo info) {
        PacketEvent event = new PacketEvent(packetIn, Direction.OUTGOING);
        Globals.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }
}
