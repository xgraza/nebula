package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.client.CPacketPlayer
import wtf.nebula.client.Nebula
import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.event.player.motion.MotionEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class FastFall : Module(ModuleCategory.MOVEMENT, "makes u fall fast - weirdo") {
    val mode by setting("Mode", Mode.MOTION)
    val height by double("Height", 2.0, 1.0..5.0)
    val speed by double("Speed", 1.0, 1.0..5.0)

    private var falling = false

    override fun getDisplayInfo(): String? {
        return Setting.formatEnum(mode)
    }

    override fun onDeactivated() {
        super.onDeactivated()
        Nebula.tickManager.speed = 1.0f
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
//        if (!falling && mode == Mode.TIMER) {
//            Nebula.tickManager.speed = 1.0f
//        }
    }

    @EventListener
    private val motionListener = listener<MotionEvent> {

        if (mc.player.onGround) {
            var y = 0.0
            while (y <= height + 0.5) {
                if (mc.world.getCollisionBoxes(mc.player, mc.player.entityBoundingBox.offset(0.0, -y, 0.0))
                        .isNotEmpty()
                ) {
//                    if (mode == Mode.TIMER) {
//                        Nebula.tickManager.speed = speed.toFloat() * 2.0f
//                    }
//
//                    else {
                        mc.player.motionY = -speed
                        it.y = -speed
//                    }

                    falling = true
                }

                else {
//                    if (mode == Mode.TIMER) {
//                        Nebula.tickManager.speed = 1.0f
//                    }
                }

                y += 0.1
            }
        }

        else {
//            if (falling && mc.player.collidedVertically) {
//                Nebula.tickManager.speed = 1.0f
//            }
        }
    }

    enum class Mode {
        MOTION, TIMER
    }
}