package cope.nebula.client.feature.module.combat;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.player.inventory.task.InventoryTaskHandler;

public class AutoArmor extends Module {
    public AutoArmor() {
        super("AutoArmor", ModuleCategory.COMBAT, "Automatically equips armor");
    }

    public static final Value<Integer> delay = new Value<>("Delay", 2, 0, 10);
    public static final Value<Boolean> await = new Value<>("Await", false);
    public static final Value<Boolean> noBinding = new Value<>("NoBinding", true);

    private final InventoryTaskHandler taskHandler = new InventoryTaskHandler();

}
