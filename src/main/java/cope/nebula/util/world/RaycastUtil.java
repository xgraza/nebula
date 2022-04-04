package cope.nebula.util.world;

import cope.nebula.util.Globals;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;

/**
 * A utility for raycasting (raytracing)
 *
 * @author aesthetical
 * @since 3/28/22
 */
public class RaycastUtil implements Globals {
    private static final Frustum FRUSTUM = new Frustum();

    /**
     * Checks if a bounding box is in our frustum
     * @param box the bounding box
     * @return if it is within our frustum
     */
    public static boolean isInFrustum(AxisAlignedBB box) {
        if (mc.getRenderViewEntity() == null) {
            return false;
        }

        Entity renderViewEntity = mc.getRenderViewEntity();
        FRUSTUM.setPosition(renderViewEntity.posX, renderViewEntity.posY, renderViewEntity.posZ);

        return FRUSTUM.isBoundingBoxInFrustum(box);
    }

    /**
     * Checks if we can see this block
     * @param pos the position
     * @return if the block is visible within our line of sight
     */
    public static boolean isBlockVisible(BlockPos pos) {
        return mc.world.rayTraceBlocks(
                new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                false,
                true,
                false) != null;
    }

    /**
     * Gets the block density
     * @param ignoreTerrain if to ignore terrain blocks
     * @param vec the position of the block
     * @param bb the bounding box
     * @return the density of the block
     */
    public static double getBlockDensity(boolean ignoreTerrain, Vec3d vec, AxisAlignedBB bb) {
        double d0 = 1.0D / ((bb.maxX - bb.minX) * 2.0D + 1.0D);
        double d1 = 1.0D / ((bb.maxY - bb.minY) * 2.0D + 1.0D);
        double d2 = 1.0D / ((bb.maxZ - bb.minZ) * 2.0D + 1.0D);
        double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;

        if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
            float solid = 0;
            float nonSolid = 0;

            for (double x = 0.0; x <= 1.0; x = x + d0) {
                for (double y = 0.0; y <= 1.0; y = y + d1) {
                    for (double z = 0.0; z <= 1.0; z = z + d2) {
                        double d5 = bb.minX + (bb.maxX - bb.minX) * x;
                        double d6 = bb.minY + (bb.maxY - bb.minY) * y;
                        double d7 = bb.minZ + (bb.maxZ - bb.minZ) * z;

                        if (!rayTraceSolidCheck(new Vec3d(d5 + d3, d6, d7 + d4), vec, ignoreTerrain)) {
                            ++solid;
                        }

                        ++nonSolid;
                    }
                }
            }

            return solid / nonSolid;
        } else {
            return 0.0;
        }
    }

    /**
     * A wrapper for vanilla raytracing with ignoreTerrain
     * @param start the starting position
     * @param end the ending position
     * @param ignoreTerrain if to ignore terrain blocks
     * @return if the raytrace found a solid block
     */
    public static boolean rayTraceSolidCheck(Vec3d start, Vec3d end, boolean ignoreTerrain) {
        if (!Double.isNaN(start.x) && !Double.isNaN(start.y) && !Double.isNaN(start.z)) {
            if (!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z)) {
                int currX = MathHelper.floor(start.x);
                int currY = MathHelper.floor(start.y);
                int currZ = MathHelper.floor(start.z);

                int endX = MathHelper.floor(end.x);
                int endY = MathHelper.floor(end.y);
                int endZ = MathHelper.floor(end.z);

                BlockPos blockPos = new BlockPos(currX, currY, currZ);
                IBlockState blockState = mc.world.getBlockState(blockPos);
                Block block = blockState.getBlock();

                if ((blockState.getCollisionBoundingBox(mc.world, blockPos) != Block.NULL_AABB) && block.canCollideCheck(blockState, false) && (BlockUtil.EXPLOSION_RESISTANT_BLOCKS.contains(block) || !ignoreTerrain)) {
                    RayTraceResult collisionInterCheck = blockState.collisionRayTrace(mc.world, blockPos, start, end);
                    if (collisionInterCheck != null) {
                        return true;
                    }
                }

                double seDeltaX = end.x - start.x;
                double seDeltaY = end.y - start.y;
                double seDeltaZ = end.z - start.z;

                int steps = 200;

                while (steps-- >= 0) {
                    if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
                        return false;
                    }

                    if (currX == endX && currY == endY && currZ == endZ) {
                        return false;
                    }

                    boolean unboundedX = true;
                    boolean unboundedY = true;
                    boolean unboundedZ = true;

                    double stepX = 999.0;
                    double stepY = 999.0;
                    double stepZ = 999.0;
                    double deltaX = 999.0;
                    double deltaY = 999.0;
                    double deltaZ = 999.0;

                    if (endX > currX) {
                        stepX = currX + 1.0;
                    } else if (endX < currX) {
                        stepX = currX;
                    } else {
                        unboundedX = false;
                    }

                    if (endY > currY) {
                        stepY = currY + 1.0;
                    } else if (endY < currY) {
                        stepY = currY;
                    } else {
                        unboundedY = false;
                    }

                    if (endZ > currZ) {
                        stepZ = currZ + 1.0;
                    } else if (endZ < currZ) {
                        stepZ = currZ;
                    } else {
                        unboundedZ = false;
                    }

                    if (unboundedX) {
                        deltaX = (stepX - start.x) / seDeltaX;
                    }

                    if (unboundedY) {
                        deltaY = (stepY - start.y) / seDeltaY;
                    }

                    if (unboundedZ) {
                        deltaZ = (stepZ - start.z) / seDeltaZ;
                    }

                    if (deltaX == 0.0) {
                        deltaX = -1.0e-4;
                    }

                    if (deltaY == 0.0) {
                        deltaY = -1.0e-4;
                    }

                    if (deltaZ == 0.0) {
                        deltaZ = -1.0e-4;
                    }

                    EnumFacing facing;

                    if (deltaX < deltaY && deltaX < deltaZ) {
                        facing = endX > currX ? EnumFacing.WEST : EnumFacing.EAST;
                        start = new Vec3d(stepX, start.y + seDeltaY * deltaX, start.z + seDeltaZ * deltaX);
                    } else if (deltaY < deltaZ) {
                        facing = endY > currY ? EnumFacing.DOWN : EnumFacing.UP;
                        start = new Vec3d(start.x + seDeltaX * deltaY, stepY, start.z + seDeltaZ * deltaY);
                    } else {
                        facing = endZ > currZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        start = new Vec3d(start.x + seDeltaX * deltaZ, start.y + seDeltaY * deltaZ, stepZ);
                    }

                    currX = MathHelper.floor(start.x) - (facing == EnumFacing.EAST ? 1 : 0);
                    currY = MathHelper.floor(start.y) - (facing == EnumFacing.UP ? 1 : 0);
                    currZ = MathHelper.floor(start.z) - (facing == EnumFacing.SOUTH ? 1 : 0);

                    blockPos = new BlockPos(currX, currY, currZ);
                    blockState = mc.world.getBlockState(blockPos);
                    block = blockState.getBlock();

                    if (block.canCollideCheck(blockState, false) && (BlockUtil.EXPLOSION_RESISTANT_BLOCKS.contains(block) || !ignoreTerrain)) {
                        RayTraceResult collisionInterCheck = blockState.collisionRayTrace(mc.world, blockPos, start, end);
                        if (collisionInterCheck != null) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
