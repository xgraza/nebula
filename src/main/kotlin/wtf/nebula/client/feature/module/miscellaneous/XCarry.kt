package wtf.nebula.client.feature.module.miscellaneous

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.network.play.client.CPacketCloseWindow
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class XCarry : Module(ModuleCategory.MISCELLANEOUS, "cancels inventory close packets. if u use this on strict ur stupid") {
    private var windowId = -1

    override fun onDeactivated() {
        super.onDeactivated()
        if (windowId != -1) {
            mc.player.connection.sendPacket(CPacketCloseWindow(windowId))
            windowId = -1
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (mc.currentScreen is GuiInventory) {
            windowId = mc.player.openContainer.windowId
        }
    }

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {
        if (it.packet is CPacketCloseWindow) {
            val packet = it.packet
            if (packet.windowId == windowId) {
                it.cancelled = true
            }
        }
    }
}