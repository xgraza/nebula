package cope.nebula.util.world.entity.player.inventory;

/**
 * Represents ways we can swap to a slot
 *
 * @author aesthetical
 * @since 3/10/22
 */
public enum SwapType {
    /**
     * Swap client side
     */
    CLIENT,

    /**
     * Swap server side
     */
    SERVER,

    /**
     * Do not swap at all
     */
    NONE
}
