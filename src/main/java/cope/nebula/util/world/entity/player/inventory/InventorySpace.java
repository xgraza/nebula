package cope.nebula.util.world.entity.player.inventory;

/**
 * Represents different places in the inventory
 *
 * @author aesthetical
 * @since 3/10/22
 */
public enum InventorySpace {
    HOTBAR(0, 9),
    INVENTORY(9, 36),
    CRAFTING(-1, -1); // TODO i dont remember these ones lmao

    private final int start, end;

    InventorySpace(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
