package cope.nebula.client.feature.module.combat;

import cope.nebula.client.events.EntityRemoveEvent;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.feature.command.Command;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.internal.timing.TimeFormat;
import cope.nebula.util.renderer.RenderUtil;
import cope.nebula.util.world.BlockUtil;
import cope.nebula.util.world.RaycastUtil;
import cope.nebula.util.world.damage.ExplosionUtil;
import cope.nebula.util.world.entity.CrystalUtil;
import cope.nebula.util.world.entity.EntityUtil;
import cope.nebula.util.world.entity.player.inventory.InventorySpace;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import cope.nebula.util.world.entity.player.inventory.SwapType;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import cope.nebula.util.world.entity.player.rotation.Bone;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

// here we go
public class AutoCrystal extends Module {
    public AutoCrystal() {
        super("AutoCrystal", ModuleCategory.COMBAT, "Automatically places and explodes crystals");
    }

    public static final Value<Timing> timing = new Value<>("Timing", Timing.SEQUENTIAL);

    public static final Value<Rotate> rotate = new Value<>("Rotate", Rotate.NORMAL);
    public static final Value<Double> limitThreshold = new Value<>(rotate, "LimitThreshold", 55.0, 10.0, 100.0);

    public static final Value<Double> targetRange = new Value<>("TargetRange", 10.0, 6.0, 20.0);

    public static final Value<Boolean> place = new Value<>("Place", true);
    public static final Value<Double> placeSpeed = new Value<>(place, "PlaceSpeed", 20.0, 1.0, 20.0);
    public static final Value<Double> placeRange = new Value<>(place, "PlaceRange", 5.0, 1.0, 6.0);
    public static final Value<Double> placeWallRange = new Value<>(place, "PlaceWallRange", 4.0, 1.0, 6.0);
    public static final Value<Boolean> armorBreaker = new Value<>(place, "ArmorBreaker", true);
    public static final Value<Integer> armorPercent = new Value<>(armorBreaker, "ArmorPercent", 20, 1, 100);
    public static final Value<Boolean> raytrace = new Value<>(place, "Raytrace", false);
    public static final Value<Boolean> strictDirection = new Value<>(place, "StrictDirection", false);
    public static final Value<Boolean> ignoreTerrain = new Value<>(place, "IgnoreTerrain", true);
    public static final Value<Boolean> predict = new Value<>(place, "Predict", true);
    public static final Value<Type> type = new Value<>(place, "Type", Type.NATIVE);

    public static final Value<Boolean> explode = new Value<>("Explode", true);
    public static final Value<Double> explodeSpeed = new Value<>(explode, "ExplodeSpeed", 20.0, 1.0, 20.0);
    public static final Value<Double> explodeRange = new Value<>(explode, "ExplodeRange", 5.0, 1.0, 6.0);
    public static final Value<Double> explodeWallRange = new Value<>(explode, "ExplodeWallRange", 4.0, 1.0, 6.0);
    public static final Value<Boolean> inhibit = new Value<>(explode, "Inhibit", true);
    public static final Value<Integer> existed = new Value<>(explode, "Existed", 2, 0, 10);

    public static final Value<Swap> swap = new Value<>("Swap", Swap.CLIENT);
    public static final Value<Integer> swapDelay = new Value<>(swap, "SwapDelay", 2, 0, 10);

    public static final Value<Boolean> swing = new Value<>("Swing", true);

    public static final Value<Double> maxLocal = new Value<>("MaxLocal", 12.0, 1.0, 20.0);
    public static final Value<Double> minDamage = new Value<>("MinDamage", 5.0, 0.0, 20.0);

    private final Stopwatch placeStopwatch = new Stopwatch();
    private final Stopwatch explodeStopwatch = new Stopwatch();
    private final Stopwatch swapStopwatch = new Stopwatch();

    private EntityPlayer target;
    private BlockPos lastPlacePos = null;
    private EntityEnderCrystal attackCrystal = null;
    private float currentDamage = 0.5f;

    private int oldSlot = -1;
    private EnumHand hand = EnumHand.MAIN_HAND;

    private final Set<EntityEnderCrystal> attackedCrystals = new HashSet<>();
    private Rotation lastRotation = Rotation.INVALID_ROTATION;

    private final Stopwatch crystals = new Stopwatch();
    private int crystalsPerSec = 0;
    private int lastCrystalsPerSec = 0;

    @Override
    public String getDisplayInfo() {
        if (crystals.passedSeconds(1L)) {
            crystals.resetTime();

            lastCrystalsPerSec = crystalsPerSec;
            crystalsPerSec = 0;
        }

        return String.valueOf(lastCrystalsPerSec == 0 ? crystalsPerSec : lastCrystalsPerSec);
    }

    @Override
    protected void onActivated() {
        crystals.resetTime();
    }

