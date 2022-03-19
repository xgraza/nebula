package cope.nebula.client.ui.click.component.button;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.client.value.Value;
import cope.nebula.util.renderer.FontUtil;

@SuppressWarnings("rawtypes")
public class EnumButton extends Button {
    private final Value<Enum> value;

    public EnumButton(Value<Enum> value) {
        super(value.getName());
        this.value = value;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        FontUtil.drawString(getName() + ": " + ChatFormatting.GRAY + value.getValue().name(), (float) (getX() + 2.3), (float) ((getY() + (getHeight() / 2.0)) - FontUtil.getHeight() / 2.0), -1);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void onClick(int button) {
        value.setValue(Value.increase(value.getValue()));
    }
}
