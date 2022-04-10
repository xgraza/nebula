package cope.nebula.client.feature.module.other;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.entity.player.EntityPlayer;

public class Notifier extends Module {
    public static Notifier INSTANCE;

    public Notifier() {
        super("Notifier", ModuleCategory.OTHER, "Notifies you about shit");
        INSTANCE = this;
    }

    public static final Value<Boolean> totems = new Value<>("Totems", true);
    public static final Value<Boolean> self = new Value<>(totems, "Self", false);

    public static boolean shouldTotemPopNotify(EntityPlayer player) {
        if (player != null && player.equals(mc.player) && !self.getValue()) {
            return false;
        }

        return INSTANCE.isOn() && totems.getValue();
    }
}
