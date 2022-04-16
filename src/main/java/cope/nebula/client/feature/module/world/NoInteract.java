package cope.nebula.client.feature.module.world;

import cope.nebula.client.events.BlockInteractEvent;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoInteract extends Module {
    public NoInteract() {
        super("NoInteract", ModuleCategory.WORLD, "Stops you from interacting with tile entities");
    }

    public static final Value<Boolean> chests = new Value<>("Chests", false);
    public static final Value<Boolean> ender = new Value<>("Ender", false);
    public static final Value<Boolean> trapped = new Value<>("Trapped", false);

    public static final Value<Boolean> anvils = new Value<>("Anvils", true);
    public static final Value<Boolean> enchantment = new Value<>("Enchantment", false);

    @SubscribeEvent
    public void onBlockInteract(BlockInteractEvent event) {
        // so that we can sneak and place blocks still (TODO: check if we're sneaking server-side)
        if (!mc.player.isSneaking()) {
            Block block = event.getBlock();

            // i hate this so much ewwwww
            event.setCanceled((chests.getValue() && block.equals(Blocks.CHEST)) ||
                    (ender.getValue() && block.equals(Blocks.ENDER_CHEST)) ||
                    (trapped.getValue() && block.equals(Blocks.TRAPPED_CHEST)) ||
                    (anvils.getValue() && block.equals(Blocks.ANVIL)) ||
                    (enchantment.getValue() && block.equals(Blocks.ENCHANTING_TABLE)));
        }
    }
}
