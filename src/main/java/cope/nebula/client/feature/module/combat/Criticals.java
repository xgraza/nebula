package cope.nebula.client.feature.module.combat;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Criticals extends Module {
    public Criticals() {
        super("Criticals", ModuleCategory.COMBAT, "Makes you do critical hits");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.STRICT);

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof CPacketUseEntity && event.getDirection().equals(Direction.OUTGOING)) {
            CPacketUseEntity packet = event.getPacket();
            if (packet.getAction().equals(Action.ATTACK) && mc.player.onGround) {
                Entity entity = packet.getEntityFromWorld(mc.world);
                if (entity == null || !(entity instanceof EntityLivingBase) || entity.equals(mc.player)) {
                    return;
                }

                double minY = mc.player.getEntityBoundingBox().minY;

                switch (mode.getValue()) {
                    case BASIC:
                        mc.player.connection.sendPacket(new Position(mc.player.posX, minY + 0.2, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new Position(mc.player.posX, minY, mc.player.posZ, false));
                        break;

                    case STRICT:
                        mc.player.connection.sendPacket(new Position(mc.player.posX, minY + 0.062602401692772D, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new Position(mc.player.posX, minY + 0.0726023996066094D, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new Position(mc.player.posX, minY, mc.player.posZ, false));
                        break;
                }
            }
        }
    }

    public enum Mode {
        BASIC, STRICT
    }
}
