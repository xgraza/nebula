package cope.nebula.client.feature.module.render;

import com.google.common.collect.Lists;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class AntiRender extends Module {
    public static AntiRender INSTANCE;

    public static final List<EnumParticleTypes> EXPLOSION_PARTICLES = Lists.newArrayList(
            EnumParticleTypes.EXPLOSION_HUGE,
            EnumParticleTypes.EXPLOSION_LARGE,
            EnumParticleTypes.EXPLOSION_NORMAL,
            EnumParticleTypes.SMOKE_LARGE,
            EnumParticleTypes.SMOKE_NORMAL
    );

    public AntiRender() {
        super("AntiRender", ModuleCategory.RENDER, "Stops things from rendering");
        INSTANCE = this;
    }

    public static final Value<Boolean> fire = new Value<>("Fire", true);
    public static final Value<Boolean> hurtcam = new Value<>("Hurtcam", true);
    public static final Value<Boolean> blocks = new Value<>("Blocks", true);
    public static final Value<Boolean> vignette = new Value<>("Vignette", true);
    public static final Value<Boolean> totems = new Value<>("Totems", true);
    public static final Value<Boolean> particles = new Value<>("Particles", false);
    public static final Value<Boolean> explosions = new Value<>("Explosions", true);
    public static final Value<Boolean> tile = new Value<>("Tile", false);
    public static final Value<Boolean> fog = new Value<>("Fog", false);
    public static final Value<Boolean> armor = new Value<>("Armor", false);
    public static final Value<Boolean> maps = new Value<>("Maps", false);
    public static final Value<Boolean> xp = new Value<>("XP", true);
    public static final Value<Boolean> toasts = new Value<>("Toasts", true); // "Break a tree!" FUCK YOU
    public static final Value<Effects> effects = new Value<>("Effects", Effects.HIDE);

    @SubscribeEvent
    public void onBlockOverlay(RenderBlockOverlayEvent event) {
        if (event.getOverlayType().equals(OverlayType.BLOCK) || event.getOverlayType().equals(OverlayType.WATER)) {
            event.setCanceled(blocks.getValue());
        } else {
            event.setCanceled(fire.getValue());
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING) && event.getPacket() instanceof SPacketParticles) {
            SPacketParticles packet = event.getPacket();
            if (explosions.getValue() && EXPLOSION_PARTICLES.contains(packet.getParticleType())) {
                event.setCanceled(true);
            }
        }
    }

    public enum Effects {
        HIDE, BACKGROUND
    }
}
