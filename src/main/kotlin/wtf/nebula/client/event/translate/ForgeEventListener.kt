package wtf.nebula.client.event.translate

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import org.lwjgl.input.Keyboard
import wtf.nebula.asm.hooks.EntityPlayerSPHook
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.packet.PacketSendEvent
import java.lang.Float.isFinite
import java.lang.Double.isFinite

class ForgeEventListener {
    init {
        MinecraftForge.EVENT_BUS.register(this)
        Nebula.BUS.subscribe(this)
    }

    @SubscribeEvent
    fun onKeyInput(event: KeyInputEvent) {
        Nebula.BUS.post(wtf.nebula.client.event.KeyInputEvent(
            Keyboard.getEventKey(), Keyboard.getEventKeyState()))
    }

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {

        if (it.packet is CPacketEntityAction) {
            val packet = it.packet

            if (packet.action == CPacketEntityAction.Action.START_SPRINTING
                || packet.action == CPacketEntityAction.Action.STOP_SPRINTING) {

                EntityPlayerSPHook.playerSprintState = packet.action == CPacketEntityAction.Action.START_SPRINTING
            }

            if (packet.action == CPacketEntityAction.Action.START_SNEAKING
                || packet.action == CPacketEntityAction.Action.STOP_SNEAKING) {

                EntityPlayerSPHook.playerSneakState = packet.action == CPacketEntityAction.Action.START_SNEAKING
            }
        }

//        else if (it.packet is CPacketPlayer) {
//            val packet = it.packet
//
//            EntityPlayerSPHook.lastGround = packet.isOnGround;
//
//            val x = packet.getX(0.0)
//            val y = packet.getY(0.0)
//            val z = packet.getZ(0.0)
//
//            val yaw = packet.getYaw(0.0f)
//            val pitch = packet.getPitch(0.0f)
//
//            if (isFinite(yaw) && isFinite(pitch)) {
//                EntityPlayerSPHook.prevYaw = yaw
//                EntityPlayerSPHook.prevPitch = pitch;
//            }
//
//            if (isFinite(x)) {
//                EntityPlayerSPHook.prevX = x;
//            }
//
//            if (isFinite(y)) {
//                EntityPlayerSPHook.prevY = y;
//            }
//
//            if (isFinite(z)) {
//                EntityPlayerSPHook.prevZ = z;
//            }
//        }
    }
}