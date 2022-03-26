package cope.nebula.client.manager;

import cope.nebula.util.Globals;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.UUID;

/**
 * Manages things that do with the server.
 * Eg: ping for a player, TPS calculation, etc
 *
 * @author aesthetical
 * @since 3/26/22
 */
public class ServerManager implements Globals {
    public ServerManager() {
        EVENT_BUS.register(this);
    }

    /**
     * Gets the latency of a player
     * @param uuid The player's UUID
     * @return their response time, or -1 if none found.
     */
    public int getLatency(UUID uuid) {
        for (NetworkPlayerInfo info : mc.player.connection.getPlayerInfoMap()) {
            if (info.getGameProfile() != null && info.getGameProfile().getId().equals(uuid)) {
                return info.getResponseTime();
            }
        }

        return -1;
    }
}
