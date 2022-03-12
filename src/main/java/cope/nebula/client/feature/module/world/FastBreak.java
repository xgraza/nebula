package cope.nebula.client.feature.module.world;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.player.inventory.SwapType;

public class FastBreak extends Module {
    public FastBreak() {
        super("FastBreak", ModuleCategory.WORLD, "Breaks blocks faster");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.PACKET);
    public static final Value<Break> breakMode = new Value<>("Break", Break.AUTOMATIC);
    public static final Value<Boolean> rotate = new Value<>("Rotate", true);
    public static final Value<SwapType> swap = new Value<>("Swap", SwapType.SERVER);


    public enum Mode {
        PACKET, VANILLA
    }

    public enum Break {
        AUTOMATIC, STRICT, MANUAL
    }
}
