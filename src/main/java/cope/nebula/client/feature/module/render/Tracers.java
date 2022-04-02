package cope.nebula.client.feature.module.render;

import cope.nebula.asm.mixins.render.entity.IEntityRenderer;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.EntityUtil;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import static org.lwjgl.opengl.GL11.*;

public class Tracers extends Module {
    public Tracers() {
        super("Tracers", ModuleCategory.RENDER, "Draws lines to other entities");
    }

    public static final Value<Boolean> stem = new Value<>("Stem", true);
    public static final Value<Float> lineWidth = new Value<>("LineWidth", 1.5f, 0.1f, 3.0f);

    public static final Value<Boolean> players = new Value<>("Players", true);
    public static final Value<Boolean> friends = new Value<>(players, "Friends", true);

    public static final Value<Boolean> passive = new Value<>("Passive", false);
    public static final Value<Boolean> mobs = new Value<>("Mobs", false);

    @Override
    public void onRender3d() {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == null || entity.equals(mc.player) || !(entity instanceof EntityLivingBase)) {
                continue;
            }

            EntityLivingBase livingBase = (EntityLivingBase) entity;

            if (!players.getValue() && entity instanceof EntityPlayer) {
                // TODO: friends
                continue;
            }

            if (EntityUtil.isHostile(livingBase) && !mobs.getValue()) {
                continue;
            }

            if (EntityUtil.isPassive(livingBase) && !passive.getValue()) {
                continue;
            }

            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();

            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

            GlStateManager.glLineWidth(lineWidth.getValue());

            double dist = mc.player.getDistance(entity) / 20.0;

            float r = (float) (2.0 - dist);
            float g = (float) dist;

            glLoadIdentity();
            ((IEntityRenderer) mc.entityRenderer).doOrientCamera(mc.getRenderPartialTicks());

            RenderManager renderManager = mc.getRenderManager();
            Vec3d eyes = mc.player.getLook(mc.getRenderPartialTicks());

            double x = AngleUtil.interpolate(entity.posX, entity.prevPosX);
            double y = AngleUtil.interpolate(entity.posY, entity.prevPosY);
            double z = AngleUtil.interpolate(entity.posZ, entity.prevPosZ);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            // base tracers, from middle of screen to entity feet
            buffer.pos(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z).color(r, g, 0.0f, 1.0f).endVertex();
            buffer.pos(x - renderManager.viewerPosX, y - renderManager.viewerPosY, z - renderManager.viewerPosZ).color(r, g, 0.0f, 1.0f).endVertex();

            if (stem.getValue()) {
                buffer.pos(x - renderManager.viewerPosX, y - renderManager.viewerPosY, z - renderManager.viewerPosZ).color(r, g, 0.0f, 1.0f).endVertex();
                buffer.pos(x - renderManager.viewerPosX, (y + entity.height) - renderManager.viewerPosY, z - renderManager.viewerPosZ).color(r, g, 0.0f, 1.0f).endVertex();
            }

            tessellator.draw();

            GlStateManager.enableDepth();
            GlStateManager.disableTexture2D();
            glDisable(GL_LINE_SMOOTH);
            GlStateManager.popMatrix();
        }
    }
}
