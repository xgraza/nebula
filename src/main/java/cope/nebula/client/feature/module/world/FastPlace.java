package cope.nebula.client.feature.module.world;

import cope.nebula.asm.mixins.IMinecraft;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumHand;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", ModuleCategory.WORLD, "Places items faster");
    }

    public static final Value<Integer> speed = new Value<>("Speed", 4, 0, 4);

    public static final Value<Boolean> xp = new Value<>("XP", true);
    public static final Value<Boolean> crystals = new Value<>("Crystals", false);
    public static final Value<Boolean> blocks = new Value<>("Blocks", true);

    @Override
    public void onTick() {
        Item item = InventoryUtil.getHeld(EnumHand.MAIN_HAND).getItem();
        if (xp.getValue() && item.equals(Items.EXPERIENCE_BOTTLE) ||
                crystals.getValue() && item.equals(Items.END_CRYSTAL) ||
                blocks.getValue() && item instanceof ItemBlock) {

            ((IMinecraft) mc).setRightClickDelayTimer(4 - speed.getValue());
        }
    }
}
