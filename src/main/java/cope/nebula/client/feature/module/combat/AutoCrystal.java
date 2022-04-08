package cope.nebula.client.feature.module.combat;

import cope.nebula.asm.duck.IEntityPlayer;
import cope.nebula.client.events.EntityAddedEvent;
import cope.nebula.client.events.EntityRemoveEvent;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.internal.timing.TimeFormat;
import cope.nebula.util.renderer.FontUtil;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
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
    public static final Value<Weakness> antiWeakness = new Value<>(explode, "AntiWeakness", Weakness.SERVER);

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

    public static final Value<Boolean> swing = new Value<>("Swing", true);
    public static final Value<Merge> merge = new Value<>("Merge", Merge.CONFIRM);

    // targets
    private EntityPlayer target = null;
    private BlockPos placePos = null;
    private EntityEnderCrystal attackCrystal = null;

    private float damage = 0.5f;
    private int entityIdSpawn = -1;

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

    private final Set<Integer> placedCrystals = new HashSet<>();
    private final Map<Integer, Stopwatch> inhibitCrystals = new HashMap<>();

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

            // box render

            RenderUtil.renderFilledBox(box, color);
            RenderUtil.renderOutlinedBox(box, 3.5f, color);

            // text render

            String text = String.format("%.2f", damage);

            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();

            RenderManager renderManager = mc.getRenderManager();

            GlStateManager.translate((placePos.getX() + 0.5f) - renderManager.viewerPosX, (placePos.getY() + 0.5f) - renderManager.viewerPosY, (placePos.getZ() + 0.5f) - renderManager.viewerPosZ);
            GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-mc.player.rotationYaw, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(mc.player.rotationPitch, mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
            GlStateManager.scale(-0.02666667, -0.02666667, 0.02666667);

            GlStateManager.translate(-(FontUtil.getWidth(text) / 2.0), 0.0, 0.0);

            FontUtil.drawString(text, 0.0f, 0.0f, -1);

            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void onTick() {
        // find place position and best end crystal to attack
        findBestPlacePosition();
        findBestEndCrystal();

        if (explode.getValue() && attackCrystal != null) {
            // get the entity id for inhibit
            int entityId = attackCrystal.getEntityId();
            if (shouldInhibit(entityId)) {
                return;
            }

            // check if we have passed the required amount of ticks to explode a crystal
            long elapsed = explodeTimer.getTime(TimeFormat.TICKS);
            if (elapsed >= 20.0f - explodeSpeed.getValue()) {

                // reset time
                explodeTimer.resetTime();

                if (mc.player.isPotionActive(MobEffects.WEAKNESS) &&
                        !mc.player.isPotionActive(MobEffects.STRENGTH) &&
                        !antiWeakness.getValue().equals(Weakness.NONE)) {

                    // TODO: dont just swap to swords, allow axe and pick swapping as well
                }

                // send rotations and check if we should limit to the next tick
                if (!rotate.getValue().equals(Rotate.NONE) &&
                        sendRotations(AngleUtil.toEntity(attackCrystal, Bone.HEAD))) {

                    // TODO
                }

                // attack the crystal
                CrystalUtil.explode(entityId, hand, swing.getValue());

                // add crystal to inhibited crystal map
                if (inhibit.getValue()) {
                    inhibitCrystals.put(entityId, new Stopwatch().resetTime());
                } else {
                    // reset crystal
                    attackCrystal = null;
                }
            }
        }

        if (place.getValue() && placePos != null) {

            // check if we have passed the required amount of ticks to place a crystal
            long elapsed = placeTimer.getTime(TimeFormat.TICKS);
            if (elapsed >= 20.0f - placeSpeed.getValue()) {

                // check if there are any crystals in our hotbar.
                // if not, we cannot continue as theres no crystals...
                if (!findCrystals()) {
                    return;
                }

                // check if we can place a crystal here
                // where we pass inhibit.getValue(), it will check if theres a crystal already there.
                // if we cannot place, it'll return. as with above, this prevents unnecessary place packets
                if (!CrystalUtil.canPlaceAt(placePos, inhibit.getValue(), protocol.getValue().equals(Protocol.UPDATED))) {
                    return;
                }

                // reset time
                placeTimer.resetTime();

                // place crystal
                CrystalUtil.placeAt(placePos, hand, interact.getValue().equals(Interact.STRICT), swing.getValue(), rotate.getValue().rotationType);

                // if silent swap is active, we'll swap back
                if (swapping.getValue().equals(SwapType.SERVER)) {
                    swapBack();
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityAdded(EntityAddedEvent event) {
        if (event.getEntity() instanceof EntityEnderCrystal) {
            EntityEnderCrystal crystal = (EntityEnderCrystal) event.getEntity();
            if (placePos == null) {
                return;
            }

            if (placePos.equals(crystal.getPosition().down()) || crystal.getEntityId() == entityIdSpawn) {
                attackCrystal = crystal;
                placedCrystals.add(crystal.getEntityId());
            }
        }
    }

    @SubscribeEvent
    public void onEntityRemove(EntityRemoveEvent event) {
        if (event.getEntity() instanceof EntityEnderCrystal) {
            if (attackCrystal == null) {
                return;
            }

            if (event.getEntity().equals(attackCrystal)) {
                placedCrystals.remove(attackCrystal.getEntityId());
                inhibitCrystals.remove(attackCrystal.getEntityId());

                attackCrystal.setDead();
                attackCrystal = null;
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING)) {
            if (event.getPacket() instanceof SPacketDestroyEntities) {
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

                    if (merge.getValue().equals(Merge.CONFIRM)) {
                        mc.world.removeEntityDangerously(entity);
                    }
                }
            } else if (event.getPacket() instanceof SPacketExplosion) {
                SPacketExplosion packet = event.getPacket();
                double power = packet.getStrength() * packet.getStrength();

                mc.addScheduledTask(() -> {
                    for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
                        if (!(entity instanceof EntityEnderCrystal) || entity.isDead) {
                            continue;
                        }

                        if (entity.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()) < power) {
                            if (attackCrystal != null && attackCrystal.getEntityId() == entity.getEntityId()) {
                                attackCrystal = null;
                            }

                            entity.setDead();
                            mc.world.removeEntity(entity);

                            if (merge.getValue().equals(Merge.CONFIRM)) {
                                mc.world.removeEntityDangerously(entity);
                            }
                        }
                    }
                });
            } else if (event.getPacket() instanceof SPacketSoundEffect) {
                SPacketSoundEffect packet = event.getPacket();

                if (packet.getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
                    mc.addScheduledTask(() -> {
                        for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
                            if (!(entity instanceof EntityEnderCrystal) || entity.isDead) {
                                continue;
                            }

                            if (entity.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()) < 12.0) {
                                if (attackCrystal != null && attackCrystal.getEntityId() == entity.getEntityId()) {
                                    attackCrystal = null;
                                }

                                entity.setDead();
                                mc.world.removeEntity(entity);

                                if (merge.getValue().equals(Merge.CONFIRM)) {
                                    mc.world.removeEntityDangerously(entity);
                                }
                            }
                        }
                    });
                }
            } else if (event.getPacket() instanceof SPacketSpawnObject) {
                SPacketSpawnObject packet = event.getPacket();
                if (packet.getType() == 51) {
                    BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                    if (placePos != null && placePos.equals(pos.down())) {
                        ++crystalCount;

                        entityIdSpawn = packet.getEntityID();
                        placedCrystals.add(packet.getEntityID());

                        if (explodeSpeed.getValue() == 20.0) {
                            if (!rotate.getValue().equals(Rotate.NONE)) {
                                Rotation rotation = AngleUtil.toVec(new Vec3d(pos).add(0.5, 0.5, 0.5));
                                if (rotation.isValid()) {
                                    getNebula().getRotationManager().setRotation(rotation);
                                }
                            }

                            CrystalUtil.explode(packet.getEntityID(), hand, swing.getValue());
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
        float currDamage = 1.0f;

        BlockPos origin = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        for (BlockPos pos : BlockUtil.sphere(origin, placeRange.getValue().intValue())) {
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

            float targetDamage = currDamage;
            if (target != null) {
                targetDamage = ExplosionUtil.calculateCrystalDamage(target, vec, ignoreTerrain.getValue());
                if (targetDamage < minDamage.getValue() || localDamage > targetDamage) {
                    continue;
                }
            }

            for (EntityPlayer player : mc.world.playerEntities) {
                float playerDamage = ExplosionUtil.calculateCrystalDamage(player, vec, ignoreTerrain.getValue());

                // protect friends
                if (((IEntityPlayer) player).isFriend()) {
                    continue;
                }

                if (playerDamage > targetDamage && playerDamage > localDamage) {
                    targetDamage = playerDamage;
                    target = player;
                }
            }

            if (targetDamage > currDamage) {
                position = pos;
                currDamage = targetDamage;
            }
        }

        placePos = position;
        damage = currDamage;
    }

    /**
     * Finds the best end crystal to attack
     *
     * I should make this sort by damage done to player, but in the end it doesn't really matter
     */
    private void findBestEndCrystal() {
        EntityEnderCrystal crystal = attackCrystal;

        if (crystal == null) {
            double dist = 0.0;
            for (int entityId : new HashSet<>(placedCrystals)) {
                Entity entity = mc.world.getEntityByID(entityId);
                if (entity == null || entity.isDead || entity.ticksExisted < ticksExisted.getValue()) {
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
        }

        if (crystal != null && shouldInhibit(crystal.getEntityId())) {
            crystal = null;
        }

        attackCrystal = crystal;
    }

    /**
     * Checks if we should limit our attacks based off of inhibit
     * @param entityId the crystal entity id
     * @return the entity id
     */
    private boolean shouldInhibit(int entityId) {
        Stopwatch stopwatch  = inhibitCrystals.getOrDefault(entityId, null);
        if (!inhibit.getValue() || stopwatch == null) {
            return false;
        }

        int ping = 50 + getNebula().getServerManager().getLatency(mc.player.getUniqueID());
        if (!stopwatch.passedMs(ping)) {
            return true;
        } else {
            inhibitCrystals.remove(entityId);
        }

        return false;
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
     * Spoofs rotations sent to the server
     * @param rotation the requested rotation
     * @return if we should limit and wait
     */
    private boolean sendRotations(Rotation rotation) {
        // TODO: limit

        if (rotation.isValid()) {
            getNebula().getRotationManager().setRotation(rotation);
        }

        return false;
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

    public enum Weakness {
        /**
         * Does nothing about weakness
         */
        NONE(SwapType.NONE),

        /**
         * Swaps to a tool server side
         */
        CLIENT(SwapType.CLIENT),

        /**
         * Swaps to a tool server side
         */
        SERVER(SwapType.SERVER);

        private final SwapType swapType;

        Weakness(SwapType swapType) {
            this.swapType = swapType;
        }
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

    public enum Merge {
        /**
         * Does no merging
         */
        NONE,

        /**
         * Removes the crystal from the world instantly after sending the attack
         */
        INSTANT,

        /**
         * Removes the crystal from the world instantly once a packet has confirmed that crystal is gone
         */
        CONFIRM
    }
}
