package cope.nebula.client.events;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class BlockInteractEvent extends Event {
    private final BlockPos pos;
    private final Block block;
    private final Vec3d hitVec;
    private final EnumFacing direction;
    private final EnumHand hand;

    public BlockInteractEvent(BlockPos pos, Block block, Vec3d hitVec, EnumFacing direction, EnumHand hand) {
        this.pos = pos;
        this.block = block;
        this.hitVec = hitVec;
        this.direction = direction;
        this.hand = hand;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Block getBlock() {
        return block;
    }

    public Vec3d getHitVec() {
        return hitVec;
    }

    public EnumFacing getDirection() {
        return direction;
    }

    public EnumHand getHand() {
        return hand;
    }
}
