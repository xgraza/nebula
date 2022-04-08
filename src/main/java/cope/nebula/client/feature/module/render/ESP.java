package cope.nebula.client.feature.module.render;

import cope.nebula.asm.mixins.render.IRenderGlobal;
import cope.nebula.asm.mixins.render.shader.IShaderGroup;
import cope.nebula.client.events.RenderLivingBaseModelEvent;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.EXTFramebufferObject;

import static org.lwjgl.opengl.GL11.*;

public class ESP extends Module {
    public ESP() {
        super("ESP", ModuleCategory.RENDER, "Renders an ESP");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.OUTLINE);
    public static final Value<Boolean> self = new Value<>("Self", true);
    public static final Value<Float> lineWidth = new Value<>("LineWidth", 1.5f, 0.1f, 5.0f);

    @Override
    protected void onDeactivated() {
        mc.world.loadedEntityList.forEach((entity) -> {
            if (entity.isGlowing()) {
                entity.setGlowing(false);
            }
        });
    }

    @Override
    public void onRender3d() {
        if (mode.getValue().equals(Mode.GLOW)) {
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity == null || entity.isDead || !(entity instanceof EntityLivingBase)) {
                    continue;
                }

                entity.setGlowing(true);
            }

            ((IShaderGroup) ((IRenderGlobal) mc.renderGlobal).getEntityOutlineShader()).getListShaders().forEach((shader) -> {
                ShaderUniform uniform = shader.getShaderManager().getShaderUniform("Radius");
                if (uniform != null) {
                    uniform.set(lineWidth.getValue());
                }
            });
        }
    }

    @SubscribeEvent
    public void onRenderLivingBaseModel(RenderLivingBaseModelEvent event) {
        if (event.getEntity() == null || (!self.getValue() && event.getEntity().equals(mc.player))) {
            return;
        }

        switch (mode.getValue()) {
            case OUTLINE:
                event.setCanceled(true);
                renderOutline(event);
                break;

            case WIREFRAME:
                event.setCanceled(true);
                renderWireframe(event);
                break;
        }
    }


    private void renderOutline(RenderLivingBaseModelEvent event) {
        setupFBOs();

        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glPushMatrix();

        glClearStencil(0);
        glClear(GL_STENCIL_BUFFER_BIT);

        glEnable(GL_STENCIL_TEST);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);

        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        glLineWidth(lineWidth.getValue());

        glStencilFunc(GL_NEVER, 1, 0xff);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        event.render();

        glStencilFunc(GL_NEVER, 0, 0xff);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        event.render();

        glStencilFunc(GL_EQUAL, 1, 0xff);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        glColor3f(1.0f, 1.0f, 1.0f);

        glDepthMask(false);
        glDisable(GL_DEPTH_TEST);

        event.render();

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);

        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_LIGHTING);

        glPopAttrib();
        glPopMatrix();

        event.render();
    }

    private void renderWireframe(RenderLivingBaseModelEvent event) {
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glPushMatrix();

        glDepthMask(false);
        glDisable(GL_DEPTH_TEST);

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        glLineWidth(lineWidth.getValue());
        glColor3f(1.0f, 1.0f, 1.0f);

        event.render();

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        event.render();

        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_LIGHTING);

        glPopAttrib();
        glPopMatrix();

        event.render();
    }

    private void setupFBOs() {
        Framebuffer framebuffer = mc.getFramebuffer();
        if (framebuffer.depthBuffer > -1) {
            EXTFramebufferObject.glDeleteRenderbuffersEXT(framebuffer.depthBuffer);
            int stencilId = EXTFramebufferObject.glGenRenderbuffersEXT();

            EXTFramebufferObject.glBindRenderbufferEXT(36161, stencilId);
            EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, mc.displayWidth, mc.displayHeight);
            EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencilId);
            EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencilId);

            framebuffer.depthBuffer = -1;
        }
    }

    public enum Mode {
        /**
         * Does an outline with glStencil
         */
        OUTLINE,

        /**
         * Renders a wireframe around a player model
         */
        WIREFRAME,

        /**
         * Uses minecraft's vanilla glow shader
         */
        GLOW,
    }
}
