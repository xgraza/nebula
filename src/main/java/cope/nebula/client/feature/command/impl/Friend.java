package cope.nebula.client.feature.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.asm.duck.IEntityPlayer;
import cope.nebula.client.feature.command.Command;
import cope.nebula.client.feature.command.type.PlayerArgumentType;
import net.minecraft.entity.player.EntityPlayer;

public class Friend extends Command {
    public Friend() {
        super("friend", LiteralArgumentBuilder.literal("friend")
                .then(RequiredArgumentBuilder.argument("player", PlayerArgumentType.player())
                        .executes((ctx) -> {
                            EntityPlayer player = PlayerArgumentType.getPlayer(ctx, "player");
                            if (player == null) {
                                send("Could not find that player.");
                                return 0;
                            }

                            boolean result = ((IEntityPlayer) player).friend();
                            send(result ? "Added" : "Removed" + " " + ChatFormatting.DARK_PURPLE + player.getName() + ChatFormatting.RESET + " as a friend.");

                            return 0;
                        }))
                .executes((ctx) -> {
                    send("Please provide a player username/uuid");
                    return 0;
                }));
    }
}
