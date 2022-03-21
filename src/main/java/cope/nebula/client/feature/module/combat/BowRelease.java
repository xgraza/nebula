package cope.nebula.client.feature.module.combat;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.math.BlockPos;

public class BowRelease extends Module {
    public BowRelease() {
        super("BowRelease", ModuleCategory.COMBAT, "Automatically releases your bow");
    }

    public static final Value<Integer> ticks = new Value<>("Ticks", 3, 3, 20);

    @Override
    public void onTick() {
        if (mc.player.isHandActive() &&
                mc.player.getActiveItemStack().getItem() instanceof ItemBow &&
                mc.player.getItemInUseMaxCount() > ticks.getValue()) {

            mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            mc.player.stopActiveHand();
        }
    }
}
