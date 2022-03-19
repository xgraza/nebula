package cope.nebula.client.ui.click.component.button;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.render.ClickGUI;
import cope.nebula.client.ui.click.component.Component;
import cope.nebula.client.value.Bind;
import cope.nebula.client.value.Value;
import cope.nebula.util.renderer.FontUtil;
import cope.nebula.util.renderer.RenderUtil;
import cope.nebula.util.renderer.animation.Animation;
import cope.nebula.util.renderer.animation.AnimationDirection;

import java.awt.*;

@SuppressWarnings("rawtypes")
public class ModuleButton extends Button {
    private final Module module;

    private boolean expanded = false;

    private final Animation toggleAnimation;

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;

        toggleAnimation = new Animation(getWidth(), 5L);

        for (Value value : module.getValues()) {
            if (value instanceof Bind) {
                children.add(new BindButton((Bind) value));
                continue;
            }

            if (value.getValue() instanceof Boolean) {
                children.add(new BooleanButton(value));
            } else if (value.getValue() instanceof Enum) {
                children.add(new EnumButton(value));
            }
        }
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        toggleAnimation.setMax(getWidth());
        if (ClickGUI.animations.getValue()) {
            toggleAnimation.setIncrement(ClickGUI.speed.getValue());
            toggleAnimation.tick(AnimationDirection.fromBoolean(module.isOn()));
        } else {
            toggleAnimation.setIncrement(module.isOn() ? getWidth() : 0.0);
        }

        if (toggleAnimation.getProgress() > 0.0) {
            RenderUtil.drawRectangle(getX(), getY(), toggleAnimation.getProgress(), 15.0, new Color(122, 49, 183, 245).getRGB());
        }

        FontUtil.drawString(getName(), (int) (getX() + 3.0f), (int) ((getY() + (15.0 / 2.0)) - FontUtil.getHeight() / 2.0), -1);

        // TODO: opening/closing animations
        if (expanded) {
            double posY = getY() + 15.0;
            for (Component component : children) {
                component.setX(getX() + 2.0);
                component.setY(posY);
                component.setWidth(getWidth() - 4.0);
                component.setHeight(15.0);

                component.drawComponent(mouseX, mouseY);

                posY += component.getHeight() + 1.0;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (expanded) {
            children.forEach((child) -> child.mouseClicked(mouseX, mouseY, button));
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (expanded) {
            children.forEach((child) -> child.keyTyped(typedChar, keyCode));
        }
    }

    @Override
    public void onClick(int button) {
        switch (button) {
            case 0: {
                module.toggle();
                break;
            }

            case 1: {
                expanded = !expanded;
                break;
            }
        }
    }

    @Override
    public double getHeight() {
        double height = 15.0;

        if (expanded) {
            for (Component component : children) {
                height += component.getHeight() + 1.0;
            }
        }

        return height;
    }
}
