package cope.nebula.client.ui.click.component.button;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.client.value.Bind;
import cope.nebula.util.renderer.FontUtil;
import org.lwjgl.input.Keyboard;

public class BindButton extends Button {
    private final Bind bind;
    private boolean listening = false;

    private long time = System.currentTimeMillis();
    private int periods = 0;

    public BindButton(Bind bind) {
        super(bind.getName());
        this.bind = bind;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        StringBuilder text;

        if (listening) {
            if (System.currentTimeMillis() - time >= 500L) {
                time = System.currentTimeMillis();

                ++periods;
                if (periods > 3) {
                    periods = 0;
                }
            }

            text = new StringBuilder("Listening");
            for (int i = 0; i < periods; ++i) {
                text.append(".");
            }
        } else {
            text = new StringBuilder("Bind: " + ChatFormatting.GRAY + Keyboard.getKeyName(bind.getValue()));
        }

        FontUtil.drawString(text.toString(), (float) (getX() + 2.3), (float) ((getY() + (getHeight() / 2.0)) - FontUtil.getHeight() / 2.0), -1);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (listening) {
            listening = false;

            if (keyCode == Keyboard.KEY_NONE || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.EVENT_SIZE) {
                bind.setValue(Keyboard.KEY_NONE);
                return;
            }

            bind.setValue(keyCode);
        }
    }

    @Override
    public void onClick(int button) {
        listening = !listening;
    }
}
