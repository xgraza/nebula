package cope.nebula.client.ui.click.component;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;

import java.util.List;

/**
 * Represents a panel (or frame) that will hold all the components
 *
 * @author aesthetical
 * @since 3/12/22
 */
public class Panel extends Component {
    public Panel(ModuleCategory category, List<Module> modules) {
        super(category.getDisplayName());

        // TODO
        modules.forEach((module) -> children.add(null));
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }
}
