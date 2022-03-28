package cope.nebula.util.world.entity;

import cope.nebula.client.Nebula;
import cope.nebula.util.Globals;
import cope.nebula.util.world.BlockUtil;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * Helps with end crystals
 *
 * @author aesthetical
 * @since 3/19/22
 */
public class CrystalUtil implements Globals {
    /**
     * Checks if we can place an end crystal here
     * @param blockPos The position to place at
     * @param protocol If we should take into account being on a newer server
     * @return if we can place an and crystal at this block
     */
    public static boolean canPlaceAt(BlockPos blockPos, boolean protocol) {
        try {
            Block block = mc.world.getBlockState(blockPos).getBlock();
            // if the block the crystal is being placed on is not bedrock or obsidian, we cant place there
            if (!(block == Blocks.OBSIDIAN || block == Blocks.BEDROCK)) {
                return false;
            }

            // we get the position from one up
            BlockPos pos = blockPos.add(0.0, 1.0, 0.0);
            // we get the position from two up
            BlockPos pos1 = blockPos.add(0.0, 2.0, 0.0);

            // if no 1.13 placements and the block above the block isnt air, or the first block above isnt air, we cant place because of non 1.13 placements
            if ((!protocol && mc.world.getBlockState(pos1).getBlock() != Blocks.AIR) || mc.world.getBlockState(pos1).getBlock() != Blocks.AIR) {
                return false;
            }

            // make sure theres no entities to interfere with placements
            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
                if (entity.isDead || entity instanceof EntityEnderCrystal) {
                    continue;
                }

                return false;
            }

            // if no 1.13 place, we have to also check two blocks above
            if (!protocol) {
                for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos1))) {
                    if (entity.isDead || entity instanceof EntityEnderCrystal) {
                        continue;
                    }

                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Places the crystal at this location
     * @param pos The position
     * @param hand The hand to place with
     * @param strictDirection If to place with a stricter direction
     * @param swing If to swing
     * @param rotate If to rotate
     */
    public static void placeAt(BlockPos pos, EnumHand hand, boolean strictDirection, boolean swing, boolean rotate) {
        EnumFacing facing = EnumFacing.UP;
        Vec3d vec = new Vec3d(0.0, 0.0, 0.0);

        if (strictDirection) {
            if (pos.getY() > mc.player.getEntityBoundingBox().minY + mc.player.getEyeHeight()) {
                RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1.0f), new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

                if (result != null) {
                    facing = result.sideHit == null ? EnumFacing.DOWN : result.sideHit;

                    if (result.hitVec != null) {
                        vec = result.hitVec;
                    }
                }
            }
        }

        if (rotate) {
            Rotation rotation = AngleUtil.toBlock(pos, facing);
            if (rotation.isValid()) {
                Nebula.getInstance().getRotationManager().setRotation(rotation);
            }
        }

        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                pos, facing, hand,
                (float) (vec.x - pos.getX()),
                (float) (vec.y - pos.getY()),
                (float) (vec.z - pos.getZ())));

        if (swing) {
            mc.player.swingArm(hand);
        } else {
            mc.player.connection.sendPacket(new CPacketAnimation(hand));
        }
    }

    /**
     * Attacks an end crystal
     * @param crystal The crystal
     * @param hand The hand to attack with
     * @param swing If to swing your hand
     */
    public static void attack(EntityEnderCrystal crystal, EnumHand hand, boolean swing) {
        mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
        if (swing) {
            mc.player.swingArm(hand);
        } else {
            mc.player.connection.sendPacket(new CPacketAnimation(hand));
        }
    }
}
