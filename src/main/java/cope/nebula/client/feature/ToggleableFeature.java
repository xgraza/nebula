package cope.nebula.client.feature;

/**
 * Represents a feature that is toggleable
 *
 * @author aesthetical
 * @since 3/7/22
 */
public class ToggleableFeature extends Feature {
    private boolean state = false;

    public ToggleableFeature(String name) {
        super(name);
    }

    /**
     * Called once this feature is activated
     *
     * This method is meant to be overwritten
     */
    protected void onActivated() {

    }

    /**
     * Called once this feature is deactivated
     *
     * This method is meant to be overwritten
     */
    protected void onDeactivated() {

    }

    /**
     * Enables this feature
     */
    public void enable() {
        EVENT_BUS.register(this);
        onActivated();

        state = true;
    }

    /**
     * Disables this feature
     */
    public void disable() {
        EVENT_BUS.unregister(this);
        onDeactivated();

        state = false;
    }

    /**
     * Toggles this feature
     */
    public void toggle() {
        setState(!state);
    }

    /**
     * Directly sets the state of this feature
     * @param in The state of the feature
     */
    public void setState(boolean in) {
        state = in;
        if (state) {
            enable();
        } else {
            disable();
        }
    }

    /**
     * If this feature is on
     * @return If state is true
     */
    public boolean isOn() {
        return state;
    }

    /**
     * If this feature is off
     * @return If state is false
     */
    public boolean isOff() {
        return !state;
    }
}
