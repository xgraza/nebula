package cope.nebula.client.feature.module.combat;

import cope.nebula.client.events.MotionUpdateEvent;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * Credit: https://github.com/Sxmurai/not-konas-src/blob/main/src/main/java/me/darki/konas/module/modules/combat/BowAim.java
 *
 * I did **not** write any of the rotation methods in this module, this is skidded from above. I'm not that good at math, and this BowAim is nice.
 *
 * Konas devs, if you want your code removed please open an issue kthx
 */
public class BowAim extends Module {
    public BowAim() {
        super("BowAim", ModuleCategory.COMBAT, "Automatically aims your bow");
    }

    public static final Value<Double> maxDistance = new Value<>("MaxDistance", 50.0, 10.0, 75.0);
    public static final Value<Boolean> render = new Value<>("Render", true);
    public static final Value<Boolean> server = new Value<>("Server", false);

    private EntityLivingBase target = null;

    @Override
    public String getDisplayInfo() {
        if (target != null) {
            return target.getName();
        }

        return super.getDisplayInfo();
    }

    @Override
    public void onRender3d() {
        if (target != null && render.getValue()) {
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();

            RenderManager renderManager = mc.getRenderManager();
            glTranslated(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            GlStateManager.glLineWidth(2.5f);

            int color = new Color(134, 49, 183).getRGB();

            float red = (color >> 16 & 0xff) / 255f;
            float green = (color >> 8 & 0xff) / 255f;
            float blue = (color & 0xff) / 255f;

            double x = AngleUtil.interpolate(target.posX, target.prevPosX);
            double y = AngleUtil.interpolate(target.posY, target.prevPosY);
            double z = AngleUtil.interpolate(target.posZ, target.prevPosZ);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

            for (double angle = 0.0; angle < 2.0 * Math.PI; angle += 0.1) {
                buffer.pos(x + Math.sin(angle) * 0.8, y, z + Math.cos(angle) * 0.8).color(red, green, blue, 1.0f).endVertex();
            }

            tessellator.draw();

            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            glDisable(GL_LINE_SMOOTH);
            GlStateManager.glLineWidth(1.0f);
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void onMotionUpdate(MotionUpdateEvent event) {
        List<EntityLivingBase> entities = mc.world.getEntities(EntityLivingBase.class,
                (e) -> e != null &&
                        mc.player.canEntityBeSeen(e) &&
                        !e.isDead &&
                        !e.equals(mc.player) &&
                        mc.player.getDistanceSq(e) < maxDistance.getValue() * maxDistance.getValue());

        if (entities.isEmpty()) {
            return;
        }

        entities.sort(Comparator.comparingDouble((e) -> mc.player.getDistanceSq(e)));

        target = entities.get(0);

        if (mc.player.isHandActive() && mc.player.getActiveItemStack().getItem().equals(Items.BOW) && target != null) {
            Rotation rotation = getBowAimRotations();
            if (rotation.isValid()) {
                if (server.getValue()) {
                    getNebula().getRotationManager().setRotation(rotation);
                } else {
                    mc.player.rotationYaw = rotation.getYaw();
                    mc.player.rotationPitch = rotation.getPitch();
                }
            }
        }
    }

    /**
     * Gets the rotations for aiming a bow at an entity
     * @return the rotations
     */
    public Rotation getBowAimRotations() {
        float currentDuration = (float) (mc.player.getActiveItemStack().getMaxItemUseDuration() - mc.player.getItemInUseCount()) / 20.0f;

        currentDuration = (currentDuration * currentDuration + currentDuration * 2.0f) / 3.0f;
        if (currentDuration >= 1.0f) {
            currentDuration = 1.0f;
        }

        double duration = currentDuration * 3.0f;
        double coeff = 0.05000000074505806;

        float pitch = (float) -Math.toDegrees(calculateArc(target, duration, coeff));

        if (Float.isNaN(pitch)) {
            return Rotation.INVALID_ROTATION;
        }

        double iX = target.posX - target.lastTickPosX;
        double iZ = target.posZ - target.lastTickPosZ;

        double d = mc.player.getDistance(target);

        d -= d % 2.0;

        iX = d / 2.0 * iX * (mc.player.isSprinting() ? 1.3 : 1.1);
        iZ = d / 2.0 * iZ * (mc.player.isSprinting() ? 1.3 : 1.1);

        float yaw = (float) Math.toDegrees(Math.atan2(target.posZ + iZ - mc.player.posZ, target.posX + iX - mc.player.posX)) - 90.0f;

        return new Rotation(yaw, pitch);
    }

    private float calculateArc(EntityLivingBase target, double duration, double coeff) {
        double yArc = target.posY + (double) (target.getEyeHeight() / 2.0f) - (mc.player.posY + (double) mc.player.getEyeHeight());
        double dX = target.posX - mc.player.posX;
        double dZ = target.posZ - mc.player.posZ;

        double dirRoot = Math.sqrt(dX * dX + dZ * dZ);

        return calculateArc(duration, coeff, dirRoot, yArc);
    }

    private float calculateArc(double duration, double coeff, double dirRoot, double yArc) {
        double dirCoeff = coeff * (dirRoot * dirRoot);

        yArc = 2.0 * yArc * (duration * duration);
        yArc = coeff * (dirCoeff + yArc);
        yArc = Math.sqrt(duration * duration * duration * duration - yArc);

        duration = duration * duration - yArc;

        yArc = Math.atan2(duration * duration + yArc, coeff * dirRoot);

        duration = Math.atan2(duration, coeff * dirRoot);

        return (float)Math.min(yArc, duration);
    }
}
