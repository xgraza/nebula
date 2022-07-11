package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.Nebula
import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.event.player.SetbackEvent
import wtf.nebula.client.event.player.motion.MotionEvent
import wtf.nebula.client.event.player.motion.update.PreMotionUpdate
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.ext.isMoving
import wtf.nebula.util.motion.MotionUtil
import java.lang.Double.max
import kotlin.math.sqrt

class Speed : Module(ModuleCategory.MOVEMENT, "vroom vroom") {
    val mode by setting("Mode", Mode.STRAFE)
    val timerBoost by bool("Timer Boost", true)
    val damageBoost by bool("Damage Boost", true)

    private var stage = StrafeStage.COLLIDE

    private var moveSpeed = 0.0
    private var distance = 0.0
    private var boost = false

    private var lagTicks = 0

    override fun getDisplayInfo(): String {
        return when (mode) {
            Mode.STRICT_STRAFE -> "Strict Strafe"
            Mode.YPORT -> "YPort"
            Mode.ONGROUND -> "OnGround"
            else -> Setting.formatEnum(mode)
        }
    }

    override fun onDeactivated() {
        super.onDeactivated()

        stage = StrafeStage.COLLIDE
        moveSpeed = 0.0
        distance = 0.0
        boost = false

        mc.timer.tickLength = 50.0f

        lagTicks = 0
    }

    @EventListener
    private val motionListener = listener<MotionEvent> {
        --lagTicks
        if (lagTicks > 0) {
            return@listener
        }

        when (mode) {
            Mode.STRAFE, Mode.STRICT_STRAFE -> {

                if (mc.player.onGround && mc.player.isMoving()) {
                    stage = StrafeStage.JUMP
                }

                when (stage) {
                    StrafeStage.START -> {
                        if (mc.player.isMoving()) {
                            stage = StrafeStage.JUMP
                            moveSpeed = (if (mode == Mode.STRAFE) 1.38 else 1.35) * MotionUtil.getBaseNCPSpeed() - 0.01
                        }

                        Nebula.tickManager.reset()
                    }

                    StrafeStage.JUMP -> {
                        stage = StrafeStage.SPEED

                        if (mc.player.onGround && mc.player.isMoving()) {
                            mc.player.motionY = MotionUtil.getJumpHeight(mode == Mode.STRICT_STRAFE)
                            it.y = mc.player.motionY

                            moveSpeed *= if (boost) {
                                1.608
                            } else {
                                1.345
                            }

                            if (timerBoost) {
                                Nebula.tickManager.speed = 1.088f
                            }
                        }
                    }

                    StrafeStage.SPEED -> {
                        stage = StrafeStage.COLLIDE

                        val adjusted = (if (mode == Mode.STRAFE) 0.66 else 0.72) * (distance - MotionUtil.getBaseNCPSpeed())
                        moveSpeed = distance - adjusted

                        boost = !boost
                    }

                    StrafeStage.COLLIDE -> {
                        val boxes = mc.world.getCollisionBoxes(mc.player, mc.player.entityBoundingBox.offset(0.0, mc.player.motionY, 0.0))
                        if ((boxes.isNotEmpty() && mc.player.collidedVertically) && stage != StrafeStage.START) {
                            stage = StrafeStage.START
                        }

                        moveSpeed -= moveSpeed / 159.0
                    }
                }

                moveSpeed = max(moveSpeed, MotionUtil.getBaseNCPSpeed())

                val strafe = MotionUtil.strafe(moveSpeed)

                if (mc.player.isMoving()) {
                    mc.player.motionX = strafe[0]
                    mc.player.motionZ = strafe[1]
                }

                else {
                    mc.player.motionX = 0.0
                    mc.player.motionZ = 0.0
                }

                it.x = mc.player.motionX
                it.z = mc.player.motionZ
            }

            else -> return@listener
        }
    }

    @EventListener
    private val preMotionUpdate = listener<PreMotionUpdate> {
        val diffX = mc.player.posX - mc.player.prevPosX
        val diffZ = mc.player.posZ - mc.player.prevPosZ

        distance = sqrt(diffX * diffX + diffZ * diffZ)
    }

    @EventListener
    private val setBackListener = listener<SetbackEvent> {
        mc.timer.tickLength = 50.0f
        lagTicks = 10
    }

    enum class Mode {
        STRAFE, STRICT_STRAFE, YPORT, ONGROUND
    }

    enum class StrafeStage {
        START, JUMP, SPEED, COLLIDE
    }
}