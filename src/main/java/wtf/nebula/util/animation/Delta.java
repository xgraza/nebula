package wtf.nebula.util.animation;

import org.lwjgl.Sys;

public class Delta {
    public static int DELTATIME;

    public static long lastFrameTime = getTime();

    public static void updateDelta() {
        long currentFrameTime = getTime();
        DELTATIME = (int) (currentFrameTime - lastFrameTime);
        lastFrameTime = currentFrameTime;
    }

    public static long getTime() {
        return (Sys.getTime() * 1000L) / Sys.getTimerResolution();
    }
}
