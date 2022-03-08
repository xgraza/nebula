package cope.nebula.util;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.client.Nebula;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;

/**
 * An interface with useful utilities used in all features
 *
 * @author aesthetical
 * @since 3/7/22
 */
public interface Globals {
    Minecraft mc = Minecraft.getMinecraft();
    EventBus EVENT_BUS = MinecraftForge.EVENT_BUS;

    String MESSAGE_PREFIX = ChatFormatting.DARK_PURPLE + "[Nebula]" + ChatFormatting.RESET;

    /**
     * Shows a chat message with optional deletion
     * @param message The message to print
     * @param id The id for editing later
     */
    default void sendChatMessage(String message, int id) {
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(
                new TextComponentString(MESSAGE_PREFIX).appendText(" ").appendText(message), id);
    }

    /**
     * Sends a chat message without deletion
     * @param message The message to print
     */
    default void sendChatMessage(String message) {
        mc.ingameGUI.getChatGUI().printChatMessage(
                new TextComponentString(MESSAGE_PREFIX).appendText(" ").appendText(message));
    }

    /**
     * Wrapper for Nebula.getInstance() for objects that implement Globals
     * @return The Nebula client instance
     */
    default Nebula getNebula() {
        return Nebula.getInstance();
    }

    /**
     * Checks if core components of minecraft have been initialized yet
     * @return If world is null or player is null
     */
    default boolean nullCheck() {
        return mc.world == null || mc.player == null;
    }
}
