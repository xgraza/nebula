package cope.nebula.client.feature.module.movement;

import cope.nebula.asm.mixins.network.packet.c2s.ICPacketPlayer;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.renderer.FontUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", ModuleCategory.MOVEMENT, "Stops you from falling down");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.BASIC);
    public static final Value<Float> fallDistance = new Value<>("FallDistance", 3.0f, 3.0f, 20.0f);

    @Override
    public String getDisplayInfo() {
        return FontUtil.formatText(mode.getValue().name());
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.OUTGOING) &&
                event.getPacket() instanceof CPacketPlayer &&
                mc.player.fallDistance >= fallDistance.getValue() &&
                mode.getValue().equals(Mode.BASIC)) {

            ((ICPacketPlayer) event.getPacket()).setOnGround(true);
        }
    }

    @Override
    public void onTick() {
        if (mc.player.fallDistance >= fallDistance.getValue()) {
            if (mode.getValue().equals(Mode.SETBACK)) {
                mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + mc.player.fallDistance, mc.player.posZ, true));
            } else if (mode.getValue().equals(Mode.NCP)) {
                // TODO
            }
        }
    }

    public enum Mode {
        BASIC, SETBACK, NCP
    }
}
