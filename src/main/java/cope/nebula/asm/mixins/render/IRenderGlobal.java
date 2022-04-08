package cope.nebula.asm.mixins.render;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderGlobal.class)
public interface IRenderGlobal {
    @Accessor ShaderGroup getEntityOutlineShader();
}
