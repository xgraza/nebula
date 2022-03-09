package cope.nebula.client.feature.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import cope.nebula.client.feature.Feature;
import net.minecraft.util.text.TextComponentString;

public class Command extends Feature {
    private final LiteralArgumentBuilder<Object> builder;

    public Command(String name, LiteralArgumentBuilder<Object> builder) {
        super(name);
        this.builder = builder;
    }

    /**
     * A wrapper for Globals#sendChatMessage
     * @param text The text to send
     */
    public static void send(String text) {
        mc.ingameGUI.getChatGUI().printChatMessage(
                new TextComponentString(MESSAGE_PREFIX).appendText(" ").appendText(text));
    }

    /**
     * A wrapper for Globals#sendChatMessage
     * @param text The text to send
     * @param id The message id to edit later on
     */
    public static void send(String text, int id) {
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(
                new TextComponentString(MESSAGE_PREFIX).appendText(" ").appendText(text), id);
    }

    public LiteralArgumentBuilder<Object> getBuilder() {
        return builder;
    }
}
