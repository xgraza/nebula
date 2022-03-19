package cope.nebula.client.feature.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import cope.nebula.client.feature.command.Command;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.world.GameType;

public class FakePlayer extends Command {
    private static EntityOtherPlayerMP fakePlayer = null;

    public FakePlayer() {
        super("fakeplayer", LiteralArgumentBuilder.literal("fakeplayer")
                .executes((ctx) -> {
                    if (fakePlayer != null) {
                        mc.world.removeEntity(fakePlayer);
                        mc.world.removeEntityDangerously(fakePlayer);

                        fakePlayer = null;

                        send("Removed fake player.");
                        return 0;
                    }

                    fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
                    fakePlayer.copyLocationAndAnglesFrom(mc.player);
                    fakePlayer.inventory.copyInventory(mc.player.inventory);
                    fakePlayer.setHealth(20.0f);
                    fakePlayer.setGameType(GameType.SURVIVAL);

                    mc.world.spawnEntity(fakePlayer);

                    send("Spawned fake player.");

                    return 0;
                }));
    }
}
