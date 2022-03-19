package cope.nebula.client.manager;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cope.nebula.client.Nebula;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.command.Command;
import cope.nebula.client.feature.command.impl.Bind;
import cope.nebula.client.feature.command.impl.FakePlayer;
import cope.nebula.client.feature.command.impl.MacroCommand;
import cope.nebula.client.feature.command.impl.Toggle;
import cope.nebula.util.Globals;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

/**
 * Holds all the commands of the client
 *
 * @author aesthetical
 * @since 3/8/22
 */
public class CommandManager implements Globals {
    private final CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
    private final ArrayList<Command> commands = Lists.newArrayList(new Bind(), new FakePlayer(), new MacroCommand(), new Toggle());

    private String prefix = ",";

    public CommandManager() {
        EVENT_BUS.register(this);

        Nebula.getLogger().info("Loaded {} commands", commands.size());
        commands.forEach((command) -> dispatcher.register(command.getBuilder()));
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof CPacketChatMessage && event.getDirection().equals(Direction.OUTGOING)) {
            CPacketChatMessage packet = (CPacketChatMessage) event.getPacket();
            if (packet.getMessage().startsWith(prefix)) {
                event.setCanceled(true);
                String message = packet.getMessage();

                try {
                    dispatcher.execute(dispatcher.parse(message.substring(prefix.length()), 1));
                } catch (CommandSyntaxException e) {
                    sendChatMessage("An exception occurred, please run the help command.");
                }
            }
        }
    }

    /**
     * Finds a command based on the name of the command
     * @param name The name of the command
     * @param <T> The command type
     * @return a command of T, or null if none found
     */
    public <T extends Command> T getCommand(String name) {
        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(name)) {
                return (T) command;
            }
        }

        return null;
    }

    /**
     * @return the command prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return a list of all the commands
     */
    public ArrayList<Command> getCommands() {
        return commands;
    }
}
