package cope.nebula.util.world.entity.player.rotation;

/**
 * Represents a rotation to be sent to the server
 *
 * @author aesthetical
 * @since 3/10/22
 */
public class Rotation {
    /**
     * A completely invalid rotation object
     */
    public static final Rotation INVALID_ROTATION = new Rotation(Float.NaN, Float.NaN);

    /**
     * The yaw and pitch rotation values
     */
    private final float yaw, pitch;
    private final RotationType type;

    public Rotation(float yaw, float pitch) {
        this(RotationType.NONE, yaw, pitch);
    }

    public Rotation(RotationType type, float yaw, float pitch) {
        this.type = type;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public RotationType getType() {
        return type;
    }

    public Rotation setYaw(float yawIn) {
        return new Rotation(yawIn, pitch);
    }

    public Rotation setPitch(float pitchIn) {
        return new Rotation(yaw, pitchIn);
    }

    public Rotation setType(RotationType type) {
        return new Rotation(type, yaw, pitch);
    }

    /**
     * @return if this rotation is applicable to use
     */
    public boolean isValid() {
        return !Float.isNaN(yaw) && !Float.isNaN(pitch);
    }
}
