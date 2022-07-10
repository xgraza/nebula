package wtf.nebula.util.rotation

import net.minecraft.entity.Entity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import wtf.nebula.util.Globals
import kotlin.math.atan2

object AngleUtil : Globals {
    val INVALID_ROTATION = InvalidRotation()

    fun toEntity(entity: Entity?, bone: Bone): Rotation? {
        // check for null entities
        return if (entity == null) {
            INVALID_ROTATION
        } else toVec(getEyes(entity, bone))
    }

    fun toBlock(pos: BlockPos, facing: EnumFacing): Rotation? {
        val x = interpolate(mc.player.posX, mc.player.lastTickPosX)
        val y = interpolate(mc.player.posY, mc.player.lastTickPosY)
        val z = interpolate(mc.player.posZ, mc.player.lastTickPosZ)
        val diff = Vec3d(
            pos.x + 0.5 - x + facing.xOffset / 2.0,
            pos.y + 0.5,
            pos.z + 0.5 - z + facing.zOffset / 2.0
        )
        val distance = MathHelper.sqrt(diff.x * diff.x + diff.z * diff.z).toDouble()
        var yaw = (atan2(diff.z, diff.x) * 180.0 / Math.PI - 90.0).toFloat()
        val pitch = (atan2(y + mc.player.getEyeHeight() - diff.y, distance) * 180.0 / Math.PI).toFloat()
        if (yaw < 0.0f) {
            yaw += 360.0f
        }
        return Rotation(yaw, pitch)
    }

    fun toVec(vec: Vec3d): Rotation? {
        val diff = diff(vec, getEyes(mc.player, Bone.HEAD))
        val distance = MathHelper.sqrt(diff.x * diff.x + diff.z * diff.z).toDouble()
        var yaw = (atan2(diff.z, diff.x) * 180.0 / Math.PI - 90.0).toFloat()
        val pitch = (atan2(diff.y * -1.0, distance) * 180.0 / Math.PI).toFloat()
        if (yaw < 0.0f) {
            yaw += 360.0f
        }
        return Rotation(yaw, pitch)
    }

    fun diff(from: Vec3d, to: Vec3d): Vec3d {
        return Vec3d(from.x - to.x, from.y - to.y, from.z - to.z)
    }

    fun getEyes(entity: Entity, bone: Bone = Bone.HEAD): Vec3d {
        val x = interpolate(entity.posX, entity.prevPosX)
        var y = interpolate(entity.posY, entity.prevPosY)
        val z = interpolate(entity.posZ, entity.prevPosZ)
        when (bone) {
            Bone.HEAD -> {
                y += entity.eyeHeight
            }
            Bone.TORSO -> {
                y += entity.eyeHeight / 2.0 + 0.35
            }
            Bone.LEGS -> {
                y += entity.eyeHeight / 2.0
            }
            Bone.FEET -> {
                y += 0.05
            }
        }
        return Vec3d(x, y, z)
    }

    fun interpolate(start: Double, end: Double): Double {
        val partialTicks: Float = mc.renderPartialTicks
        return if (partialTicks == 1.0f) {
            start
        } else end + (start - end) * partialTicks
    }
}