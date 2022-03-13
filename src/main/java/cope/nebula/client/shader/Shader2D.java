package cope.nebula.client.shader;


import cope.nebula.asm.mixins.render.shader.IShaderGroup;
import cope.nebula.client.Nebula;
import cope.nebula.util.Globals;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.glScalef;

/**
 * Represents a 2D shader
 *
 * @author aesthetical
 * @since 3/12/22
 */
public abstract class Shader2D implements Globals {
    private final String name;

    private ShaderGroup shaderGroup;
    private Framebuffer buffer;

    public Shader2D(String name, ResourceLocation location) {
        this.name = name;
        init(location);
    }

    /**
     * Create shader group and frame buffer from JSON location
     * @param location The location of the JSON file in resources
     */
    private void init(ResourceLocation location) {
        try {
            if (buffer != null) {
                buffer.deleteFramebuffer();
            }

            shaderGroup = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), location);
            shaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
            buffer = ((IShaderGroup) shaderGroup).getListFramebuffers().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Renders the shader
     * @param renderPartialTicks The render partial ticks
     */
    public void render(float renderPartialTicks) {
        config();

        if (OpenGlHelper.isFramebufferEnabled()) {
            int scaleFactor = new ScaledResolution(mc).getScaleFactor();

            mc.profiler.startSection("2dshader_" + name);

            buffer.framebufferClear();

            buffer.bindFramebuffer(true);
            shaderGroup.render(renderPartialTicks);

            mc.getFramebuffer().bindFramebuffer(true);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);

            buffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);

            GlStateManager.disableBlend();

            glScalef(scaleFactor, scaleFactor, 0.0f);

            mc.profiler.endSection();
        } else {
            Nebula.getLogger().debug("Frame buffer is not supported!");
        }
    }

    /**
     * For setting up shader uniforms
     */
    public abstract void config();

    /**
     * Retrieve a shader uniform
     * @param index The index in the listShaders List to get the uniform from
     * @param name The uniform name
     * @return null if no ShaderUniform was found, or the ShaderUniform object
     */
    public ShaderUniform getShaderUniform(int index, String name) {
        try {
            return ((IShaderGroup) shaderGroup).getListShaders().get(index).getShaderManager().getShaderUniform(name);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}