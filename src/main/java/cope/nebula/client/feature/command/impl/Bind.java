package cope.nebula.client.feature.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.client.feature.command.Command;
import cope.nebula.client.feature.command.type.KeyArgumentType;
import cope.nebula.client.feature.command.type.ModuleArgumentType;
import cope.nebula.client.feature.module.Module;
import org.lwjgl.input.Keyboard;

public class Bind extends Command {
    public Bind() {
        super("bind", LiteralArgumentBuilder.literal("bind")
                .then(RequiredArgumentBuilder.argument("keyCode", KeyArgumentType.key())
                        .executes((ctx) -> {
                            send("Please provide a module name.");
                            return 0;
                        })
                        .then(RequiredArgumentBuilder.argument("module", ModuleArgumentType.module())
                                .executes((ctx) -> {
                                    Module module = ModuleArgumentType.getModule(ctx, "module");
                                    module.setKeyBind(KeyArgumentType.getKey(ctx, "keyCode"));

                                    send("Set " + module.getName() + "'s bind to "
                                            + ChatFormatting.DARK_PURPLE + Keyboard.getKeyName(module.getKeyBind()) + ChatFormatting.RESET + ".");

                                    return 0;
                                })))
                .executes((ctx) -> {
                    send("Please provide a key name.");
                    return 0;
                }));
    }
}
