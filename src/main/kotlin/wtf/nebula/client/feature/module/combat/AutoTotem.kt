package wtf.nebula.client.feature.module.combat

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.init.Items
import net.minecraft.item.ItemSword
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.client.manager.Task
import wtf.nebula.client.manager.TaskList
import wtf.nebula.util.inventory.InventoryUtil

class AutoTotem : Module(ModuleCategory.COMBAT, "Automatically places totems in your offhand") {
    val item by setting("Item", Item.CRYSTAL)
    val delay by int("Delay", 2, 0..10)
    val swordGap by bool("Sword GApple", true)
    val hotbar by bool("Hotbar", true)
    val health by float("Health", 14.0f, 2.0f..20.0f)

    private var currentItem = Items.TOTEM_OF_UNDYING

    override fun getDisplayInfo(): String? {
        val offhandStack = mc.player.heldItemOffhand
        if (offhandStack.isEmpty) {
            return null
        }

        return mc.player.inventory.mainInventory
            .filter { it.item == offhandStack.item }
            .sumOf { it.count }
            .toString()
    }

    override fun onDeactivated() {
        super.onDeactivated()
        Nebula.inventoryManager.removeAllTasks(hashCode())
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (Nebula.inventoryManager.isEmpty(hashCode())) {
            if (canDieFromFallDamage() || mc.player.health < health) {
                currentItem = Items.TOTEM_OF_UNDYING
            }

            else {
                currentItem = item.item
                if (swordGap && mc.gameSettings.keyBindUseItem.isKeyDown && mc.player.heldItemMainhand.item is ItemSword) {
                    currentItem = Items.GOLDEN_APPLE
                }
            }

            handleItems()
        }
    }

    private fun handleItems() {
        if (mc.player.heldItemOffhand.item == currentItem) {
            return
        }

        if (hotbar && Nebula.inventoryManager.getHeldItemStack().item == currentItem) {
            return
        }

        var slot = -1
        for (i in 0 until 36) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (!stack.isEmpty && stack.item == currentItem) {
                slot = i
            }
        }

        if (slot == -1) {
            return
        }

        val serverSlot = if (slot < 9) slot + 36 else slot
        val floatingItem = !mc.player.heldItemOffhand.isEmpty

        var taskList = TaskList(hashCode())

        taskList += Task(slot = serverSlot, delay = delay)
        taskList += Task(slot = InventoryUtil.OFFHAND_SLOT, delay = delay)

        if (floatingItem) {
            taskList += Task(slot = serverSlot, delay = delay)
        }

        Nebula.inventoryManager.submitTasks(taskList)
    }

    private fun canDieFromFallDamage(): Boolean {
        return ((mc.player.fallDistance - 3.0f) / 2.0f) + 3.5f >= mc.player.health + mc.player.absorptionAmount
    }

    enum class Item(val item: net.minecraft.item.Item) {
        TOTEM(Items.TOTEM_OF_UNDYING),
        CRYSTAL(Items.END_CRYSTAL),
        GAPPLE(Items.GOLDEN_APPLE),
        EXP(Items.EXPERIENCE_BOTTLE)
    }
}