package cope.nebula.client.feature.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.client.feature.command.Command;
import cope.nebula.client.feature.command.type.ModuleArgumentType;
import cope.nebula.client.feature.module.Module;

public class Toggle extends Command {
    public Toggle() {
        super("toggle", LiteralArgumentBuilder.literal("toggle")
                .then(RequiredArgumentBuilder.argument("module", ModuleArgumentType.module())
                        .executes((ctx) -> {
                            Module module = ModuleArgumentType.getModule(ctx, "module");
                            module.toggle();

                            send(module.getName() + " toggled " + (
                                    module.isOn() ?
                                            ChatFormatting.GREEN + "on" + ChatFormatting.RESET :
                                            ChatFormatting.RED + "off" + ChatFormatting.RESET) + ".",
                                    module.hashCode());

                            return 0;
                        }))
                .executes((ctx) -> {
                    send("Please provide a module name.");
                    return 0;
                }));
    }
}
