package cope.nebula.util.world.entity.player;

import cope.nebula.util.Globals;
import cope.nebula.util.internal.math.Vec2d;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;

/**
 * Checks/calculates movement values
 *
 * @author aesthetical
 * @since 3/11/22
 */
public class MotionUtil implements Globals {
    /**
     * Checks if the local player is moving
     * @return if moveForward or moveStrafe does not equal 0
     */
    public static boolean isMoving() {
        return mc.player.movementInput.moveForward != 0.0f || mc.player.movementInput.moveStrafe != 0.0f;
    }

    /**
     * Calculates strafe motion values
     * @param motionSpeed The speed to strafe
     * @return a Vec2d of the x and z motion values
     */
    public static Vec2d strafe(double motionSpeed) {
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float yaw = (float) AngleUtil.interpolate(mc.player.rotationYaw, mc.player.prevRotationYaw);

        if (forward != 0.0f) {
            if (strafe > 0.0f) {
                yaw += forward > 0.0f ? -45.0f : 45.0f;
            } else if (strafe < 0.0f) {
                yaw += forward > 0.0f ? 45.0f : -45.0f;
            }

            strafe = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }

        double rad = Math.toRadians(yaw);

        double sin = -Math.sin(rad);
        double cos = Math.cos(rad);

        return new Vec2d(
                forward * motionSpeed * sin + strafe * motionSpeed * cos,
                forward * motionSpeed * cos - strafe * motionSpeed * sin
        );
    }
}
