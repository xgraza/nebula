package cope.nebula.asm.mixins.network.packet.c2s;

import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketUseEntity.class)
public interface ICPacketUseEntity {
    @Accessor("entityId") void setEntityId(int entityId);

    @Accessor("action") void setAction(Action action);
}
