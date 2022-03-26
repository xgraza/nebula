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

    public static final Value<Mode> mode = new Value<>("Mode", Mode.PACKET);
    public static final Value<Boolean> particles = new Value<>("Particles", false);

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof CPacketUseEntity && event.getDirection().equals(Direction.OUTGOING)) {
            CPacketUseEntity packet = event.getPacket();
            if (packet.getAction().equals(Action.ATTACK) && mc.player.onGround) {
                Entity entity = packet.getEntityFromWorld(mc.world);
                if (!(entity instanceof EntityLivingBase) || entity.equals(mc.player)) {
                    return;
                }

                double minY = mc.player.getEntityBoundingBox().minY;

                switch (mode.getValue()) {
                    case PACKET:
                        mc.player.connection.sendPacket(new Position(mc.player.posX, minY + 0.0625, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new Position(mc.player.posX, minY, mc.player.posZ, false));
                        break;

                    case STRICT:
                        // bruh.... these were the working ones all along
                        mc.player.connection.sendPacket(new Position(mc.player.posX, minY + 0.11, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new Position(mc.player.posX, minY + 0.1100013579, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new Position(mc.player.posX, minY + 0.0000013579, mc.player.posZ, false));
                        break;
                }

                // so critical hits will show up on fake players
                if (particles.getValue()) {
                    mc.player.onCriticalHit(entity);
                }
            }
        }
    }

    public enum Mode {
        PACKET, STRICT
    }
}
