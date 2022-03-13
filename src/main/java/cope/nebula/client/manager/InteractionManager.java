package cope.nebula.client.manager;

import cope.nebula.util.Globals;
import cope.nebula.util.world.BlockUtil;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import cope.nebula.util.world.entity.player.rotation.RotationType;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Handles world interactions (block placements, right clicking, etc)
 *
 * @author aesthetical
 * @since 3/11/22
 */
public class InteractionManager implements Globals {
    /**
     * Places a block
     * @param pos The block position
     * @param hand The hand the block is held in
     * @param rotationType The type of rotation
     * @param swing If to swing client side or server side
     */
    public void placeBlock(BlockPos pos, EnumHand hand, RotationType rotationType, boolean swing) {
        EnumFacing facing = BlockUtil.getFacing(pos);
        if (facing == null) {
            return;
        }

        BlockPos neighbor = pos.offset(facing);

        boolean sneak = BlockUtil.shouldSneak(neighbor);
        if (sneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
        }

        Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(facing.getOpposite().getDirectionVec()).scale(0.5));

        if (!rotationType.equals(RotationType.NONE)) {
            Rotation rotation = AngleUtil.toBlock(neighbor, facing);
            if (rotation.isValid()) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation.getYaw(), rotation.getPitch(), mc.player.onGround));
                getNebula().getRotationManager().setRotation(rotation.setType(rotationType));
            }
        }

        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, facing.getOpposite(), hitVec, hand);

        if (swing) {
            mc.player.swingArm(hand);
        } else {
            mc.player.connection.sendPacket(new CPacketAnimation(hand));
        }

        if (sneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
        }
    }
}
