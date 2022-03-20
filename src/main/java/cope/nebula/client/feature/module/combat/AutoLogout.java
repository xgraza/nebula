package cope.nebula.client.feature.module.combat;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.EntityUtil;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextComponentString;

public class AutoLogout extends Module {
    public AutoLogout() {
        super("AutoLogout", ModuleCategory.COMBAT, "*cringe* Logs out when something happens");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.HEALTH);

    public static final Value<Float> health = new Value<>("Health", 10.0f, 1.0f, 20.0f);
    public static final Value<Integer> totems = new Value<>("Totems", 0, 0, 10);

    @Override
    public void onTick() {
        switch (mode.getValue()) {
            case TOTEMS: {
                int totemCount = (int) mc.player.inventory.mainInventory.stream()
                        .filter((stack) -> stack.getItem().equals(Items.TOTEM_OF_UNDYING))
                        .count();

                if (totemCount < totems.getValue()) {
                    logout();
                }

                break;
            }

            case HEALTH: {
                if (EntityUtil.getHealth(mc.player) < health.getValue()) {
                    logout();
                }

                break;
            }
        }
    }

    private void logout() {
        mc.player.connection.getNetworkManager().closeChannel(new TextComponentString("AutoLogout"));
    }

    public enum Mode {
        TOTEMS,
        HEALTH
    }
}
