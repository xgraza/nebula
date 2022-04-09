package cope.nebula.asm.mixins.network.packet.c2s;

import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketPlayerTryUseItemOnBlock.class)
public interface ICPacketPlayerTryUseItemOnBlock {
    @Accessor("placedBlockDirection") void setPlacedBlockDirection(EnumFacing placedBlockDirection);
}
