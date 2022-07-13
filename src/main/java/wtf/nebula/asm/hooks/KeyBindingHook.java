package wtf.nebula.asm.hooks;

import net.minecraft.client.settings.KeyBinding;
import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.input.KeyBindingPressedEvent;

public class KeyBindingHook {
    public static boolean pressedHook(KeyBinding binding) {
        KeyBindingPressedEvent event = new KeyBindingPressedEvent(binding);
        Nebula.Companion.getBUS().post(event);
        return event.getPressed();
    }
}
