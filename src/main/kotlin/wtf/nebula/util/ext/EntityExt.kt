package wtf.nebula.util.ext

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import wtf.nebula.client.registry.RegistryContainer.mc
import wtf.nebula.util.motion.MotionUtil

fun EntityLivingBase.isOutOfRange(range: Double) =
    mc.player.getDistanceSq(this) > range * range

fun EntityPlayerSP.isMoving() = MotionUtil.isMoving()

fun EntityPlayerSP.setMotion(speed: Double) {
    val motion = MotionUtil.strafe(speed)

    motionX = motion[0]
    motionZ = motion[1]
}