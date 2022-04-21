package cope.nebula.client.feature.module.world;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MultiTask extends Module {
    public static MultiTask INSTANCE;

    public MultiTask() {
        super("MultiTask", ModuleCategory.WORLD, "Allows you to eat and mine at the same time");
        INSTANCE = this;
    }

    public static final Value<Boolean> offhand = new Value<>("Offhand", false);

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.OUTGOING) && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            CPacketPlayerTryUseItemOnBlock packet = event.getPacket();
            if (packet.getHand().equals(EnumHand.OFF_HAND) && offhand.getValue()) {
                if (mc.player.getHeldItemOffhand().isEmpty() || !mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)) {
                    return;
                }

                // this is for the 2b2tpvp offhand bypass but i need to figure out the packets
            }
        }
    }
}
