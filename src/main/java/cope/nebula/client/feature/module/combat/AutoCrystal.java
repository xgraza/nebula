package cope.nebula.client.feature.module.combat;

import cope.nebula.asm.duck.IEntityPlayer;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
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
import cope.nebula.util.world.entity.player.inventory.InventorySpace;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import cope.nebula.util.world.entity.player.inventory.SwapType;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import cope.nebula.util.world.entity.player.rotation.Bone;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import cope.nebula.util.world.entity.player.rotation.RotationType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.*;

public class AutoCrystal extends Module {
    public AutoCrystal() {
        super("AutoCrystal", ModuleCategory.COMBAT, "Automatically places and explodes crystals");
    }

    // placements
    public static final Value<Boolean> place = new Value<>("Place", true);
    public static final Value<Float> placeSpeed = new Value<>(place, "PlaceSpeed", 19.0f, 1.0f, 20.0f);
    public static final Value<Double> placeRange = new Value<>(place, "PlaceRange", 4.5, 1.0, 6.0);
    public static final Value<Double> placeWallRange = new Value<>(place, "PlaceWallRange", 3.0, 1.0, 6.0);
    public static final Value<Interact> interact = new Value<>(place, "Interact", Interact.NORMAL);
    public static final Value<Protocol> protocol = new Value<>(place, "Protocol", Protocol.NATIVE);
    public static final Value<Boolean> ignoreTerrain = new Value<>(place, "IgnoreTerrain", true);
    public static final Value<Boolean> facePlace = new Value<>(place, "Faceplace", false);
    public static final Value<Integer> armorPercent = new Value<>(facePlace, "ArmorPercent", 20, 1, 50);
    public static final Value<Float> faceHealth = new Value<>(facePlace, "FaceHealth", 10.0f, 1.0f, 20.0f);

    // exploding
    public static final Value<Boolean> explode = new Value<>("Explode", true);
    public static final Value<Float> explodeSpeed = new Value<>(explode, "ExplodeSpeed", 20.0f, 1.0f, 20.0f);
    public static final Value<Double> explodeRange = new Value<>(explode, "ExplodeRange", 4.5, 1.0, 6.0);
    public static final Value<Double> explodeWallRange = new Value<>(explode, "ExplodeWallRange", 3.0, 1.0, 6.0);
    public static final Value<Boolean> inhibit = new Value<>(explode, "Inhibit", true);
    public static final Value<Integer> ticksExisted = new Value<>(explode, "TicksExisted", 1, 0, 10);

    // swapping
    public static final Value<SwapType> swapping = new Value<>("Swapping", SwapType.CLIENT);
    public static final Value<Integer> swapDelay = new Value<>(swapping, "SwapDelay", 2, 0, 10);

    // rotations
    public static final Value<Rotate> rotate = new Value<>("Rotate", Rotate.SERVER);
    public static final Value<YawLimit> yawLimit = new Value<>(rotate, "YawLimit", YawLimit.SEMI);
    public static final Value<Float> maxYawRate = new Value<>(yawLimit, "MaxYawRate", 55.0f, 10.0f, 100.0f);

    // damages
    public static final Value<Float> minDamage = new Value<>("MinDamage", 4.0f, 0.0f, 10.0f);
    public static final Value<Float> maxLocal = new Value<>("MaxLocal", 12.0f, 1.0f, 20.0f);
    public static final Value<Boolean> safety = new Value<>("Safety", true);


    // targets
    private EntityPlayer target = null;
    private BlockPos placePos = null;
    private EntityEnderCrystal attackCrystal = null;

    // rotations
    private Rotation nextRotation = Rotation.INVALID_ROTATION;

    // swapping
    private int oldSlot = -1;
    private EnumHand hand = EnumHand.MAIN_HAND;

    // crystals per second calculations
    private int crystalCount = 0;
    private int lastCrystalCount = 0;

    // timing
    private final Stopwatch placeTimer = new Stopwatch();
    private final Stopwatch explodeTimer = new Stopwatch();
    private final Stopwatch swapTimer = new Stopwatch();
    private final Stopwatch crystalCountTimer = new Stopwatch();

