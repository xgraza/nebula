package cope.nebula.client.feature.module.other;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.player.inventory.InventorySpace;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import cope.nebula.util.world.entity.player.inventory.task.InventoryTask;
import cope.nebula.util.world.entity.player.inventory.task.InventoryTaskHandler;
import net.minecraft.block.Block;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class HotbarRefill extends Module {
    public HotbarRefill() {
        super("HotbarRefill", ModuleCategory.OTHER, "Refills your hotbar");
    }

    public static final Value<Double> percent = new Value<>("Percent", 30.0, 20.0, 70.0);
    public static final Value<Integer> delay = new Value<>("Delay", 2, 0, 10);
    public static final Value<Boolean> await = new Value<>("Await", false);

    private final InventoryTaskHandler taskHandler = new InventoryTaskHandler();

    @Override
    protected void onDeactivated() {
        taskHandler.clean();
    }

    @Override
    public void onTick() {
        if (!taskHandler.execute(delay.getValue() * 50L, await.getValue())) {
            return;
        }

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            int percentage = stack.getCount() / stack.getMaxStackSize();
            if (percentage <= percent.getValue()) {
                refill(i, stack);
            }
        }
    }

    private void refill(int slot, ItemStack stack) {
        int slotId = InventoryUtil.getSlot(InventorySpace.INVENTORY, (s) -> {
            if (s.isEmpty()) {
                return false;
            }

            if (!stack.getDisplayName().equalsIgnoreCase(s.getDisplayName())) {
                return false;
            }

            if (InventoryUtil.isBlock(stack)) {
                if (!InventoryUtil.isBlock(s)) {
                    return false;
                }

                Block hotbarBlock = ((ItemBlock) stack.getItem()).getBlock();
                Block invBlock = ((ItemBlock) s.getItem()).getBlock();

                return hotbarBlock.equals(invBlock);
            } else {
                return stack.getItem().equals(s.getItem());
            }
        });

        if (slotId == -1) {
            return;
        }

        taskHandler.addTask(new InventoryTask(slotId, ClickType.PICKUP));
        taskHandler.addTask(new InventoryTask(InventoryUtil.normalize(slot), ClickType.PICKUP));

        int stackCount = mc.player.inventory.getStackInSlot(slotId).getCount();
        if (stackCount + stack.getCount() > stack.getMaxStackSize()) {
            taskHandler.addTask(new InventoryTask(slotId, ClickType.PICKUP));
        }
    }
}
