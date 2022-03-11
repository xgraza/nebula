package cope.nebula.util.world.entity.player.inventory;

import cope.nebula.util.Globals;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumHand;

import java.util.function.Predicate;

/**
 * Finds slot ids, checks if the local player is holding something etc
 *
 * @author aesthetical
 * @since 3/10/22
 */
public class InventoryUtil implements Globals {
    /**
     * Gets an inventory slot
     * @param inventorySpace The space to look inside for the filter
     * @param filter Determines if this is the item we're looking for
     * @return a slot id or -1 if none found
     */
    public static int getSlot(InventorySpace inventorySpace, Predicate<ItemStack> filter) {
        int i = inventorySpace.getStart();
        if (i == -1) {
            return -1;
        }

        while (i < inventorySpace.getEnd()) {
            ++i;

            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (filter.test(stack)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Gets an item stack in a hand
     * @param hand The hand to look into
     * @return the ItemStack
     */
    public static ItemStack getHeld(EnumHand hand) {
        return mc.player.getHeldItem(hand);
    }

    /**
     * Checks if the ItemStack is a stack of blocks
     * @param stack The item stack
     * @return if the stack is a stack of blocks
     */
    public static boolean isBlock(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemBlock;
    }

    /**
     * Checks if these two item stacks can be combined
     * @param i An item stack
     * @param o An item stack
     * @return if they can be combined
     */
    public static boolean canCombine(ItemStack i, ItemStack o) {
        // item stacks cannot be combined if they do not have the same display name
        // they also cannot be combined if they cannot be stacked in the first place (eg totems)
        // they also cannot be combined if the stack is at its max stack size
        if (!i.getDisplayName().equals(o.getDisplayName()) || i.getMaxStackSize() == 1 || i.getCount() == i.getMaxStackSize()) {
            return false;
        }

        // if the first stack is a block
        if (isBlock(i)) {
            // if the second stack is not a block, they cannot be combined as they're not both blocks
            if (!isBlock(o)) {
                return false;
            }

            // this looks like shit
            // check if the blocks are the same
            return ((ItemBlock) i.getItem()).getBlock().equals(((ItemBlock) o.getItem()).getBlock());
        } else {
            // else they are not a block, check if they're the same item
            return i.getItem().equals(o.getItem());
        }

        // TODO do we need to check enchantments for things like enchanted glass?
    }

    /**
     * Converts a slot into a slot id for a CPacketClickWindow packet
     * @param slot the slot
     * @return the corrected slot id
     */
    public static int toPacketSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }

    /**
     * Sends an inventory open packet
     */
    public static void openInventory() {
        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
    }
}
