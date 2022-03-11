package cope.nebula.asm.mixins.entity;

import com.mojang.authlib.GameProfile;
import cope.nebula.client.events.MotionEvent;
import cope.nebula.client.events.MotionUpdateEvent;
import cope.nebula.client.events.MotionUpdateEvent.Era;
import cope.nebula.util.Globals;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
    @Shadow @Final
    public NetHandlerPlayClient connection;

    @Shadow private double lastReportedPosX;
    @Shadow private double lastReportedPosY;
    @Shadow private double lastReportedPosZ;
    @Shadow private float lastReportedYaw;
    @Shadow private float lastReportedPitch;
    @Shadow private boolean prevOnGround;
    @Shadow private boolean serverSneakState;
    @Shadow private boolean serverSprintState;
    @Shadow private int positionUpdateTicks;

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Shadow protected abstract boolean isCurrentViewEntity();

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void move(AbstractClientPlayer player, MoverType type, double x, double y, double z) {
        MotionEvent event = new MotionEvent(x, y, z);
        Globals.EVENT_BUS.post(event);

        if (!event.isCanceled()) {
            super.move(type, event.getX(), event.getY(), event.getZ());
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onUpdateWalkingPlayerPre(CallbackInfo info) {
        MotionUpdateEvent event = new MotionUpdateEvent(posX, getEntityBoundingBox().minY, posZ, rotationYaw, rotationPitch, onGround);
        Globals.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
            spoofUpdateWalkingPlayer(event);
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    public void onUpdateWalkingPlayerPost(CallbackInfo info) {
        Globals.EVENT_BUS.post(new MotionUpdateEvent());
    }

    /**
     * This is taken from minecraft's source and modified to allow us to spoof rotation / position values
     *
     * @see EntityPlayerSP#onUpdateWalkingPlayer()
     * @param event The motion update event marked as PRE
     */
    private void spoofUpdateWalkingPlayer(MotionUpdateEvent event) {
        if (!event.getEra().equals(Era.PRE)) {
            return;
        }

        // sync sprint state with the server
        if (isSprinting() != serverSprintState) {
            serverSprintState = isSprinting();

            connection.sendPacket(new CPacketEntityAction(this,
                    isSprinting() ? Action.START_SPRINTING : Action.STOP_SPRINTING));
        }

        // sync sneak state with the server
        if (isSneaking() != serverSneakState) {
            serverSneakState = isSneaking();

            connection.sendPacket(new CPacketEntityAction(this,
                    isSneaking() ? Action.START_SNEAKING : Action.STOP_SNEAKING));
        }

        // if we are currently viewing this entity
        if (isCurrentViewEntity()) {
            // update our position update ticks
            ++positionUpdateTicks;

            // find differences in our x, y, and z values
            double diffX = event.getX() - lastReportedPosX;
            double diffY = event.getY() - lastReportedPosY;
            double diffZ = event.getZ() - lastReportedPosZ;

            // if we have moved at all
            boolean moved = diffX * diffX + diffY * diffY + diffZ * diffZ > 9.0E-4 || positionUpdateTicks >= 20;

            // differences in our yaw & pitch
            float diffYaw = event.getYaw() - lastReportedYaw;
            float diffPitch = event.getPitch() - lastReportedPitch;

            // if we have rotated at all
            boolean rotated = diffYaw != 0.0f || diffPitch != 0.0f;

            // if we are riding, we send a packet with our motion x and z and then -999 for our y value, with our rotations
            if (isRiding()) {
                connection.sendPacket(new PositionRotation(motionX, -999.0, motionZ, event.getYaw(), event.getPitch(), event.isOnGround()));
                moved = false;
            } else {
                // sync our position with the server
                if (moved && rotated) {
                    connection.sendPacket(new PositionRotation(event.getX(), event.getY(), event.getZ(), event.getYaw(), event.getPitch(), event.isOnGround()));
                } else if (moved) {
                    connection.sendPacket(new Position(event.getX(), event.getY(), event.getZ(), event.isOnGround()));
                } else if (rotated) {
                    connection.sendPacket(new Rotation(event.getYaw(), event.getPitch(), event.isOnGround()));
                } else if (prevOnGround != onGround) {
                    connection.sendPacket(new CPacketPlayer(onGround));
                }
            }

            // cache our current x, y, and z values and reset position update ticks
            if (moved) {
                lastReportedPosX = event.getX();
                lastReportedPosY = event.getY();
                lastReportedPosZ = event.getZ();

                positionUpdateTicks = 0;
            }

            // cache our current yaw and pitch
            if (rotated) {
                lastReportedYaw = event.getYaw();
                lastReportedPitch = event.getPitch();
            }

            // make sure we cache our onGround state as well
            prevOnGround = onGround;
        }
    }
}
