package cope.nebula.client.feature.module.combat;

import cope.nebula.client.events.MotionUpdateEvent;
import cope.nebula.client.events.MotionUpdateEvent.Era;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.internal.timing.TimeFormat;
import cope.nebula.util.renderer.animation.Animation;
import cope.nebula.util.renderer.animation.AnimationDirection;
import cope.nebula.util.world.entity.EntityUtil;
import cope.nebula.util.world.entity.player.inventory.InventorySpace;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import cope.nebula.util.world.entity.player.inventory.SwapType;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import cope.nebula.util.world.entity.player.rotation.Bone;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import cope.nebula.util.world.entity.player.rotation.RotationType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Aura extends Module {
    public Aura() {
        super("Aura", ModuleCategory.COMBAT, "Attacks entities in your aura");
    }

    public static final Value<Double> range = new Value<>("Range", 4.0, 1.0, 6.0);
    public static final Value<Boolean> walls = new Value<>("Walls", true);
    public static final Value<Prioritize> prioritize = new Value<>("Prioritize", Prioritize.DISTANCE);

    public static final Value<Delay> delay = new Value<>("Delay", Delay.VANILLA);
    public static final Value<Integer> aps = new Value<>(delay, "APS", 12, 1, 30);

    public static final Value<RotationType> rotate = new Value<>("Rotate", RotationType.SERVER);
    public static final Value<Boolean> keep = new Value<>(rotate, "Keep", true);
    public static final Value<Bone> bone = new Value<>(rotate, "Bone", Bone.CHEST);

    public static final Value<Swing> swing = new Value<>("Swing", Swing.POST);
    public static final Value<Weapon> weapon = new Value<>("Weapon", Weapon.REQUIRE);

    private EntityLivingBase target = null;
    private final Stopwatch stopwatch = new Stopwatch();
    private final Animation animation = new Animation(0L, 25L);

    private int oldSlot = -1;

    @Override
    protected void onDeactivated() {
        target = null;
        swapBack();
    }

    @Override
    public void onRender3d() {
        if (target != null) {
            animation.setMax(target.height);
            animation.tick(animation.getProgress() <= animation.getMax() ? AnimationDirection.FORWARD : AnimationDirection.BACK);

            glPushMatrix();
            glDisable(GL_TEXTURE_2D);

            RenderManager renderManager = mc.getRenderManager();
            glTranslated(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            glLineWidth(2.5f);

            int color = new Color(122, 49, 183).getRGB();

            float red = (color >> 16 & 0xff) / 255f;
            float green = (color >> 8 & 0xff) / 255f;
            float blue = (color & 0xff) / 255f;

            glColor3d(red, green, blue);

            double x = AngleUtil.interpolate(target.posX, target.prevPosX);
            double y = AngleUtil.interpolate(target.posY, target.prevPosY);
            double z = AngleUtil.interpolate(target.posZ, target.prevPosZ);

            glBegin(GL_LINE_LOOP);

                for (double angle = 0.0; angle < 2.0 * Math.PI; angle += 0.1) {
                    glVertex3d(x + Math.sin(angle) * 0.8, y + animation.getProgress(), z + Math.cos(angle) * 0.8);
                }

            glEnd();

            glEnable(GL_TEXTURE_2D);
            glDisable(GL_LINE_SMOOTH);
            glLineWidth(1.0f);
            glPopMatrix();
        }
    }

    @SubscribeEvent
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (!isTargetValid(target)) {
            target = null;

            List<EntityLivingBase> entities = mc.world.getEntities(EntityLivingBase.class,
                    (entity) -> !entity.isDead && EntityUtil.getHealth(entity) > 0.0f && isTargetValid(entity));

            if (entities.isEmpty()) {
                swapBack();
                target = null;
                return;
            }

            switch (prioritize.getValue()) {
                case DISTANCE:
                    entities.sort(Comparator.comparingDouble((s) -> -mc.player.getDistanceSq(s)));
                    break;

                case DAMAGE:
                    // TODO
                    break;

                case HEALTH:
                    entities.sort(Comparator.comparingDouble((s) -> -EntityUtil.getHealth(s)));
                    break;
            }

            target = entities.get(0);
        }

        if (target == null) {
            swapBack();
            return;
        }

        if (event.getEra().equals(Era.PRE)) {
            if (!weapon.getValue().equals(Weapon.NONE)) {
                if (!(InventoryUtil.getHeld(EnumHand.MAIN_HAND).getItem() instanceof ItemSword)) {
                    if (weapon.getValue().equals(Weapon.REQUIRE)) {
                        return;
                    } else if (weapon.getValue().equals(Weapon.SWAP)) {
                        int slot = InventoryUtil.getSlot(InventorySpace.HOTBAR, (stack) -> stack.getItem() instanceof ItemSword);
                        if (slot == -1) {
                            return;
                        }

                        oldSlot = mc.player.inventory.currentItem;
                        getNebula().getHotbarManager().sendSlotChange(oldSlot, SwapType.CLIENT);
                    }
                }

                attack();
            }
        }
    }

    /**
     * Attacks the current target
     */
    private void attack() {
        if (!rotate.getValue().equals(RotationType.NONE) && keep.getValue()) {
            rotate();
        }

        if (canAttack()) {
            if (!rotate.getValue().equals(RotationType.NONE) && !keep.getValue()) {
                rotate();
            }

            // in 1.8, we swing before we attack, but in 1.12 we swing after we attack
            if (swing.getValue().equals(Swing.PRE)) {
                swingItem();
            }

            mc.player.connection.sendPacket(new CPacketUseEntity(target));
            mc.player.resetCooldown();

            if (!swing.getValue().equals(Swing.PRE)) {
                swingItem();
            }
        }
    }

    /**
     * If this entity is a possible target
     * @param entity the entity
     * @return if we can attack/use this entity
     */
    private boolean isTargetValid(EntityLivingBase entity) {
        if (entity == null || entity.equals(mc.player) || entity.isDead || EntityUtil.getHealth(entity) <= 0.0f || !entity.canBeAttackedWithItem()) {
            return false;
        }

        if (!walls.getValue() && !mc.player.canEntityBeSeen(entity)) {
            return false;
        }

        return mc.player.getDistanceSq(entity) <= range.getValue() * range.getValue();
    }

    /**
     * Rotations towards the target
     */
    private void rotate() {
        Rotation rotation = AngleUtil.toEntity(target, bone.getValue()).setType(rotate.getValue());
        if (rotation.isValid()) {
            getNebula().getRotationManager().setRotation(rotation);
        }
    }

    /**
     * Swings the item in the main hand
     *
     * If swing.getValue() is false, it will send a packet so it swings only server-side
     * Or, it'll swing client side
     *
     * Either way, it will **always** swing.
     */
    private void swingItem() {
        if (!swing.getValue().equals(Swing.OFF)) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
        } else {
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        }
    }

    /**
     * If we can attack the entity
     * @return Based off of our delay mode, we determine if a delay has passed to be able to attack
     */
    private boolean canAttack() {
        if (target == null) {
            return false;
        }

        switch (delay.getValue()) {
            case VANILLA:
                return mc.player.getCooledAttackStrength(1.0f) == 1.0f && target.hurtTime == 0;

            case APS:
                return stopwatch.hasElapsed(1000L / aps.getValue(), true, TimeFormat.MILLISECONDS);
        }

        return false;
    }

    /**
     * Swaps back to the old slot
     */
    private void swapBack() {
        if (oldSlot != -1) {
            getNebula().getHotbarManager().sendSlotChange(oldSlot, SwapType.CLIENT);
            oldSlot = -1;
        }
    }

    public enum Prioritize {
        HEALTH, DISTANCE, DAMAGE
    }

    public enum Delay {
        VANILLA, APS
    }

    public enum Swing {
        PRE, POST, OFF
    }

    public enum Weapon {
        NONE, SWAP, REQUIRE
    }
}
