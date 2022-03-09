package cope.nebula.client.manager.macro;

import cope.nebula.client.Nebula;
import cope.nebula.util.Globals;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

/**
 * Holds all the client's macros
 *
 * @author aesthetical
 * @since 3/8/22
 */
public class MacroManager implements Globals {
    private final ArrayList<Macro> macros = new ArrayList<>();

    public MacroManager() {
        EVENT_BUS.register(this);
        Nebula.getLogger().info("Loaded {} macros", macros.size());
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        int keyCode = Keyboard.getEventKey();
        if (keyCode != Keyboard.KEY_NONE && mc.currentScreen == null && !Keyboard.getEventKeyState()) {
            macros.forEach((macro) -> {
                if (macro.getKeyCode() == keyCode) {
                    mc.player.connection.sendPacket(new CPacketChatMessage(macro.getText()));
                }
            });
        }
    }

    /**
     * Adds a macro to the client
     * @param macro The macro object
     */
    public void add(Macro macro) {
        if (macro.getKeyCode() != Keyboard.KEY_NONE) {
            macros.add(macro);
        }
    }

    /**
     * Removes all macros that use this bind
     * @param bind The bind the macro uses to activate
     */
    public void remove(int bind) {
        macros.removeIf((macro) -> macro.getKeyCode() == bind);
    }
}
