package cope.nebula.client.feature.module.other;

import com.google.common.collect.Lists;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class NoSoundLag extends Module {
    private final List<SoundEvent> BLACKLIST = Lists.newArrayList(

            // Usually stuff for offhand crashing/armor equips
            SoundEvents.ITEM_ARMOR_EQIIP_ELYTRA,
            SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
            SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,

            // If a server gets raided (aka 2b2tpvp, a bunch of players at spawn will be stepping and it'll lag you out a bit)
            SoundEvents.BLOCK_ANVIL_STEP,
            SoundEvents.BLOCK_CLOTH_STEP,
            SoundEvents.BLOCK_GLASS_STEP,
            SoundEvents.BLOCK_GRASS_STEP,
            SoundEvents.BLOCK_GRAVEL_STEP,
            SoundEvents.BLOCK_LADDER_STEP,
            SoundEvents.BLOCK_METAL_STEP,
            SoundEvents.BLOCK_SAND_STEP,
            SoundEvents.BLOCK_SLIME_STEP,
            SoundEvents.BLOCK_SNOW_STEP,
            SoundEvents.BLOCK_STONE_STEP,
            SoundEvents.BLOCK_WOOD_STEP
    );

    public NoSoundLag() {
        super("NoSoundLag", ModuleCategory.OTHER, "Stops sounds from lagging you out");
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING) && event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect packet = event.getPacket();
            if (BLACKLIST.contains(packet.getSound())) {
                event.setCanceled(true);
            }
        }
    }
}
