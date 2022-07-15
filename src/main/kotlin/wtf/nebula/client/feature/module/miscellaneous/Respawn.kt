package wtf.nebula.client.feature.module.miscellaneous

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.network.play.client.CPacketClientStatus
import wtf.nebula.client.event.client.minecraft.GuiOpenedEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.Timer

class Respawn : Module(ModuleCategory.MISCELLANEOUS, "Automatically respawns you") {
    val delay by int("Delay", 2, 0..10)

    private val timer = Timer()

    @EventListener
    private val guiOpenedEvent = listener<GuiOpenedEvent> {
        if (it.opened is GuiGameOver) {
            timer.resetTime()
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (timer.passedTime(delay.toLong() * 50L) && (mc.player.isDead || mc.player.health <= 0.0f)) {
            mc.player.connection.sendPacket(CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN))
        }
    }
}