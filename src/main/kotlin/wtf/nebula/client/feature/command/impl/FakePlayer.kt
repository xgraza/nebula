package wtf.nebula.client.feature.command.impl

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.world.GameType
import wtf.nebula.client.feature.command.Command

class FakePlayer : Command(arrayOf("fakeplayer", "fp", "dummy", "dummyplayer"), "", "Spawns a fake player") {
    private var fakePlayer: EntityOtherPlayerMP? = null

    override fun build(builder: LiteralArgumentBuilder<Any>) {
        builder.executes {
            if (fakePlayer != null) {
                mc.world.removeEntityDangerously(fakePlayer)
                mc.world.removeEntity(fakePlayer)

                fakePlayer = null
            }

            else {
                val fake = EntityOtherPlayerMP(mc.world, mc.player.gameProfile)

                fake.copyLocationAndAnglesFrom(mc.player)
                fake.inventory.copyInventory(mc.player.inventory)
                fake.health = mc.player.health
                fake.absorptionAmount = mc.player.absorptionAmount
                fake.setGameType(GameType.SURVIVAL)

                mc.world.spawnEntity(fake)
                fakePlayer = fake
            }

            return@executes SINGLE_SUCCESS
        }
    }
}