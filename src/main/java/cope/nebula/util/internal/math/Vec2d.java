package cope.nebula.util.internal.math;

/**
 * A vector that holds two doubles
 *
 * @author aesthetical
 * @since 3/11/22
 */
public class Vec2d {
    private final double x, z;

    public Vec2d(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    /**
     * Adds x and z values onto the current existing ones
     * @param xIn the x value
     * @param zIn the z value
     * @return a Vec2d that will have the added x and z values added on
     */
    public Vec2d add(double xIn, double zIn) {
        return new Vec2d(x + xIn, z + zIn);
    }
}
