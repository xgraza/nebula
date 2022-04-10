package cope.nebula.client.manager;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.client.events.DeathEvent;
import cope.nebula.client.events.PopEvent;
import cope.nebula.client.events.ServerConnectionEvent;
import cope.nebula.client.events.ServerConnectionEvent.Type;
import cope.nebula.client.feature.module.other.Notifier;
import cope.nebula.util.Globals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player totem pops
 *
 * @author aesthetical
 * @since 4/9/22
 */
public class TotemPopManager implements Globals {
    public TotemPopManager() {
        EVENT_BUS.register(this);
    }

    /**
     * Represents all cached totem pops
     *
     * Key: player entity id
     * Value: totem pop count
     */
    private final Map<Integer, Integer> pops = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onServerConnection(ServerConnectionEvent event) {
        if (event.getType().equals(Type.LEAVE)) {
            EntityPlayer player = event.getPlayer();
            if (player == null) {
                return;
            }

            if (player.equals(mc.player)) {
                pops.clear();
                return;
            }

            int total = getPops(player.getEntityId());
            if (total == -1) {
                return;
            }

            sendChatMessage(player.getName()
                    + " logged out after popping "
                    + ChatFormatting.DARK_PURPLE + total + ChatFormatting.RESET
                    + " totem" + (total > 1 ? "s." : "."), player.hashCode());
        }
    }

    @SubscribeEvent
    public void onDeath(DeathEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player != null) {
            int total = getPops(player.getEntityId());
            if (total == -1) {
                return;
            }

            if (Notifier.shouldTotemPopNotify(player)) {
                sendChatMessage(player.getName()
                        + " has died after popping "
                        + ChatFormatting.DARK_PURPLE + total + ChatFormatting.RESET
                        + " totem" + (total > 1 ? "s." : "."), player.hashCode());

                pops.put(player.getEntityId(), 0);
            }
        }
    }

    @SubscribeEvent
    public void onPop(PopEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player != null) {
            int total = pops.merge(player.getEntityId(), 1, Integer::sum);

            if (Notifier.shouldTotemPopNotify(player)) {
                sendChatMessage(player.getName()
                        + " has popped " +
                        ChatFormatting.DARK_PURPLE + total + ChatFormatting.RESET
                        + " totem" + (total > 1 ? "s." : "."), player.hashCode());
            }
        }
    }

    /**
     * Gets the pops for a player
     * @param entityId the player's entity id
     * @return the amount of times they popped
     */
    public int getPops(int entityId) {
        return pops.getOrDefault(entityId, -1);
    }
}
