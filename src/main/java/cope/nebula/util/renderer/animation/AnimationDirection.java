package cope.nebula.util.renderer.animation;

/**
 * Represents a direction an animation could go
 *
 * @author aesthetical
 * @since 3/18/22
 */
public enum AnimationDirection {
    /**
     * Adds onto the increment
     */
    FORWARD,

    /**
     * Subtracts from increment
     */
    BACK;


    /**
     * Gets an animation direction based off of a boolean value
     * @param state the boolean value
     * @return FORWARD if true, BACK if false
     */
    public static AnimationDirection fromBoolean(boolean state) {
        // lmao
        return state ? FORWARD : BACK;
    }
}
