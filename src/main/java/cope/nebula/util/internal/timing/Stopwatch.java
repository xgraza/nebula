package cope.nebula.util.internal.timing;

/**
 * Represents a "stopwatch" that will record time
 *
 * @author aesthetical
 * @since 3/7/22
 */
public class Stopwatch {
    /**
     * A number that if multiplied to a time period in milliseconds, will convert into nanoseconds
     */
    private static final long MS_TO_NS = 1000000L;

    /**
     * A number that if multiplied to a time period in seconds, will convert into nanoseconds
     */
    private static final long S_TO_MS = 1000000000L;

    /**
     * The last time set by calling .reset()
     *
     * Represents System.nanoTime()
     */
    private long lastTime = -1L;

    /**
     * Checks if we have elapsed time
     * @param time The time
     * @param reset If we should reset this stopwatch if time has elapsed
     * @param format The time format
     * @return If we have elapsed time in this time format.
     */
    public boolean hasElapsed(long time, boolean reset, TimeFormat format) {
        boolean elapsed = hasElapsed(time, format);
        if (elapsed && reset) {
            resetTime();
        }

        return elapsed;
    }

    /**
     * Checks if we have elapsed time
     * @param time The time
     * @param format The time format
     * @return If we have elapsed time in this time format.
     */
    public boolean hasElapsed(long time, TimeFormat format) {
        switch (format) {
            case SECONDS: return passedSeconds(time);
            case MILLISECONDS: return passedMs(time);
            case TICKS: return passedTicks(time);
            case NANOSECONDS: return passedNs(time);
        }

        return false;
    }

    /**
     * If we have passed time in milliseconds
     * @param seconds The time in seconds
     * @return If we have passed this time
     */
    public boolean passedSeconds(long seconds) {
        // We can convert seconds into milliseconds by dividing seconds by 1 second in milliseconds.
        return passedMs(seconds / 1000L);
    }

    /**
     * If we have passed time in milliseconds
     * @param ms The time in milliseconds
     * @return If we have passed the time
     */
    public boolean passedMs(long ms) {
        return passedNs(ms * MS_TO_NS);
    }

    /**
     * If we have passed time in ticks
     * @param ticks The time in ticks
     * @return If we have passed the time
     */
    public boolean passedTicks(double ticks) {
        // Every 50 milliseconds is one tick
        long ms = (long) (ticks * 50L);
        return passedNs(ms * MS_TO_NS);
    }

    /**
     * If we have passed time in nanoseconds
     * @param ns The time in nanoseconds
     * @return If we have passed the time
     */
    public boolean passedNs(long ns) {
        return System.nanoTime() - lastTime >= ns;
    }

    /**
     * Resets the time to the current time
     */
    public void resetTime() {
        lastTime = System.nanoTime();
    }

    /**
     * Gets the overall elapsed time in a time format
     * @param format The time format
     * @return The elapsed time in the time format
     */
    public long getTime(TimeFormat format) {
        long elapsedNs = System.nanoTime();

        switch (format) {
            case SECONDS: return elapsedNs / S_TO_MS;
            case MILLISECONDS: return elapsedNs / MS_TO_NS;
            case NANOSECONDS: return elapsedNs;
            case TICKS: return (elapsedNs / MS_TO_NS) / 50L;
        }

        return elapsedNs;
    }
}
