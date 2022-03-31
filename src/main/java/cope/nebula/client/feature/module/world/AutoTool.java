package cope.nebula.client.feature.module.world;

import cope.nebula.client.events.ClickBlockEvent;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.util.world.entity.player.inventory.SwapType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoTool extends Module {
    public AutoTool() {
        super("AutoTool", ModuleCategory.WORLD, "Automatically swaps to the correct tool");
    }

    @SubscribeEvent
    public void onClickBlock(ClickBlockEvent event) {
        int slot = getBestTool(event.getPos());
        if (slot != -1 && mc.player.inventory.currentItem != slot) {
            getNebula().getHotbarManager().sendSlotChange(slot, SwapType.CLIENT);
        }
    }

    public static int getBestTool(BlockPos pos) {
        IBlockState state = mc.world.getBlockState(pos);

        int slot = -1;
        float speed = 0.0f;

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            float destroySpeed = stack.getDestroySpeed(state);
            if (destroySpeed > speed) {
                speed = destroySpeed;
                slot = i;
            }
        }

        return slot;
    }
}
