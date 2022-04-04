package cope.nebula.client.events;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerListItem.Action;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ServerConnectionEvent extends Event {
    private final Type type;
    private final GameProfile profile;
    private final EntityPlayer player;

    public ServerConnectionEvent(Type type, GameProfile profile, EntityPlayer player) {
        this.type = type;
        this.profile = profile;
        this.player = player;
    }

    public Type getType() {
        return type;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public enum Type {
        JOIN(Action.ADD_PLAYER),
        LEAVE(Action.REMOVE_PLAYER);

        private final Action action;

        Type(Action action) {
            this.action = action;
        }

        public Action getAction() {
            return action;
        }
    }
}