    @Override
    protected void onDeactivated() {
        target = null;
        lastPlacePos = null;
        attackCrystal = null;
        currentDamage = 0.5f;

        if (oldSlot != -1) {
            swapBack();
        }

        lastRotation = Rotation.INVALID_ROTATION;

        lastCrystalsPerSec = 0;
        crystalsPerSec = 0;

        attackedCrystals.clear();
    }

    @Override
    public void onRender3d() {
        if (lastPlacePos != null) {
            AxisAlignedBB box = new AxisAlignedBB(lastPlacePos).offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
            int color = new Color(122, 49, 183, 120).getRGB();

            RenderUtil.renderFilledBox(box, color);
            RenderUtil.renderOutlinedBox(box, 3.5f, color);
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        switch (event.getDirection()) {
            case INCOMING: {
                if (event.getPacket() instanceof SPacketDestroyEntities) {
                    SPacketDestroyEntities packet = event.getPacket();

                    for (int entityId : packet.getEntityIDs()) {
                        Entity entity = mc.world.getEntityByID(entityId);
                        if (entity == null || entity.isDead || !(entity instanceof EntityEnderCrystal)) {
                            continue;
                        }

                        EntityEnderCrystal crystal = (EntityEnderCrystal) entity;

                        crystal.setDead();
                        mc.world.removeEntity(crystal);

                        if (attackCrystal != null && attackCrystal.getEntityId() == entityId) {
                            attackCrystal = null;
                        }

                        attackedCrystals.add(crystal);
                    }
                } else if (event.getPacket() instanceof SPacketSoundEffect) {
                    SPacketSoundEffect packet = event.getPacket();
                    if (packet.getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
                        for (Entity entity : mc.world.loadedEntityList) {
                            if (entity == null || entity.isDead || !(entity instanceof EntityEnderCrystal)) {
                                continue;
                            }

                            if (entity.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()) < 12.0) {
                                EntityEnderCrystal crystal = (EntityEnderCrystal) entity;

                                crystal.setDead();
                                mc.world.removeEntity(crystal);

                                if (attackCrystal != null && attackCrystal.equals(crystal)) {
                                    attackCrystal = null;
                                    ++crystalsPerSec;
                                }

                                attackedCrystals.add(crystal);
                            }
                        }
                    }
                } else if (event.getPacket() instanceof SPacketExplosion) {
                    SPacketExplosion packet = event.getPacket();

                    for (Entity entity : mc.world.loadedEntityList) {
                        if (entity == null || entity.isDead || !(entity instanceof EntityEnderCrystal)) {
                            continue;
                        }

                        if (entity.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()) < packet.getStrength() * packet.getStrength()) {
                            EntityEnderCrystal crystal = (EntityEnderCrystal) entity;

                            crystal.setDead();
                            mc.world.removeEntity(crystal);

                            if (attackCrystal != null && attackCrystal.equals(crystal)) {
                                attackCrystal = null;
                            }

                            attackedCrystals.add(crystal);
                        }
                    }
                }

                break;
            }

            case OUTGOING: {
                if (event.getPacket() instanceof CPacketUseEntity) {
                    CPacketUseEntity packet = event.getPacket();
                    if (packet.getEntityFromWorld(mc.world) instanceof EntityEnderCrystal && packet.getAction().equals(Action.ATTACK)) {
                        EntityEnderCrystal crystal = (EntityEnderCrystal) packet.getEntityFromWorld(mc.world);
                        if (attackedCrystals.contains(crystal)) {
                            event.setCanceled(true);
                            return;
                        }

                        attackedCrystals.add((EntityEnderCrystal) packet.getEntityFromWorld(mc.world));
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onTick() {
        // find out hand shit
        int slot = InventoryUtil.getSlot(InventorySpace.HOTBAR,
                (stack) -> !stack.isEmpty() && stack.getItem().equals(Items.END_CRYSTAL));
        if (slot == -1) {
            return;
        }

        hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

        calculateBestPlace();
        if (place.getValue()) {
            if (lastPlacePos != null) {
                if (!swap.getValue().equals(Swap.NONE) &&
                        !InventoryUtil.isHolding(Items.END_CRYSTAL) &&
                        slot != InventoryUtil.OFFHAND_SLOT) {

                    oldSlot = mc.player.inventory.currentItem;
                    getNebula().getHotbarManager().sendSlotChange(slot, swap.getValue().swapType);
                }

                if (placeStopwatch.getTime(TimeFormat.MILLISECONDS) / 50.0f >= 20.0f - placeSpeed.getValue()) {
                    if (!CrystalUtil.canPlaceAt(lastPlacePos, type.getValue().equals(Type.UPDATED))) {
                        lastPlacePos = null;
                        return;
                    }

                    placeStopwatch.resetTime();
                    CrystalUtil.placeAt(lastPlacePos, hand, strictDirection.getValue(), swing.getValue(), rotate.getValue().equals(Rotate.NORMAL));

                    if (swap.getValue().equals(Swap.SERVER)) {
                        swapBack();
                    }
                }
            }
        }

        calculateBestAttackCrystal();
        if (explode.getValue()) {
            if (attackCrystal != null && explodeStopwatch.getTime(TimeFormat.MILLISECONDS) / 50.0f >= 20.0f - explodeSpeed.getValue()) {
                explodeStopwatch.resetTime();

                if (!rotate.getValue().equals(Rotate.NONE)) {
                    Rotation rotation = AngleUtil.toEntity(attackCrystal, Bone.CHEST);
                    if (rotation.isValid()) {
                        getNebula().getRotationManager().setRotation(rotation);
                    }
                }

                CrystalUtil.attack(attackCrystal, hand, swing.getValue());
            }
        }
    }

    /**
     * Calculates the best place position
     */
    private void calculateBestPlace() {
        BlockPos pos = null;
        float damage = 1.0f;

        for (BlockPos loc : BlockUtil.sphere(mc.player.getPosition(), placeRange.getValue().intValue())) {
            if (!CrystalUtil.canPlaceAt(loc, type.getValue().equals(Type.UPDATED))) {
                continue;
            }

            if (!RaycastUtil.isBlockVisible(loc) && !BlockUtil.isInRange(pos, placeWallRange.getValue())) {
                continue;
            }

            Vec3d vec = new Vec3d(loc.getX() + 0.5, loc.getY() + 1.0, loc.getZ() + 0.5);

            float localDamage = ExplosionUtil.calculateCrystalDamage(mc.player, vec, ignoreTerrain.getValue()) + 0.5f;
            if (localDamage >= EntityUtil.getHealth(mc.player) || localDamage > maxLocal.getValue()) {
                continue;
            }

            float entityDamage = 0.0f;
            if (target != null) {
                entityDamage = ExplosionUtil.calculateCrystalDamage(target, vec, ignoreTerrain.getValue());
                if (localDamage > entityDamage || entityDamage < minDamage.getValue()) {
                    continue;
                }
            }

            for (EntityPlayer player : mc.world.playerEntities) {
                if (player == null || player.isDead || player.equals(mc.player)) {
                    continue;
                }

                if (mc.player.getDistanceSq(player) > targetRange.getValue() * targetRange.getValue()) {
                    continue;
                }

                float playerDamage = ExplosionUtil.calculateCrystalDamage(player, vec, ignoreTerrain.getValue());
                if (playerDamage > entityDamage || playerDamage > minDamage.getValue()) {
                    target = player;
                    entityDamage = playerDamage;
                }
            }

            if (entityDamage > damage) {
                damage = entityDamage;
                pos = loc;
            }
        }

        lastPlacePos = pos;
        currentDamage = damage;
    }

    /**
     * Calculates the best crystal to attack
     */
    private void calculateBestAttackCrystal() {
        if (target == null) {
            return;
        }

        EntityEnderCrystal crystal = null;
        float damage = 0.5f;

        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == null || entity.isDead || !(entity instanceof EntityEnderCrystal)) {
                continue;
            }

            EntityEnderCrystal c = (EntityEnderCrystal) entity;
            if (attackedCrystals.contains(c) && inhibit.getValue()) {
                continue;
            }

            if (entity.ticksExisted < existed.getValue()) {
                continue;
            }

            // ignore out of range entities
            double distance = mc.player.getDistanceSq(entity);
            if (distance > (mc.player.canEntityBeSeen(entity) ? explodeRange.getValue() * explodeRange.getValue() : explodeWallRange.getValue() * explodeWallRange.getValue())) {
                continue;
            }

            crystal = c;

//            if (crystal == null) {
//                crystal = c;
//            } else {
//                float targetDamage = ExplosionUtil.calculateCrystalDamage(target, entity.getPositionVector().add(0.5, 1.0, 0.5), ignoreTerrain.getValue());
//                if (targetDamage < minDamage.getValue()) {
//                    continue;
//                }
//
//                if (targetDamage > damage) {
//                    crystal = c;
//                    damage = targetDamage;
//                }
//            }
        }

        if (crystal != null) {
            attackCrystal = crystal;
        }
    }

    private void swapBack() {
        if (oldSlot != -1) {
            getNebula().getHotbarManager().sendSlotChange(oldSlot, swap.getValue().swapType);
            oldSlot = -1;
        }

        hand = EnumHand.MAIN_HAND;
    }

    public enum Timing {
        VANILLA, SEQUENTIAL
    }

    public enum Type {
        NATIVE, UPDATED
    }

    public enum Rotate {
        NONE, NORMAL, LIMITED
    }

    public enum Swap {
        NONE(null),
        CLIENT(SwapType.CLIENT),
        SERVER(SwapType.SERVER);

        private final SwapType swapType;

        Swap(SwapType swapType) {
            this.swapType = swapType;
        }
    }
}
