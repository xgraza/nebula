package cope.nebula.client.manager;

import com.google.common.collect.Lists;
import cope.nebula.client.Nebula;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.combat.*;
import cope.nebula.client.feature.module.exploit.*;
import cope.nebula.client.feature.module.movement.*;
import cope.nebula.client.feature.module.render.*;
import cope.nebula.client.feature.module.world.*;
import cope.nebula.util.Globals;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

/**
 * Holds all modules of the client
 *
 * Also handles any key inputs needed to toggle modules
 *
 * @author aesthetical
 * @since 3/7/22
 */
public class ModuleManager implements Globals {
    private final ArrayList<Module> modules = Lists.newArrayList(
            // Combat
            new Aura(),
            new AutoCrystal(),
            new AutoLogout(),
            new AutoTotem(),
            new BowRelease(),
            new Criticals(),
            new FeetTrap(),
            new HitboxCity(),
            new SelfFill(),

            // Exploit
            new ChorusControl(),
            new PacketFly(),
            new PearlBait(),
            new PingSpoof(),
            new TickShift(),
            new XCarry(),

            // Movement
            new HoleSnap(),
            new Jesus(),
            new NoSlowDown(),
            new ReverseStep(),
            new Speed(),
            new Sprint(),
            new Step(),
            new Velocity(),

            // Render
            new AntiRender(),
            new Brightness(),
            new ClickGUI(),
            new CustomFont(),
            new HUD(),

            // World
            new FastBreak(),
            new FastPlace(),
            new NoGlitchBlocks(),
            new Scaffold(),
            new Timer()
    );

    public ModuleManager() {
        EVENT_BUS.register(this);
        Nebula.getLogger().info("Loaded {} modules", modules.size());
        modules.forEach(Module::registerAllSettings);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        int keyCode = Keyboard.getEventKey();
        if (keyCode != Keyboard.KEY_NONE && !Keyboard.getEventKeyState() && mc.currentScreen == null) {
            modules.forEach((module) -> {
                if (module.getKeyBind() == keyCode) {
                    module.toggle();
                }
            });
        }
    }

    public ArrayList<Module> getModules() {
        return modules;
    }
}
