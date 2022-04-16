package cope.nebula.asm.mixins.world.block;

import cope.nebula.client.feature.module.world.Wallhack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    public void shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> info) {
        if (Wallhack.INSTANCE.isOn() && Wallhack.WHITELIST.contains((Block) (Object) this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
    public void getRenderLayer(CallbackInfoReturnable<BlockRenderLayer> info) {
        if (Wallhack.INSTANCE.isOn() && !Wallhack.WHITELIST.contains((Block) (Object) this)) {
            info.setReturnValue(BlockRenderLayer.TRANSLUCENT);
        }
    }

    @Inject(method = "getLightValue(Lnet/minecraft/block/state/IBlockState;)I", at = @At("HEAD"), cancellable = true)
    public void getLightValue(IBlockState state, CallbackInfoReturnable<Integer> info) {
        if (Wallhack.INSTANCE.isOn()) {
            info.setReturnValue(100);
        }
    }
}