    private final Set<EntityEnderCrystal> placedCrystals = new HashSet<>();
    private final Map<EntityEnderCrystal, Stopwatch> inhibitCrystals = new HashMap<>();

    @Override
    public String getDisplayInfo() {
        if (crystalCountTimer.hasElapsed(1L, true, TimeFormat.SECONDS)) {
            lastCrystalCount = crystalCount;
            crystalCount = 0;
        }

        return String.valueOf(lastCrystalCount == 0 ? crystalCount : lastCrystalCount);
    }

    @Override
    protected void onActivated() {
        crystalCountTimer.resetTime();
    }

    @Override
    protected void onDeactivated() {
        target = null;
        placePos = null;
        attackCrystal = null;

        nextRotation = Rotation.INVALID_ROTATION;

        if (oldSlot != -1) {
            swapBack();
        }

        hand = EnumHand.MAIN_HAND;

        crystalCount = 0;
        lastCrystalCount = 0;

        placedCrystals.clear();
        inhibitCrystals.clear();
    }

    @Override
    public void onRender3d() {
        if (placePos != null) {
            AxisAlignedBB box = new AxisAlignedBB(placePos)
                    .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
            int color = new Color(122, 49, 183, 120).getRGB();

            RenderUtil.renderFilledBox(box, color);
            RenderUtil.renderOutlinedBox(box, 3.5f, color);
        }
    }

