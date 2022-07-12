package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.client.CPacketPlayer
import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.event.player.motion.MotionEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.motion.MotionUtil
import kotlin.math.cos
import kotlin.math.sin

class Phase : Module(ModuleCategory.MOVEMENT, "funny ncp shit") {
    val mode by setting("Mode", Mode.FREE)

    override fun getDisplayInfo(): String? {
        return Setting.formatEnum(mode)
    }

    override fun onDeactivated() {
        super.onDeactivated()
        mc.player.noClip = false
    }

    @EventListener
    private val motionListener = listener<MotionEvent> {
        if (mode == Mode.FREE) {

            val strafe = MotionUtil.strafe(0.138)

            mc.player.motionX = strafe[0]
            mc.player.motionY = 0.0
            mc.player.motionZ = strafe[1]

            it.x = mc.player.motionX
            it.y = mc.player.motionY
            it.z = mc.player.motionZ

            mc.player.noClip = true
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (mode == Mode.CLIP) {
            val rad = Math.toRadians(mc.player.rotationYaw.toDouble())

            val x = mc.player.posX + 0.056 * -sin(rad)
            val z = mc.player.posZ + 0.056 * cos(rad)

            mc.player.connection.sendPacket(CPacketPlayer.Position(x, mc.player.posY, z, true))
            mc.player.setPosition(x, mc.player.posY, z)
        }
    }

    enum class Mode {
        FREE, CLIP, DOOR
    }
}