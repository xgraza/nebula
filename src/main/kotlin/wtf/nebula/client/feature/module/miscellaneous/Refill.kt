package wtf.nebula.client.feature.module.miscellaneous

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.client.manager.Task
import wtf.nebula.client.manager.TaskList

class Refill : Module(ModuleCategory.MISCELLANEOUS, "Refills your hotbar with items") {
    val percentage by double("Percentage", 70.0, 30.0..85.0)
    val delay by int("Delay", 2, 0..10)

    override fun onDeactivated() {
        super.onDeactivated()
        Nebula.inventoryManager.removeAllTasks(hashCode())
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (Nebula.inventoryManager.isEmpty(hashCode())) {
            for (i in 0 until 9) {
                val stack = mc.player.inventory.getStackInSlot(i)
                if (stack.isEmpty) {
                    continue
                }

                val percentageDepleted = (stack.count.toDouble() / stack.maxStackSize.toDouble()) * 100.0
                if (percentageDepleted < percentage) {
                    refill(i, stack)
                }
            }
        }
    }

    private fun refill(slot: Int, stack: ItemStack) {
        var refillSlot = -1
        var size = 0

        for (i in 9 until 36) {
            val itemStack = mc.player.inventory.getStackInSlot(i)
            if (itemStack.isEmpty) {
                continue
            }

            if (!itemStack.displayName.equals(stack.displayName)) {
                continue
            }

            if (stack.item is ItemBlock) {
                val stackBlock = (stack.item as ItemBlock).block

                if (itemStack.item !is ItemBlock) {
                    continue
                }

                if (stackBlock == (itemStack.item as ItemBlock).block) {
                    refillSlot = i
                    size = itemStack.count
                }
            }

            else {
                if (stack.item == itemStack.item) {
                    refillSlot = i
                    size = itemStack.count
                }
            }
        }

        if (refillSlot == -1) {
            return
        }

        var taskList = TaskList(hashCode())

        taskList += Task(slot = refillSlot, delay = delay)
        taskList += Task(slot = slot + 36, delay = delay)

        if (size + stack.count > stack.maxDamage) {
            taskList += Task(slot = refillSlot, delay = delay)
        }

        Nebula.inventoryManager.submitTasks(taskList)
    }
}