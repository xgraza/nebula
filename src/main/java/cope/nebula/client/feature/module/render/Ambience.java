package cope.nebula.client.feature.module.render;

import cope.nebula.asm.mixins.network.packet.s2c.ISPacketTimeUpdate;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class Ambience extends Module {
    public Ambience() {
        super("Ambience", ModuleCategory.RENDER, "Changes the look and feel of the game world");
    }

    public static final Value<Color> color = new Value<>("Color", new Color(255, 255, 255));
    public static final Value<Integer> time = new Value<>("Time", 0, 0, 24000);

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING) && event.getPacket() instanceof SPacketTimeUpdate) {
            ((ISPacketTimeUpdate) event.getPacket()).setWorldTime(time.getValue());
        }
    }
}
