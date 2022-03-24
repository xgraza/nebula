package cope.nebula.client.feature.module.other;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", ModuleCategory.OTHER, "Automatically respawns you");
    }

    public static final Value<Boolean> disable = new Value<>("Disable", false);

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiGameOver) {
            mc.player.connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
            if (disable.getValue()) {
                disable();
            }
        }
    }
}
