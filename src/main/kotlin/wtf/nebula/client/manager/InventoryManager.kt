package wtf.nebula.client.manager

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.util.Globals

class InventoryManager : Globals {
    var serverSlot = -1

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {
        if (it.packet is CPacketHeldItemChange) {
            serverSlot = it.packet.slotId
        }
    }

    fun getHeldItemStack(): ItemStack {
        if (serverSlot == -1) {
            serverSlot = mc.player.inventory.currentItem
        }

        return mc.player.inventory.getStackInSlot(serverSlot)
    }
}