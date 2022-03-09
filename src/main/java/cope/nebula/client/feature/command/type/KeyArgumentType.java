package cope.nebula.client.feature.command.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.lwjgl.input.Keyboard;

/**
 * A key name argument type for brigadier argument parsing
 *
 * @author aesthetical
 * @since 3/8/22
 */
public class KeyArgumentType implements ArgumentType<Integer> {
    public static KeyArgumentType key() {
        return new KeyArgumentType();
    }

    public static Integer getKey(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        return Keyboard.getKeyIndex(reader.readString().toUpperCase());
    }
}
