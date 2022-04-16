package cope.nebula.asm.mixins.input;

import cope.nebula.client.events.BlockInteractEvent;
import cope.nebula.client.events.ClickBlockEvent;
import cope.nebula.util.Globals;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(method = "clickBlock", at = @At("HEAD"), cancellable = true)
    public void clickBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
        if (Globals.EVENT_BUS.post(new ClickBlockEvent(loc, face))) {
            info.cancel();
        }
    }

    @Inject(method = "processRightClickBlock", at = @At("HEAD"), cancellable = true)
    public void processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand, CallbackInfoReturnable<EnumActionResult> info) {
        BlockInteractEvent event = new BlockInteractEvent(pos, worldIn.getBlockState(pos).getBlock(), vec, direction, hand);
        Globals.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }
}
