package cope.nebula.client.feature.command.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cope.nebula.client.feature.module.Module;
import cope.nebula.util.Globals;

/**
 * A module argument type for brigadier argument parsing
 *
 * @author aesthetical
 * @since 3/8/22
 */
public class ModuleArgumentType implements ArgumentType<Module>, Globals {
    public static ModuleArgumentType module() {
        return new ModuleArgumentType();
    }

    public static Module getModule(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Module.class);
    }

    @Override
    public Module parse(StringReader reader) throws CommandSyntaxException {
        String text = reader.readString();

        for (Module module : getNebula().getModuleManager().getModules()) {
            if (module.getName().equalsIgnoreCase(text)) {
                return module;
            }
        }

        return null;
    }
}
