package wtf.nebula.client.manager

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.util.Globals
import wtf.nebula.util.inventory.InventoryRegion
import wtf.nebula.util.inventory.InventoryUtil

class InventoryManager : Globals {
    var serverSlot = -1

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {
        if (it.packet is CPacketHeldItemChange) {
            serverSlot = it.packet.slotId
        }
    }

    fun getSlot(region: InventoryRegion, items: Array<Item>): Int {
        for (item in items) {
            if (mc.player.heldItemOffhand.item == item) {
                return InventoryUtil.OFFHAND_SLOT
            }

            val slot = InventoryUtil.findItem(region) { it.item == item }
            if (slot != -1) {
                return slot
            }
        }

        return -1
    }

    fun getSlot(region: InventoryRegion, blocks: Array<Block>): Int {
        for (block in blocks) {
            if (mc.player.heldItemOffhand.item is ItemBlock && (mc.player.heldItemOffhand.item as ItemBlock).block == block) {
                return InventoryUtil.OFFHAND_SLOT
            }

            val slot = InventoryUtil.findItem(region) { it.item is ItemBlock && (it.item as ItemBlock).block == block }
            if (slot != -1) {
                return slot
            }
        }

        return -1
    }

    fun getHeldItemStack(): ItemStack {
        if (serverSlot == -1) {
            serverSlot = mc.player.inventory.currentItem
        }

        return mc.player.inventory.getStackInSlot(serverSlot)
    }
}