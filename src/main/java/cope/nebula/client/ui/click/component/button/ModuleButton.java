package cope.nebula.client.ui.click.component.button;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.render.ClickGUI;
import cope.nebula.util.renderer.FontUtil;
import cope.nebula.util.renderer.RenderUtil;
import cope.nebula.util.renderer.animation.Animation;
import cope.nebula.util.renderer.animation.AnimationDirection;

import java.awt.*;

public class ModuleButton extends Button {
    private final Module module;

    private boolean expanded = false;

    private final Animation toggleAnimation;

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;

        toggleAnimation = new Animation(getWidth(), 5L);
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
            RenderUtil.drawRectangle(getX(), getY(), toggleAnimation.getProgress(), getHeight(), new Color(122, 49, 183, 245).getRGB());
        }

        FontUtil.drawString(getName(), (int) (getX() + 3.0f), (int) ((getY() + (getHeight() / 2.0)) - FontUtil.getHeight() / 2.0), -1);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

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
}
