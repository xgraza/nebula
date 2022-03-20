package cope.nebula.client.ui.click.component;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.feature.module.render.ClickGUI;
import cope.nebula.client.ui.click.component.button.ModuleButton;
import cope.nebula.util.renderer.FontUtil;
import cope.nebula.util.renderer.RenderUtil;
import cope.nebula.util.renderer.animation.Animation;
import cope.nebula.util.renderer.animation.AnimationDirection;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;

/**
 * Represents a panel (or frame) that will hold all the components
 *
 * @author aesthetical
 * @since 3/12/22
 */
public class Panel extends Component {
    private boolean expanded = true;

    private boolean dragging = false;
    private double dragX, dragY;

    // im dumb, so thanks cosmos
    private double scroll = 0.0;

    // opening animation
    private final Animation animation = new Animation(200.0, 5L);

    public Panel(double x, ModuleCategory category, List<Module> modules) {
        super(category.getDisplayName());

        setX(x);
        setY(26.0);

        setWidth(112.0);
        setHeight(18.0);

        modules.forEach((module) -> children.add(new ModuleButton(module)));
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        if (dragging) {
            setX(dragX + mouseX);
            setY(dragY + mouseY);
        }

        if (isMouseWithinBounds(mouseX, mouseY, getX(), getY(), getWidth(), getHeight() + animation.getProgress())) {
            int scrollWheel = Mouse.getDWheel();
            if (scrollWheel > 0) {
                scroll += 10.0;
            } else if (scrollWheel < 0) {
                scroll -= 10.0;
            }
        }

        RenderUtil.drawHalfRoundedRectangle(getX(), getY(), getWidth(), getHeight(), 5.0, new Color(29, 29, 29).getRGB());
        FontUtil.drawString(getName(), (int) (getX() + 2.3f), (int) ((getY() + (getHeight() / 2.0)) - FontUtil.getHeight() / 2.0), -1);

        if (ClickGUI.animations.getValue()) {
            animation.setIncrement(ClickGUI.speed.getValue());
            animation.tick(AnimationDirection.fromBoolean(expanded));
        } else {
            animation.setProgress(expanded ? 200.0 : 0.0);
        }

        RenderUtil.scissor(getX(), getY() + getHeight(), getX() + getWidth(), getY() + animation.getProgress());
        RenderUtil.drawRectangle(getX(), getY() + getHeight(), getWidth(), animation.getProgress(), new Color(28, 28, 28, 226).getRGB());

        if (expanded) {
            double posY = getY() + getHeight() + scroll;
            for (Component component : children) {
                component.setX(getX() + 2.0);
                component.setY(posY);
                component.setWidth(getWidth() - 4.0);
                component.setHeight(15.0);

                component.drawComponent(mouseX, mouseY);

                posY += component.getHeight() + 1.0;
            }
        }

        RenderUtil.stopScissor();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseWithinBounds(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            switch (button) {
                case 0: {
                    dragging = true;

                    dragX = getX() - mouseX;
                    dragY = getY() - mouseY;
                    break;
                }

                case 1: {
                    expanded = !expanded;
                    break;
                }
            }
        }

        if (expanded && !dragging) {
            children.forEach((child) -> child.mouseClicked(mouseX, mouseY, button));
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0 && dragging) {
            dragging = false;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (expanded) {
            children.forEach((child) -> child.keyTyped(typedChar, keyCode));
        }
    }
}
