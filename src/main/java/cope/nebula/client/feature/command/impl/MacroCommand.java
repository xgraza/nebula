package cope.nebula.client.feature.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.client.Nebula;
import cope.nebula.client.feature.command.Command;
import cope.nebula.client.feature.command.type.KeyArgumentType;
import cope.nebula.client.manager.macro.Macro;
import org.lwjgl.input.Keyboard;

public class MacroCommand extends Command {
    public MacroCommand() {
        super("macro", LiteralArgumentBuilder.literal("macro")
                .then(RequiredArgumentBuilder.argument("keyCode", KeyArgumentType.key())
                        .executes((ctx) -> {
                            send("Please provide what this macro will do.");
                            return 0;
                        })
                        .then(RequiredArgumentBuilder.argument("content", StringArgumentType.string())
                                .executes((ctx) -> {
                                    Macro macro = new Macro(StringArgumentType.getString(ctx, "content"), KeyArgumentType.getKey(ctx, "keyCode"));
                                    Nebula.getInstance().getMacroManager().add(macro);

                                    send("Created new macro with the key " +
                                            ChatFormatting.DARK_PURPLE + Keyboard.getKeyName(macro.getKeyCode()) + ChatFormatting.RESET + ".");
                                    return 0;
                                })))
                .executes((ctx) -> {
                    send("Please provide a macro key name");
                    return 0;
                }));
    }
}
