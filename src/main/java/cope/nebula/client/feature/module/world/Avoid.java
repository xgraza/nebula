package cope.nebula.client.feature.module.world;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;

public class Avoid extends Module {
    public static Avoid INSTANCE;

    public Avoid() {
        super("Avoid", ModuleCategory.WORLD, "Avoids stuff");
        INSTANCE = this;
    }

    public static final Value<Boolean> cactus = new Value<>("Cactus", true);
    public static final Value<Boolean> fire = new Value<>("Fire", true);
    public static final Value<Boolean> unloaded = new Value<>("Unloaded", true);
    public static final Value<Boolean> theVoid = new Value<>("Void", true);

    @Override
    public void onTick() {
        if (theVoid.getValue() && mc.player.posY <= 0.0) {
            mc.player.motionY = 0.0; // hold onto dear life
        }

        if (unloaded.getValue() && !mc.world.getChunk(mc.player.getPosition()).isLoaded()) {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
        }
    }
}
