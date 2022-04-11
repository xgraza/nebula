package cope.nebula.client.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class StepEvent extends Event {
    private float stepHeight;

    public StepEvent(float stepHeight) {
        this.stepHeight = stepHeight;
    }

    public void setStepHeight(float stepHeight) {
        this.stepHeight = stepHeight;
    }

    public float getStepHeight() {
        return stepHeight;
    }
}
