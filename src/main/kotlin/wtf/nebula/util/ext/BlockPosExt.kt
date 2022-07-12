package wtf.nebula.util.ext

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

fun BlockPos.getHitVecForPlacement(facing: EnumFacing): Vec3d {
    return Vec3d(offset(facing))
        .add(0.5, 0.5, 0.5)
        .add(Vec3d(facing.opposite.directionVec).scale(0.5))
}