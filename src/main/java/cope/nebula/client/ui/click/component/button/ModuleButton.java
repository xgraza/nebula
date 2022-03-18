package cope.nebula.client.ui.click.component.button;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.render.ClickGUI;
import cope.nebula.util.renderer.FontUtil;
import cope.nebula.util.renderer.RenderUtil;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class ModuleButton extends Button {
    private final Module module;

    private boolean expanded = false;

    private long time = System.currentTimeMillis();
    private boolean animationState = false;
    private double progress = 0.0;

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        if (ClickGUI.animations.getValue()) {
            if (System.currentTimeMillis() - time >= 5L) {
                time = System.currentTimeMillis();
                double speed = ClickGUI.speed.getValue();

                if (animationState) {
                    progress += speed;
                } else {
                    progress -= speed;
                }

                progress = MathHelper.clamp(progress, 0, getWidth());
            }
        } else {
            if (animationState) {
                progress = getWidth();
            } else {
                progress = 0.0;
            }
        }

        // TODO: animation shit, im too lazy
        if (progress > 0.0) {
            RenderUtil.drawRectangle(getX(), getY(), progress, getHeight(), new Color(122, 49, 183, 245).getRGB());
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

                time = System.currentTimeMillis();
                animationState = module.isOn();
                break;
            }

            case 1: {
                expanded = !expanded;
                break;
            }
        }
    }
}
