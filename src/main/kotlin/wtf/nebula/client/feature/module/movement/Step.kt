package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.client.CPacketPlayer
import wtf.nebula.client.Nebula
import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.event.player.motion.StepEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class Step : Module(ModuleCategory.MOVEMENT, "Steps up blocks") {
    val mode by setting("Mode", Mode.NCP)
    val useTimer by setting("Timer", true)
    val height by double("Height", 1.0, 0.7..2.5)

    private var timer = false

    override fun getDisplayInfo(): String? {
        if (mode == Mode.NCP) {
            return "NCP"
        }

        return Setting.formatEnum(mode)
    }

    override fun onDeactivated() {
        super.onDeactivated()

        mc.player.stepHeight = 0.6f

        Nebula.serverManager.speed = 1.0f
        timer = false
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (timer && mc.player.onGround) {
            Nebula.serverManager.speed = 1.0f
        }

        mc.player.stepHeight = height.toFloat()
    }

    @EventListener
    private val stepListener = listener<StepEvent> {
        if (mode == Mode.VANILLA) {
            return@listener
        }

        val stepHeight = mc.player.entityBoundingBox.minY - mc.player.posY
        if (stepHeight > height) {
            return@listener
        }

        val packets = STEP_PACKETS[stepHeight] ?: return@listener

        if (useTimer) {
            Nebula.serverManager.speed = 1.0f / (packets.size + 1.0f)
            timer = true
        }

        packets.forEach {
            mc.player.connection.sendPacket(CPacketPlayer.Position(
                mc.player.posX, mc.player.posY + it, mc.player.posZ, false))
        }
    }

    enum class Mode {
        NCP, VANILLA
    }

    companion object {
        val STEP_PACKETS = mapOf(
            0.75 to arrayOf(0.39, 0.753, 0.75),
            0.8125 to arrayOf(0.39, 0.7, 0.8125),
            0.875 to arrayOf(0.39, 0.7, 0.875),
            1.0 to arrayOf(0.42, 0.753, 1.0),

            // im gonna work on strict 1.5 step later, i'll keep old ncp here for now tho
            // 1.5 to arrayOf(0.42, 0.753, 1.0, 1.16, 1.22, 1.25, 1.28, 1.32),
            1.5 to arrayOf(0.42, 0.75, 1.0, 1.16, 1.23, 1.2, 1.5),
            2.0 to arrayOf(0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43),
            2.5 to arrayOf(0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907)
        )
    }
}