package cope.nebula.client.feature.module.combat;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Criticals extends Module {
    public Criticals() {
        super("Criticals", ModuleCategory.COMBAT, "Makes you do critical hits");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.BASIC);

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof CPacketUseEntity && event.getDirection().equals(Direction.OUTGOING)) {
            CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
            if (packet.getAction().equals(Action.ATTACK) && packet.getEntityFromWorld(mc.world) instanceof EntityLivingBase && mc.player.onGround) {
                switch (mode.getValue()) {
                    case BASIC: {
                        mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 0.21, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                        break;
                    }

                    case STRICT: {
                        // TODO
                        break;
                    }

                    case JUMP: {
                        mc.player.jump();
                        break;
                    }
                }
            }
        }
    }

    public enum Mode {
        BASIC, STRICT, JUMP
    }
}
