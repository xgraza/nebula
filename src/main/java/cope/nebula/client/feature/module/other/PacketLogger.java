package cope.nebula.client.feature.module.other;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PacketLogger extends Module {
    public PacketLogger() {
        super("PacketLogger", ModuleCategory.OTHER, "Logs packets");
    }

    public static final Value<Boolean> send = new Value<>("Send", true);
    public static final Value<Boolean> receive = new Value<>("Receive", false);

    public static final Value<Boolean> values = new Value<>("Fields", false);

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.OUTGOING) && !send.getValue()) {
            return;
        }

        if (event.getDirection().equals(Direction.INCOMING) && !receive.getValue()) {
            return;
        }

        sendDeconstructedPacket(event.getPacket());
    }

    private void sendDeconstructedPacket(Packet<?> packet) {
        StringBuilder text = new StringBuilder(packet.getClass().getSimpleName());

        if (!values.getValue()) {
            sendChatMessage(text.toString());
            return;
        }

        List<Field> fields = new ArrayList<>();

        // we need to do some wizard shit to get fields from super classes and what not
        Class<?> clazz = packet.getClass();
        while (clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                boolean access = field.isAccessible();
                if (!access) {
                    field.setAccessible(true);
                }

                fields.add(field);
                field.setAccessible(access);
            }

            clazz = clazz.getSuperclass();
        }

        text.append(" ");
        for (Field field : fields) {
            try {
                text.append(field.getName()).append(": ").append(field.get(packet)).append(" ");
            } catch (IllegalAccessException ignored) {

            }
        }

        sendChatMessage(text.toString());
    }
}
