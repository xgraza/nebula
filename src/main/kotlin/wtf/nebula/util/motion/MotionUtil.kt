package wtf.nebula.util.motion

import net.minecraft.init.MobEffects
import wtf.nebula.util.Globals
import wtf.nebula.util.rotation.AngleUtil.interpolate
import kotlin.math.cos
import kotlin.math.sin

object MotionUtil : Globals {
    /**
     * Checks if the local player is moving
     * @return if moveForward or moveStrafe does not equal 0
     */
    fun isMoving(): Boolean {
        return mc.player.movementInput.moveForward != 0.0f || mc.player.movementInput.moveStrafe != 0.0f
    }

    /**
     * Calculates strafe motion values
     * @param motionSpeed The speed to strafe
     * @return a Vec2d of the x and z motion values
     */
    fun strafe(motionSpeed: Double): Array<Double> {
        var forward = mc.player.movementInput.moveForward
        var strafe = mc.player.movementInput.moveStrafe
        var yaw = interpolate(
            mc.player.rotationYaw.toDouble(),
            mc.player.prevRotationYaw.toDouble()
        ).toFloat()

        if (forward != 0.0f) {
            if (strafe > 0.0f) {
                yaw += if (forward > 0.0f) -45.0f else 45.0f
            } else if (strafe < 0.0f) {
                yaw += if (forward > 0.0f) 45.0f else -45.0f
            }
            strafe = 0.0f
            if (forward > 0.0f) {
                forward = 1.0f
            } else if (forward < 0.0f) {
                forward = -1.0f
            }
        }

        val rad = Math.toRadians(yaw.toDouble())
        val sin = -sin(rad)
        val cos = cos(rad)

        return arrayOf(
            forward * motionSpeed * sin + strafe * motionSpeed * cos,
            forward * motionSpeed * cos - strafe * motionSpeed * sin
        )
    }

    fun getMovement(): FloatArray {
        var forward: Float = mc.player.movementInput.moveForward
        var strafe: Float = mc.player.movementInput.moveStrafe
        var yaw: Float = mc.player.rotationYaw
        if (forward != 0.0f) {
            if (strafe > 0.0f) {
                yaw += if (forward > 0.0f) -45.0f else 45.0f
            } else if (strafe < 0.0f) {
                yaw += if (forward > 0.0f) 45.0f else -45.0f
            }
            strafe = 0.0f
            if (forward > 0.0f) {
                forward = 1.0f
            } else if (forward < 0.0f) {
                forward = -1.0f
            }
        }
        return floatArrayOf(forward, strafe, yaw)
    }

    fun clampRotation(): Float {
        var rotationYaw = mc.player.rotationYaw
        var n = 1.0f

        if (mc.player.movementInput.moveForward < 0.0f) {
            rotationYaw += 180.0f
            n = -0.5f
        }

        else if (mc.player.movementInput.moveForward > 0.0f) {
            n = 0.5f
        }

        if (mc.player.movementInput.moveStrafe > 0.0f) {
            rotationYaw -= 90.0f * n
        }

        if (mc.player.movementInput.moveStrafe < 0.0f) {
            rotationYaw += 90.0f * n
        }

        return rotationYaw * 0.017453292f
    }

    fun getBaseNCPSpeed(): Double {
        var baseSpeed = 0.2873
        if (mc.player == null) {
            return baseSpeed
        }
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            baseSpeed *= 1.0 + 0.2 * (mc.player.getActivePotionEffect(MobEffects.SPEED)!!.amplifier + 1)
        }
        return baseSpeed
    }

    fun getJumpHeight(strict: Boolean): Double {
        var y = if (strict) 0.41999998688697815 else 0.3995
        if (mc.player == null) {
            return y
        }
        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            y += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST)!!.amplifier + 1) * 0.1
        }
        return y
    }
}