    @Override
    public void onTick() {
        // find place position and best end crystal to attack
        findBestPlacePosition();
        findBestEndCrystal();

        if (explode.getValue()) {
            if (attackCrystal == null || attackCrystal.isDead) {
                attackCrystal = null;
            }

            if (attackCrystal != null && explodeTimer.getTime(TimeFormat.MILLISECONDS) / 50.0f >= 20.0f - explodeSpeed.getValue()) {
                explodeTimer.resetTime();

                if (!rotate.getValue().equals(Rotate.NONE)) {
                    boolean result = sendRotations(AngleUtil.getEyes(attackCrystal, Bone.HEAD));
                    if (result) {
                        // TODO
                    }

                    Rotation rotation = AngleUtil.toEntity(attackCrystal, Bone.HEAD);
                    if (rotation.isValid()) {
                        getNebula().getRotationManager().setRotation(rotation);
                    }
                }

                CrystalUtil.attack(attackCrystal, hand, true);
                inhibitCrystals.put(attackCrystal, new Stopwatch().resetTime());
            }
        }

        if (place.getValue()) {
            // find crystals in our hotbar
            if (!findCrystals()) {
                return;
            }

            // if there is no place position
            if (placePos == null) {
                return;
            }

            if (placeTimer.getTime(TimeFormat.MILLISECONDS) / 50.0f >= 20.0f - placeSpeed.getValue()) {
                // make sure we can place a crystal here
                if (!CrystalUtil.canPlaceAt(placePos, true, protocol.getValue().equals(Protocol.UPDATED))) {
                    return;
                }

                placeTimer.resetTime();

                if (!rotate.getValue().equals(Rotate.NONE)) {
                    boolean result = sendRotations(new Vec3d(placePos));
                    if (result) {
                        // TODO
                    }
                }

                CrystalUtil.placeAt(placePos, hand, interact.getValue().equals(Interact.STRICT), true, rotate.getValue().rotationType);

                if (swapping.getValue().equals(SwapType.SERVER)) {
                    swapBack();
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING)) {
            if (event.getPacket() instanceof SPacketSpawnObject) {
                SPacketSpawnObject packet = event.getPacket();
                if (packet.getType() == 51) {
                    Entity entity = mc.world.getEntityByID(packet.getEntityID());
                    if (entity instanceof EntityEnderCrystal) {
                        if (placePos == null) {
                            return;
                        }

                        if (placePos.equals(entity.getPosition().down())) {
                            placedCrystals.add((EntityEnderCrystal) entity);
                        }
                    }
                }
            } else if (event.getPacket() instanceof SPacketDestroyEntities) {
                SPacketDestroyEntities packet = event.getPacket();

                for (int entityId : packet.getEntityIDs()) {
                    Entity entity = mc.world.getEntityByID(entityId);
                    if (!(entity instanceof EntityEnderCrystal) || entity.isDead) {
                        continue;
                    }

                    if (attackCrystal != null && attackCrystal.getEntityId() == entityId) {
                        attackCrystal = null;
                    }

                    entity.setDead();
                    mc.world.removeEntity(entity);
                }
            } else if (event.getPacket() instanceof SPacketExplosion) {
                SPacketExplosion packet = event.getPacket();
                double power = packet.getStrength() * 2.0;

                for (Entity entity : mc.world.loadedEntityList) {
                    if (!(entity instanceof EntityEnderCrystal) || entity.isDead) {
                        continue;
                    }

                    if (entity.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()) < power) {
                        if (attackCrystal != null && attackCrystal.getEntityId() == entity.getEntityId()) {
                            attackCrystal = null;
                        }

                        entity.setDead();
                        mc.world.removeEntity(entity);
                    }
                }
            } else if (event.getPacket() instanceof SPacketSoundEffect) {
                SPacketSoundEffect packet = event.getPacket();

                if (packet.getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
                    for (Entity entity : mc.world.loadedEntityList) {
                        if (!(entity instanceof EntityEnderCrystal) || entity.isDead) {
                            continue;
                        }

                        if (entity.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()) < 12.0) {
                            if (attackCrystal != null && attackCrystal.getEntityId() == entity.getEntityId()) {
                                attackCrystal = null;
                            }

                            entity.setDead();
                            mc.world.removeEntity(entity);
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds the best place positions
     */
    private void findBestPlacePosition() {
        BlockPos position = null;
        float damage = 1.0f;

        for (BlockPos pos : BlockUtil.sphere(mc.player.getPosition(), placeRange.getValue().intValue())) {
            // if we cannot see the position we want to place in and it exceeds our wall range bounds, we cant use this position
            if (!RaycastUtil.isBlockVisible(pos) && !BlockUtil.isInRange(pos, placeWallRange.getValue())) {
                continue;
            }

            // if we cannot place a crystal here
            if (!CrystalUtil.canPlaceAt(pos, false, protocol.getValue().equals(Protocol.UPDATED))) {
                continue;
            }

            Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

            float localDamage = ExplosionUtil.calculateCrystalDamage(mc.player, vec, ignoreTerrain.getValue()) + 0.5f;
            if (localDamage > maxLocal.getValue()) {
                continue;
            }

            // if we want to be safe (safety is on)
            if (safety.getValue() && localDamage >= (mc.player.getHealth() - 0.5f)) {
                continue;
            }

            float targetDamage = damage;
            if (target != null) {
                targetDamage = ExplosionUtil.calculateCrystalDamage(target, vec, ignoreTerrain.getValue());
                if (targetDamage < minDamage.getValue() || (safety.getValue() && localDamage > targetDamage)) {
                    continue;
                }
            }

            Iterator<EntityPlayer> players = mc.world.playerEntities.iterator();
            while (players.hasNext()) {
                EntityPlayer player = players.next();

                float playerDamage = ExplosionUtil.calculateCrystalDamage(player, vec, ignoreTerrain.getValue());

                // protect friends
                if (((IEntityPlayer) player).isFriend() && playerDamage > minDamage.getValue()) {
                    continue;
                }

                if (playerDamage > targetDamage) {
                    targetDamage = playerDamage;
                    target = player;
                }
            }

            if (targetDamage > damage) {
                position = pos;
                damage = targetDamage;
            }
        }

        placePos = position;
    }

    /**
     * Finds the best end crystal to attack
     *
     * I should make this sort by damage done to player, but in the end it doesn't really matter
     */
    private void findBestEndCrystal() {
        double dist = 0.0;
        EntityEnderCrystal crystal = null;

        Iterator<Entity> iterator = mc.world.loadedEntityList.iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (!(entity instanceof EntityEnderCrystal) || entity.isDead || entity.ticksExisted < ticksExisted.getValue()) {
                continue;
            }

            double distance = mc.player.getDistanceSq(entity);

            double range = explodeRange.getValue() * explodeRange.getValue();
            if (!mc.player.canEntityBeSeen(entity)) {
                range = explodeWallRange.getValue() * explodeWallRange.getValue();
            }

            if (distance > range) {
                continue;
            }

            if (crystal == null || distance < dist) {
                dist = distance;
                crystal = (EntityEnderCrystal) entity;
            }
        }

        if (inhibit.getValue() && inhibitCrystals.containsKey(crystal)) {
            Stopwatch stopwatch = inhibitCrystals.get(crystal);

            int ping = 50 + getNebula().getServerManager().getLatency(mc.player.getUniqueID());
            if (!stopwatch.passedMs(ping)) {
                crystal = null;
            } else {
                inhibitCrystals.remove(crystal);
            }
        }

        attackCrystal = crystal;
    }

    /**
     * Finds end crystals to of course, place
     * @return if we could find crystals or not
     */
    private boolean findCrystals() {
        // find the slot the crystals are in
        int slot = InventoryUtil.getSlot(InventorySpace.HOTBAR,
                (stack) -> !stack.isEmpty() && stack.getItem().equals(Items.END_CRYSTAL));

        // no crystals in the hotbar/offhand
        if (slot == -1) {
            return false;
        }

        if (slot == InventoryUtil.OFFHAND_SLOT) {
            hand = EnumHand.OFF_HAND;
        } else {
            hand = EnumHand.MAIN_HAND;

            oldSlot = mc.player.inventory.currentItem;
            getNebula().getHotbarManager().sendSlotChange(slot, swapping.getValue());

            swapTimer.resetTime();

//            if (oldSlot != -1 && !swapTimer.passedTicks(swapDelay.getValue())) {
//                return false;
//            } else {
//                oldSlot = mc.player.inventory.currentItem;
//                getNebula().getHotbarManager().sendSlotChange(slot, swapping.getValue());
//
//                swapTimer.resetTime();
//            }
        }

        return true;
    }

    /**
     * Spoofs rotations
     * @param vec the vec to rotate to
     * @return if we should limit and wait
     */
    private boolean sendRotations(Vec3d vec) {
        return true;
    }

    /**
     * Swaps back to your old slot
     */
    private void swapBack() {
        if (oldSlot != -1) {
            getNebula().getHotbarManager().sendSlotChange(oldSlot, swapping.getValue());
            oldSlot = -1;
        }
    }

    public enum Interact {
        /**
         * Normal interaction. Does the following:
         *
         * - Facing is EnumFacing.UP
         * - FacingX, FacingY, FacingZ are all 0.0f
         */
        NORMAL,

        /**
         * Strict interaction, more vanilla-like interactions
         *
         * - Facing is the closest facing we can see
         *  - For example, if one of the sides is exposed to us, we'll place on that side in the packet
         *  - However, if we are under the block, DOWN is what we'll sue
         *  - But UP will be the default otherwise
         * - FacingX, FacingY, FacingZ are all vanilla like, and are never all 0.0f
         */
        STRICT
    }

    public enum Protocol {
        /**
         * Native 1.12.2 crystal placements, no 1x1 holes
         */
        NATIVE,

        /**
         * Updated 1.13+ server crystal placements, 1x1 holes
         */
        UPDATED
    }

    public enum Rotate {
        /**
         * Do not rotate
         */
        NONE(RotationType.NONE),

        /**
         * Rotate client side
         */
        CLIENT(RotationType.CLIENT),

        /**
         * Rotate server side
         */
        SERVER(RotationType.SERVER),

        /**
         * Rotate server side, but limit rotations.
         *
         * If the YawRate > yaw/pitch difference, it'll split up rotations into multiple ticks
         */
        LIMITED(RotationType.SERVER);

        private final RotationType rotationType;

        Rotate(RotationType type) {
            rotationType = type;
        }

    }

    public enum YawLimit {
        /**
         * Only limits rotations when placing
         */
        SEMI,

        /**
         * Limits rotations while placing and exploding crystals
         */
        FULL
    }
}
