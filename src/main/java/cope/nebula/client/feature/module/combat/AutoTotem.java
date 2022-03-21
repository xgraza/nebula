package cope.nebula.client.feature.module.combat;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.EntityUtil;
import cope.nebula.util.world.entity.player.inventory.InventorySpace;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import cope.nebula.util.world.entity.player.inventory.task.InventoryTask;
import cope.nebula.util.world.entity.player.inventory.task.InventoryTaskHandler;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;

public class AutoTotem extends Module {
    public AutoTotem() {
        super("AutoTotem", ModuleCategory.COMBAT, "Manages your offhand");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.CRYSTAL);
    public static final Value<Float> health = new Value<>("Health", 14.0f, 1.0f, 20.0f);
    public static final Value<Integer> delay = new Value<>("Delay", 2, 0, 10);
    public static final Value<Boolean> await = new Value<>("Await", false);
    public static final Value<Boolean> gap = new Value<>("Gap", true);

    private final InventoryTaskHandler taskHandler = new InventoryTaskHandler();
    private Item item;

    @Override
    protected void onActivated() {
        EVENT_BUS.register(taskHandler);
    }

    @Override
    protected void onDeactivated() {
        EVENT_BUS.unregister(taskHandler);
        taskHandler.clean();
    }

    @Override
    public void onTick() {
        if (!taskHandler.execute(delay.getValue() * 50L, await.getValue())) {
            return;
        } else {
            item = null;
        }

        if (willDieUponFall() || EntityUtil.getHealth(mc.player) <= health.getValue()) {
            item = Items.TOTEM_OF_UNDYING;
        } else {
            if (InventoryUtil.getHeld(EnumHand.MAIN_HAND).getItem() instanceof ItemSword &&
                    gap.getValue() &&
                    mc.gameSettings.keyBindUseItem.isKeyDown()) {
                item = Items.GOLDEN_APPLE;
            } else {
                item = mode.getValue().item;
            }
        }

        if (InventoryUtil.getHeld(EnumHand.OFF_HAND).getItem().equals(item)) {
            return;
        }

        int slot = InventoryUtil.getSlot(InventorySpace.BOTH, (s) -> !s.isEmpty() && s.getItem().equals(item));
        if (slot != -1) {
            int packetSlotId = InventoryUtil.normalize(slot);

            taskHandler.addTask(new InventoryTask(packetSlotId, ClickType.PICKUP));
            taskHandler.addTask(new InventoryTask(InventoryUtil.OFFHAND_SLOT, ClickType.PICKUP));
            if (!InventoryUtil.getHeld(EnumHand.OFF_HAND).isEmpty()) {
                taskHandler.addTask(new InventoryTask(packetSlotId, ClickType.PICKUP));
            }
        }
    }

    /**
     * @return if given the fall damage, we will die
     */
    private boolean willDieUponFall() {
        return ((mc.player.fallDistance - 3.0f) / 2.0f) + 3.5f >= EntityUtil.getHealth(mc.player);
    }

    public enum Mode {
        TOTEM(Items.TOTEM_OF_UNDYING),
        GAPPLE(Items.GOLDEN_APPLE),
        CRYSTAL(Items.END_CRYSTAL),
        EXP(Items.EXPERIENCE_BOTTLE);

        private final Item item;

        Mode(Item item) {
            this.item = item;
        }
    }
}
