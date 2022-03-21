package cope.nebula.client.events;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

@Cancelable
public class BlockCollisionEvent extends Event {
    private final List<AxisAlignedBB> boxes;
    private final Block block;
    private final BlockPos pos;
    private final Entity entity;
    private AxisAlignedBB box;

    public BlockCollisionEvent(List<AxisAlignedBB> boxes, Block block, BlockPos pos, Entity entity, AxisAlignedBB box) {
        this.boxes = boxes;
        this.block = block;
        this.pos = pos;
        this.entity = entity;
        this.box = box;
    }

    public List<AxisAlignedBB> getBoxes() {
        return boxes;
    }

    public Block getBlock() {
        return block;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Entity getEntity() {
        return entity;
    }

    public AxisAlignedBB getBox() {
        return box;
    }

    public void setBox(AxisAlignedBB box) {
        this.box = box;
    }
}
