package cope.nebula.client.feature.command.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cope.nebula.util.Globals;
import net.minecraft.entity.player.EntityPlayer;

/**
 * An entity player argument type for brigadier argument parsing
 *
 * @author aesthetical
 * @since 4/15/22
 */
public class PlayerArgumentType implements ArgumentType<EntityPlayer>, Globals {
    public static PlayerArgumentType player() {
        return new PlayerArgumentType();
    }

    public static EntityPlayer getPlayer(final CommandContext<?> context, final String name) {
        return context.getArgument(name, EntityPlayer.class);
    }

    @Override
    public EntityPlayer parse(StringReader reader) throws CommandSyntaxException {

        // make sure we have a connection
        if (mc.player.connection == null) {
            return null;
        }

        String text = reader.readString();

        // look through all the players in the world
        for (EntityPlayer player : mc.world.playerEntities) {

            // if the player username or UUID equals the argument, we've found a match!
            if (player.getName().equalsIgnoreCase(text) || player.getUniqueID().toString().equals(text)) {
                return player;
            }
        }

        // we found noone :(
        return null;
    }
}
