package cope.nebula.client.feature.module;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.client.feature.ToggleableFeature;
import cope.nebula.client.value.Bind;
import cope.nebula.client.value.Value;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;

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
     * Dynamically loads values defined in the module class using reflections
     */
    public void registerAllSettings() {
        Arrays.stream(getClass().getFields())
                .filter((field) -> Value.class.isAssignableFrom(field.getType()))
                .forEach((field) -> {
                    field.setAccessible(true);

                    try {
                        values.add((Value) field.get(this));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
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

    /**
     * Gets display info for the HUD arraylist
     * @return the display info, or null if none
     */
    public String getDisplayInfo() {
        return null;
    }

    /**
     * Gets the full display string
     * @return the full display string
     */
    public String getDisplay() {
        String text = getName();
        if (getDisplayInfo() != null) {
            text += " " + ChatFormatting.GRAY + "[" + ChatFormatting.RESET + getDisplayInfo() + ChatFormatting.GRAY + "]";
        }

        return text;
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

    public Value getValue(String name) {
        for (Value value : values) {
            if (value.getName().equals(name)) {
                return value;
            }
        }

        return null;
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
