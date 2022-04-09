package cope.nebula.util.internal.math;

/**
 * Does some basic shit that really doesn't need a util but yeah
 *
 * @author aesthetical
 * @since 4/8/22
 */
public class MathUtil {
    /**
     * Gets a random double from within the bounds of min/max
     * @param min the minimum value
     * @param max the maximum value
     * @return the random double between min - max
     */
    public static double random(double min, double max) {
        return Math.random() * (min - max) + max;
    }
}
