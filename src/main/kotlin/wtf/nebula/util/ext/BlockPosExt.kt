package wtf.nebula.util.ext

import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import wtf.nebula.client.feature.guis.common.Component.Companion.mc

fun BlockPos.getHitVecForPlacement(facing: EnumFacing): Vec3d {
    return Vec3d(offset(facing))
        .add(0.5, 0.5, 0.5)
        .add(Vec3d(facing.opposite.directionVec).scale(0.5))
}

fun BlockPos.getSphere(radius: Int): List<BlockPos> {
    val positions = mutableListOf<BlockPos>()

    var x = -radius
    while (x < radius) {
        var y = -radius
        while (y < radius) {
            var z = -radius
            while (z < radius) {
                val blockPos = add(x, y, z)

                val dist = mc.player.getDistanceSq(blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5)
                if (dist < radius * radius) {
                    positions.add(blockPos)
                }

                ++z
            }

            ++y
        }

        ++x
    }

    return positions
}

fun BlockPos.isReplaceable(): Boolean
    = mc.world.getBlockState(this).material.isReplaceable

fun BlockPos.canPlaceCrystal(updated: Boolean, entityCheck: Boolean = true): Boolean {
    val blockAt = mc.world.getBlockState(this).block
    if (blockAt == Blocks.OBSIDIAN || blockAt == Blocks.BEDROCK) {
        var above = up()
        if (!above.isReplaceable()) {
            return false
        }

        if (entityCheck) {

            try {
                for (entity in mc.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(above))) {
                    if (entity == null || entity.isDead || entity is EntityEnderCrystal) {
                        continue
                    }

                    return false
                }
            } catch (ignored: Exception) {
                return true
            }
        }

        if (!updated) {
            above = above.up()
            if (!above.isReplaceable()) {
                return false
            }
        }

        return true
    } else return false
}