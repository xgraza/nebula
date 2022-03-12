package cope.nebula.client.feature.module.combat;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.player.inventory.InventorySpace;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import cope.nebula.util.world.entity.player.inventory.SwapType;
import cope.nebula.util.world.entity.player.rotation.RotationType;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class SelfFill extends Module {
    /**
     * Vanilla packet offsets for a fake jump
     */
    private static final double[] FAKE_JUMP = { 0.41999998688698, 0.7531999805211997, 1.00133597911214, 1.16610926093821 };

    public SelfFill() {
        super("SelfFill", ModuleCategory.COMBAT, "Lags you back into a block");
    }

    public static final Value<BlockType> mode = new Value<>("Mode", BlockType.OBSIDIAN);
    public static final Value<SwapType> swap = new Value<>("Swap", SwapType.SERVER);
    public static final Value<RotationType> rotate = new Value<>("Rotate", RotationType.SERVER);
    public static final Value<Boolean> swing = new Value<>("Swing", true);
    public static final Value<Boolean> flag = new Value<>("Flag", true);

    @Override
    protected void onActivated() {
        BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        // if we are already burrowed
        if (!mc.world.getCollisionBoxes(mc.player, new AxisAlignedBB(pos)).isEmpty()) {
            disable();
            return;
        }

        int slot = InventoryUtil.getSlot(InventorySpace.HOTBAR,
                (s) -> s.getItem() instanceof ItemBlock && ((ItemBlock) s.getItem()).getBlock().equals(mode.getValue().block));

        if (slot == -1) {
            disable();
            return;
        }

        EnumHand hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
        int oldSlot = -1;

        if (hand.equals(EnumHand.MAIN_HAND)) {
            oldSlot = mc.player.inventory.currentItem;
            getNebula().getHotbarManager().sendSlotChange(slot, swap.getValue());
        }

        for (double offset : FAKE_JUMP) {
            mc.player.connection.sendPacket(new Position(mc.player.posX, pos.getY() + offset, mc.player.posZ, false));
        }

        getNebula().getInteractionManager().placeBlock(pos, hand, rotate.getValue(), swing.getValue());

        if (flag.getValue()) {
            mc.player.connection.sendPacket(new Position(mc.player.posX, pos.getY() + 3.0, mc.player.posZ, false));
        } else {
            if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
                mc.player.setPosition(pos.getX(), pos.getY() + 1.0, pos.getZ());
            }
        }

        if (oldSlot != -1) {
            getNebula().getHotbarManager().sendSlotChange(oldSlot, swap.getValue());
        }

        disable();
    }

    public enum BlockType {
        OBSIDIAN(Blocks.OBSIDIAN),
        END_ROD(Blocks.END_ROD),
        ECHEST(Blocks.ENDER_CHEST);

        private final Block block;

        BlockType(Block block) {
            this.block = block;
        }
    }
}
