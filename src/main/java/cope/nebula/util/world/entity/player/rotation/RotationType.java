package cope.nebula.util.world.entity.player.rotation;

/**
 * Dictates how a rotation will be handled
 *
 * @author aesthetical
 * @since 3/10/22
 */
public enum RotationType {
    /**
     * Should rotate client side
     */
    CLIENT,

    /**
     * Should only rotate server side, aka "silent"
     */
    SERVER,

    /**
     * Do not rotate
     */
    NONE
}
