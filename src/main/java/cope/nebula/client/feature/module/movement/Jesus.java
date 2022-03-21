package cope.nebula.client.feature.module.movement;

import cope.nebula.asm.mixins.network.packet.c2s.ICPacketPlayer;
import cope.nebula.client.events.BlockCollisionEvent;
import cope.nebula.client.events.MotionUpdateEvent;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

public class Jesus extends Module {
    public Jesus() {
        super("Jesus", ModuleCategory.MOVEMENT, "Walks on water");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.TRAMPOLINE);
    public static final Value<Boolean> dip = new Value<>("Dip", true);
    public static final Value<Boolean> lava = new Value<>("Lava", false);

    private int waterTicks = 0;
    private double y = 0.0;

    private boolean flag = false;
    private int materialAccelSpeed = 0;

    private int ticksInWater = 0;

    @Override
    public void onTick() {
        if (!flag) {
            ++materialAccelSpeed;
            if (materialAccelSpeed >= 20) {
                materialAccelSpeed = 60;
                flag = true;
            }
        } else {
            --materialAccelSpeed;
            if (materialAccelSpeed <= 0) {
                flag = false;
                materialAccelSpeed = 0;
            }
        }

        if (isOnLiquid()) {
            ++ticksInWater;
        } else {
            ticksInWater = 0;
        }

        if (mode.getValue().equals(Mode.TRAMPOLINE)) {
            if (isInValidLiquid()) {
                ++waterTicks;
                if (waterTicks > 2) {
                    mc.player.jump();
                    mc.player.motionY += 7.0E-4;

                    waterTicks = 0;
                }
            }
        } else if (mode.getValue().equals(Mode.DOLPHIN)) {
            if (isInValidLiquid()) {
                mc.player.jump();
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof CPacketPlayer && event.getDirection().equals(Direction.OUTGOING)) {
            CPacketPlayer packet = event.getPacket();

            if (((ICPacketPlayer) packet).isMoving() && isOnLiquid() && !isInValidLiquid()) {
                ((ICPacketPlayer) packet).setOnGround(false);

                if (mode.getValue().equals(Mode.NCP) && mc.player.ticksExisted % 2 == 0) {
                    ((ICPacketPlayer) packet).setY(packet.getY(mc.player.posY) - 0.03);
                }

                if (mode.getValue().equals(Mode.STRICT)) {
                    if (ticksInWater == 0 || ticksInWater < 8) {
                        return;
                    }

                    y += ThreadLocalRandom.current().nextDouble(0.002, 0.01);
                    if (y >= 0.2) {
                        y = 0.02;
                    }

                    ((ICPacketPlayer) packet).setY(packet.getY(mc.player.posY) - y);
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockCollision(BlockCollisionEvent event) {
        if (event.getBlock() instanceof BlockLiquid && event.getEntity() != null && event.getEntity().equals(mc.player)) {
            if (mode.getValue().equals(Mode.DOLPHIN) || mode.getValue().equals(Mode.TRAMPOLINE)) {
                return;
            }

            if (event.getBlock().equals(Blocks.LAVA) && !lava.getValue()) {
                return;
            }

            if ((dip.getValue() && mc.player.fallDistance > 3.0f) || mc.player.isBurning()) {
                return;
            }

            if (mc.player.isSneaking() || mc.gameSettings.keyBindJump.isKeyDown()) {
                return;
            }

            if (isInValidLiquid() || !isOnLiquid()) {
                return;
            }

            if (mode.getValue().equals(Mode.STRICT) && flag) {
                mc.player.motionX *= 0.55;
                mc.player.motionZ *= 0.55;
            }

            AxisAlignedBB box = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.921, 1.0).offset(event.getPos());
            if (event.getBox().intersects(box)) {
                event.getBoxes().add(box);
                event.setCanceled(true);
            }
        }
    }

    private boolean isOnLiquid() {
        double y = mc.player.posY - 0.05;

        for (int x = (int) Math.floor(mc.player.posX); x < Math.ceil(mc.player.posX); ++x) {
            for (int z = (int) Math.floor(mc.player.posZ); z < Math.ceil(mc.player.posZ); ++z) {
                BlockPos pos = new BlockPos(x, Math.floor(y), z);
                if (mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isInValidLiquid() {
        return mc.player.isInWater() || (lava.getValue() && mc.player.isInLava());
    }

    public enum Mode {
        NCP, STRICT, TRAMPOLINE, DOLPHIN
    }
}
