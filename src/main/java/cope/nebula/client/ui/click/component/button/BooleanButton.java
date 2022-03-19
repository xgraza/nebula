package cope.nebula.client.ui.click.component.button;

import cope.nebula.client.value.Value;
import cope.nebula.util.renderer.FontUtil;
import cope.nebula.util.renderer.RenderUtil;

import java.awt.*;

public class BooleanButton extends Button {
    private final Value<Boolean> value;

    public BooleanButton(Value<Boolean> value) {
        super(value.getName());
        this.value = value;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        // TODO: animations
        if (value.getValue()) {
            // RenderUtil.drawRectangle(getX(), getY(), toggleAnimation.getProgress(), 15.0, new Color(122, 49, 183, 245).getRGB());
            RenderUtil.drawRectangle(getX(), getY(), getWidth(), 15.0, new Color(122, 49, 183, 245).getRGB());
        }

        FontUtil.drawString(getName(), (float) (getX() + 2.3), (float) ((getY() + (getHeight() / 2.0)) - FontUtil.getHeight() / 2.0), -1);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void onClick(int button) {
        value.setValue(!value.getValue());
    }
}
