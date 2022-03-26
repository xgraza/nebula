package cope.nebula.client.feature.module.combat;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import cope.nebula.util.world.entity.player.inventory.task.InventoryTask;
import cope.nebula.util.world.entity.player.inventory.task.InventoryTaskHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class AutoArmor extends Module {
    public AutoArmor() {
        super("AutoArmor", ModuleCategory.COMBAT, "Automatically equips armor");
    }

    public static final Value<Integer> delay = new Value<>("Delay", 2, 0, 10);
    public static final Value<Boolean> await = new Value<>("Await", false);
    public static final Value<Boolean> noBinding = new Value<>("NoBinding", true);

    private final InventoryTaskHandler taskHandler = new InventoryTaskHandler();
    private final int[] bestArmor = { -1, -1, -1, -1 };

    @Override
    protected void onDeactivated() {
        taskHandler.clean();
        Arrays.fill(bestArmor, -1);
    }

    @Override
    public void onTick() {
        if (!taskHandler.execute(delay.getValue() * 50L, await.getValue())) {
            return;
        }

        for (int i = 0; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemArmor)) {
                continue;
            }

            if (noBinding.getValue() && EnchantmentHelper.hasBindingCurse(stack)) {
                continue;
            }

            ItemArmor armor = (ItemArmor) stack.getItem();
            int armorIndex = armor.armorType.getIndex();

            ItemStack current = mc.player.inventory.armorInventory.get(armorIndex);
            int damageReduceAmount = current.getItem() instanceof ItemArmor ? ((ItemArmor) current.getItem()).damageReduceAmount : -1;

            if (armor.damageReduceAmount > damageReduceAmount) {
                bestArmor[armorIndex] = i;
            }
        }

        for (int i = 0; i < 4; ++i) {
            int slot = bestArmor[i];
            if (slot == -1) {
                continue;
            }

            taskHandler.addTask(new InventoryTask(InventoryUtil.normalize(slot), ClickType.PICKUP));
            taskHandler.addTask(new InventoryTask(8 - i, ClickType.PICKUP));

            if (!mc.player.inventory.armorInventory.get(i).isEmpty()) {
                taskHandler.addTask(new InventoryTask(InventoryUtil.normalize(slot), ClickType.PICKUP));
            }

            bestArmor[i] = -1;
        }
    }
}
