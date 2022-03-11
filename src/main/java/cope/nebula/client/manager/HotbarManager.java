package cope.nebula.client.manager;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.util.Globals;
import cope.nebula.util.world.entity.player.inventory.SwapType;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Manages the client's hotbar
 *
 * @author aesthetical
 * @since 3/10/22
 */
public class HotbarManager implements Globals {
    /**
     * The slot the server thinks we are on
     */
    private int serverSlot = -1;

    public HotbarManager() {
        EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        switch (event.getDirection()) {
            case INCOMING: {
                // if the server changes our item slot, sync with the manager
                if (event.getPacket() instanceof SPacketHeldItemChange) {
                    serverSlot = ((SPacketHeldItemChange) event.getPacket()).getHeldItemHotbarIndex();
                }

                break;
            }

            case OUTGOING: {
                // if we send a item slot change packet, sync with the manager
                if (event.getPacket() instanceof CPacketHeldItemChange) {
                    serverSlot = ((CPacketHeldItemChange) event.getPacket()).getSlotId();
                }

                break;
            }
        }
    }

    /**
     * Sends a slot change
     * @param slotId The slot id
     * @param type The swap type
     */
    public void sendSlotChange(int slotId, SwapType type) {
        mc.player.connection.sendPacket(new CPacketHeldItemChange(slotId));
        if (type.equals(SwapType.CLIENT)) {
            mc.player.inventory.currentItem = slotId;
        }
    }

    /**
     * Syncs the server slot with the client
     */
    public void sync() {
        if (!nullCheck() && serverSlot != mc.player.inventory.currentItem) {
            sendSlotChange(serverSlot, SwapType.CLIENT);
        }
    }
}
