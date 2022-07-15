package wtf.nebula.client.manager

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.block.Block
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.server.SPacketHeldItemChange
import wtf.nebula.client.event.packet.PacketReceiveEvent
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.util.Globals
import wtf.nebula.util.inventory.InventoryRegion
import wtf.nebula.util.inventory.InventoryUtil
import java.util.concurrent.ConcurrentHashMap

class InventoryManager : Globals {
    var serverSlot = -1

    val tasks = ConcurrentHashMap<Int, MutableList<Task>>()
    private var runTasks = true

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {
        if (it.packet is CPacketHeldItemChange) {
            serverSlot = it.packet.slotId
        }
    }

    @EventListener
    private val packetReceiveListener = listener<PacketReceiveEvent> {
        if (it.packet is SPacketHeldItemChange) {
            serverSlot = it.packet.heldItemHotbarIndex
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        runCatching {
            for ((id, todo) in tasks) {
                for (task in todo) {
                    if (!runTasks) {
                        return@listener
                    }

                    if (System.currentTimeMillis() - task.time >= task.delay * 50L) {
                        val windowId = mc.player.openContainer.windowId
                        mc.playerController.windowClick(windowId, task.slot, task.mouseButton, task.type, mc.player)
                        tasks[id] = tasks[id]?.minus(task) as MutableList<Task>
                    }
                }
            }
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

    fun submitTasks(taskList: TaskList) {
        var time = System.currentTimeMillis()
        taskList.tasks.forEach {
            it.time = time
            time += ((it.delay + 1) * 50L)
        }

        tasks[taskList.id] = taskList.tasks
    }

    fun removeAllTasks(id: Int) {
        runTasks = false
        tasks.remove(id)
        runTasks = true
    }

    fun isEmpty(id: Int): Boolean {
        val tasks = tasks[id] ?: return true
        return tasks.isEmpty()
    }

    fun getHeldItemStack(): ItemStack {
        if (serverSlot == -1) {
            serverSlot = mc.player.inventory.currentItem
        }

        return mc.player.inventory.getStackInSlot(serverSlot)
    }
}

class TaskList(val id: Int) {
    val tasks = mutableListOf<Task>()

    operator fun plus(task: Task): TaskList {
        task.id = id
        tasks += task
        return this
    }
}

data class Task(
    var id: Int = 0,
    val slot: Int,
    val mouseButton: Int = 0,
    val type: ClickType = ClickType.PICKUP,
    var time: Long = System.currentTimeMillis(),
    val delay: Int
)