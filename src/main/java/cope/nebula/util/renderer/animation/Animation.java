package cope.nebula.util.renderer.animation;

import net.minecraft.util.math.MathHelper;

/**
 * Represents an animation
 *
 * @author aesthetical
 * @since 3/18/22
 */
public class Animation {
    private final long delay;
    private long time = -1L;

    private double progress = 0.0;
    private double increment = 0.0;
    private double max;

    public Animation(double maxIn, long delay) {
        this.delay = delay;

        if (maxIn <= 0.0) {
            maxIn = 100L;
        }

        max = maxIn;
        time = System.currentTimeMillis();
    }

    /**
     * Ticks the animation
     * @param direction the direction to point the animation in
     */
    public void tick(AnimationDirection direction) {
        // we're not using a timer for sake of time
        if (System.currentTimeMillis() - time >= delay) {
            // reset time
            time = System.currentTimeMillis();

            switch (direction) {
                case FORWARD:
                    progress += increment;
                    break;

                case BACK:
                    progress -= increment;
                    break;
            }

            // clamp between 0 and our max
            progress = MathHelper.clamp(progress, 0.0, max);
        }
    }

    /**
     * Resets the animation
     */
    public void reset() {
        time = System.currentTimeMillis();
        progress = 0.0;
    }


    /**
     * Sets the amount to increment the value by
     * @param in the incrementation value
     */
    public void setIncrement(double in) {
        increment = in;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMax() {
        return max;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public double getProgress() {
        return progress;
    }
}
