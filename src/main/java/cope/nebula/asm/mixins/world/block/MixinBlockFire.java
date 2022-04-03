package cope.nebula.asm.mixins.world.block;

import cope.nebula.client.feature.module.world.Avoid;
import cope.nebula.util.Globals;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockFire.class)
public class MixinBlockFire {
    @Inject(method = "getCollisionBoundingBox", at = @At("HEAD"), cancellable = true)
    public void getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<AxisAlignedBB> info) {
        if (Avoid.INSTANCE.isOn() && Avoid.fire.getValue() && Globals.mc.player.onGround) {
            AxisAlignedBB bb = Block.FULL_BLOCK_AABB.offset(pos);
            if (Globals.mc.player.getEntityBoundingBox().intersects(bb)) {
                return;
            }

            info.setReturnValue(Block.FULL_BLOCK_AABB);
        }
    }
}
