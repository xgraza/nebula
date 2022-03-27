package cope.nebula.asm.mixins.entity.player;

import cope.nebula.asm.duck.IEntityPlayer;
import cope.nebula.client.Nebula;
import cope.nebula.client.events.EntityPushedByWaterEvent;
import cope.nebula.client.manager.relation.Relationship;
import cope.nebula.client.manager.relation.RelationshipManager;
import cope.nebula.util.Globals;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IEntityPlayer {
    @Shadow public abstract String getName();

    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "isPushedByWater", at = @At("HEAD"), cancellable = true)
    public void isPushedByWater(CallbackInfoReturnable<Boolean> info) {
        EntityPushedByWaterEvent event = new EntityPushedByWaterEvent((EntityPlayer) (Object) this);
        Globals.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Override
    public boolean isFriend() {
        RelationshipManager manager = Nebula.getInstance().getRelationshipManager();
        return manager.isFriend(getUniqueID());
    }

    @Override
    public boolean friend() {
        RelationshipManager manager = Nebula.getInstance().getRelationshipManager();

        if (isFriend()) {
            manager.delete(getUniqueID());
            return false;
        } else {
            manager.add(new Relationship(getUniqueID(), getName()));
            return true;
        }
    }
}
