package wtf.nebula.client.manager

import com.mojang.realmsclient.gui.ChatFormatting
import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.event.player.DeathEvent
import wtf.nebula.client.event.player.TotemPopEvent
import wtf.nebula.util.Globals

class TotemPopManager : Globals {
    private val totemPops = mutableMapOf<Int, Int>()
    var sendEvents = false

    @EventListener
    private val totemPopListener = listener<TotemPopEvent> {
        val popped = (totemPops[it.entityPlayer.entityId] ?: 0) + 1
        totemPops[it.entityPlayer.entityId] = popped

        if (sendEvents) {
            printChatMessage(
                it.entityPlayer.hashCode(),
            (if (it.entityPlayer == mc.player) "You" else it.entityPlayer.name)
                        + " has popped "
                    + ChatFormatting.RED + popped + ChatFormatting.RESET
                    + "totem" + (if (popped > 1) "s" else ""))
        }
    }

    @EventListener
    private val deathEventListener = listener<DeathEvent> {
        if (sendEvents) {
            val pops = totemPops[it.entityPlayer.entityId] ?: return@listener
            printChatMessage(
                it.entityPlayer.hashCode(),
                (if (it.entityPlayer == mc.player) "You have died" else it.entityPlayer.name + " has died")
                        + " after popping "
                        + ChatFormatting.RED + pops + ChatFormatting.RESET
                        + "totem" + (if (pops > 1) "s" else ""))

            totemPops -= it.entityPlayer.entityId
        }
    }
}