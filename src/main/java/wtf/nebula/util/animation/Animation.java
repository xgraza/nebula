package wtf.nebula.util.animation;

public class Animation {

    private float value, min, max, speed, time;
    private boolean reversed;
    private Easing ease;

    public Animation() {
        this.ease = Easing.LINEAR;
        this.value = 0;
        this.min = 0;
        this.max = 1;
        this.speed = 50;
        this.reversed = false;
    }

    public void reset() {
        time = min;
    }

    public Animation update() {
        if (reversed) {
            if (time > min) {
                time -= (Delta.DELTATIME * .001F * speed);
            }
        } else {
            if (time < max) {
                time += (Delta.DELTATIME * .001F * speed);
            }
        }

        time = clamp(time, min, max);

        float easeVal = getEase().ease(time, min, max, max);
        this.value = Math.min(easeVal, max);

        return this;
    }

    public Animation setValue(float value) {
        this.value = value;
        return this;
    }
    public Animation setMin(float min) {
        this.min = min;
        return this;
    }

    public Animation setMax(float max) {
        this.max = max;
        return this;
    }

    public Animation setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public Animation setReversed(boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    public Animation setEase(Easing ease) {
        this.ease = ease;
        return this;
    }

    public float getValue() {
        return value;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getSpeed() {
        return speed;
    }

    public float getTime() {
        return time;
    }

    public boolean isReversed() {
        return reversed;
    }

    public Easing getEase() {
        return ease;
    }

    private float clamp(float num, float min, float max) {
        return num < min ? min : (Math.min(num, max));
    }
}