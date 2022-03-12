package cope.nebula.client.feature.module;

import cope.nebula.client.feature.ToggleableFeature;
import cope.nebula.client.value.Bind;
import cope.nebula.client.value.Value;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

/**
 * A base class for a module
 *
 * @author aesthetical
 * @since 3/7/22
 */
@SuppressWarnings("rawtypes")
public class Module extends ToggleableFeature {
    private final ModuleCategory category;
    private final String description;

    private final ArrayList<Value> values = new ArrayList<>();
    protected final Value<Boolean> drawn = new Value<>("Drawn", true);
    protected final Bind bind = new Bind("Keybind", Keyboard.KEY_NONE);

    public Module(String name, ModuleCategory category, String description) {
        super(name);

        this.category = category;
        this.description = description;

        values.add(drawn);
        values.add(bind);
    }

    /**
     * Called upon a client tick
     */
    public void onTick() {

    }

    /**
     * Called upon a living update of the local player
     */
    public void onUpdate() {

    }

    /**
     * Called upon a HUD render
     */
    public void onRender2d() {

    }

    /**
     * Called upon a world render
     */
    public void onRender3d() {

    }

    public void setKeyBind(int keyCode) {
        bind.setValue(keyCode);
    }

    public int getKeyBind() {
        return bind.getValue();
    }

    public boolean isDrawn() {
        return drawn.getValue();
    }

    public ArrayList<Value> getValues() {
        return values;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
}
