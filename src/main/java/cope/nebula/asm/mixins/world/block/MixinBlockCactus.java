package cope.nebula.asm.mixins.world.block;

import cope.nebula.client.feature.module.world.Avoid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockCactus.class)
public class MixinBlockCactus {
    @Inject(method = "getCollisionBoundingBox", at = @At("HEAD"), cancellable = true)
    public void getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<AxisAlignedBB> info) {
        if (Avoid.INSTANCE.isOn() && Avoid.cactus.getValue()) {
            info.setReturnValue(Block.FULL_BLOCK_AABB);
        }
    }
}
