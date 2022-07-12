package wtf.nebula.util.inventory

import net.minecraft.item.ItemStack
import wtf.nebula.util.Globals
import java.util.function.Predicate

object InventoryUtil : Globals {
    const val OFFHAND_SLOT = 45

    fun findItem(region: InventoryRegion, filter: Predicate<ItemStack>): Int {
        if (filter.test(mc.player.heldItemOffhand)) {
            return OFFHAND_SLOT
        }

        for (i in region.range) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (!stack.isEmpty && filter.test(stack)) {
                return i
            }
        }

        return -1
    }

}