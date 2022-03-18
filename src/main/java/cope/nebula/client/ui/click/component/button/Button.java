package cope.nebula.client.ui.click.component.button;

import cope.nebula.client.ui.click.component.Component;

public abstract class Button extends Component {
    public Button(String name) {
        super(name);
    }

    public abstract void onClick(int button);

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseInBounds(mouseX, mouseY)) {
            playClickSound();
            onClick(button);
        }
    }
}
