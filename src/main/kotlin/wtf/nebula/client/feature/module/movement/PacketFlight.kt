package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.Vec3d
import wtf.nebula.client.event.packet.PacketReceiveEvent
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.client.event.player.motion.MotionEvent
import wtf.nebula.client.event.player.motion.update.PreMotionUpdate
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import kotlin.math.floor
import kotlin.math.sqrt

class PacketFlight : Module(ModuleCategory.MOVEMENT, "Funny mojang") {
    val mode by setting("Mode", Mode.FACTOR)
    val bounds by setting("Bounds", Bounds.DOWN)
    val phase by setting("Phase", Phase.NCP)
    val factor by double("Factor", 1.5, 0.1..5.0)
    val conceal by bool("Conceal", false)
    val frequency by bool("Frequency", true)
    val antiKick by bool("Anti-Kick", true)

    private val movements = mutableSetOf<CPacketPlayer>()
    private val teleports = mutableMapOf<Int, Prediction>()
    private var teleportId = -1

    private var flagTicks = 0
    private var moveSpeed = 0.0

    override fun onDeactivated() {
        super.onDeactivated()
        teleports.clear()
        teleportId = -1
    }

    @EventListener
    private val preMotionListener = listener<PreMotionUpdate> {

    }

    @EventListener
    private val motionListener = listener<MotionEvent> {
        moveSpeed = if (conceal) CONCEAL else 0.2873

        var motionY = 0.0
        val shouldAntiKick = antiKick && !isPhased() && mc.player.ticksExisted % 40 == 0 && !mc.player.onGround

        if (mc.gameSettings.keyBindJump.isKeyDown) {
            motionY = BASE
        }

        else if (mc.gameSettings.keyBindSneak.isKeyDown) {
            motionY = -BASE
        }

        else {
            if (shouldAntiKick) {
                motionY = -0.04
            }
        }

        var loops = floor(factor).toInt()



        it.x = mc.player.motionX
        it.y = motionY
        it.z = mc.player.motionZ

        mc.player.noClip = true
    }

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {
        if (it.packet is CPacketPlayer) {
            val packet = it.packet
            if (!movements.remove(packet)) {
                it.cancel()
            }
        }
    }

    @EventListener
    private val packetReceiveListener = listener<PacketReceiveEvent> {
        if (it.packet is SPacketPlayerPosLook) {
            val packet = it.packet

            val teleport = teleports[packet.teleportId]
            if (teleport != null) {

                // funny
                if (packet.x == teleport.vec.x && packet.y == teleport.vec.y && packet.z == teleport.vec.z) {
                    it.cancel()

                    if (frequency) {
                        mc.player.connection.sendPacket(CPacketConfirmTeleport(packet.teleportId))
                    }
                }
            }

            else {

                // didn't predict in time :(
                flagTicks = 20
            }

            // fuck you
            packet.yaw = mc.player.rotationYaw
            packet.pitch = mc.player.rotationPitch
        }
    }

    private fun isPhased(): Boolean =
        mc.world.getCollisionBoxes(
            mc.player,
            mc.player.entityBoundingBox.expand(
                -0.0625, -0.0625, -0.0625)).isNotEmpty()

    enum class Mode {
        FACTOR, SETBACK
    }

    enum class Bounds(val x: Double, val y: Double, val z: Double) {
        DOWN(0.0, -1337.69, 0.0),
        UP(0.0, 1337.69, 0.0),
        PRESERVE(101.0, 0.0, -101.0)
    }

    enum class Phase {
        NONE, VANILLA, NCP
    }

    companion object {
        private const val BASE = 0.0624
        private val CONCEAL = 1.0 / sqrt(2.0)
    }
}

data class Prediction(val vec: Vec3d, val time: Long)