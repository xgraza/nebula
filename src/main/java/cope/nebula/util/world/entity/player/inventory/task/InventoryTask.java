package cope.nebula.util.world.entity.player.inventory.task;

import cope.nebula.util.Globals;
import net.minecraft.inventory.ClickType;

/**
 * Represents a pending inventory task
 *
 * Useful for delaying window clicks for stricter servers
 *
 * @author aesthetical
 * @since 3/10/22
 */
public class InventoryTask implements Globals {
    /**
     * The slot to click
     *
     * This should be run through InventoryUtil#toPacketSlot
     */
    private final int slotId;

    /**
     * The type of window click to use
     */
    private final ClickType clickType;

    public InventoryTask(int slotId, ClickType clickType) {
        this.slotId = slotId;
        this.clickType = clickType;
    }

    /**
     * Runs this inventory task
     */
    public void execute() {
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotId, 0, clickType, mc.player);
    }
}
