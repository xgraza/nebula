package cope.nebula.client.ui.click.component.other;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.client.ui.click.component.Component;
import cope.nebula.client.value.Value;
import cope.nebula.util.renderer.FontUtil;
import cope.nebula.util.renderer.RenderUtil;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class Slider extends Component {
    private final Value<Number> value;
    private final float difference;

    public Slider(Value<Number> value) {
        super(value.getName());

        this.value = value;
        this.difference = value.getMax().floatValue() - value.getMin().floatValue();
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        if (Mouse.isButtonDown(0) && isMouseInBounds(mouseX, mouseY)) {
            setValue(mouseX);
        }

        double width = value.getValue().floatValue() <= value.getMin().floatValue() ? 0.0 : getWidth() * partialMultiplier();
        RenderUtil.drawRectangle(getX(), getY(), width, getHeight(), new Color(122, 49, 183, 245).getRGB());
        FontUtil.drawString(getName() + " " + ChatFormatting.GRAY + value.getValue(), (float) (getX() + 2.3), (float) ((getY() + (15.0 / 2.0)) - FontUtil.getHeight() / 2.0), -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (Mouse.isButtonDown(0) && isMouseInBounds(mouseX, mouseY)) {
            setValue(mouseX);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    /**
     * Sets the value of the slider based off the mouseX value
     * @param mouseX the mouse x value
     */
    private void setValue(int mouseX) {
        float percent = (float) ((mouseX - getX()) / getWidth());

        if (value.getValue() instanceof Float) {
            float result = value.getMin().floatValue() + this.difference * percent;
            value.setValue(Math.round(10.0f * result) / 10.0f);
        } else if (value.getValue() instanceof Double) {
            double result = value.getMin().doubleValue() + difference * percent;
            value.setValue(Math.round(10.0 * result) / 10.0);
        } else {
            value.setValue(Math.round(value.getMin().intValue() + difference * percent));
        }
    }

    private float part() {
        return value.getValue().floatValue() - value.getMin().floatValue();
    }

    private float partialMultiplier() {
        return this.part() / this.difference;
    }
}
