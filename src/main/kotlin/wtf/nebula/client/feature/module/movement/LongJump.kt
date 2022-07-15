package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import org.lwjgl.input.Keyboard
import wtf.nebula.client.Nebula
import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.event.packet.PacketReceiveEvent
import wtf.nebula.client.event.player.motion.MotionEvent
import wtf.nebula.client.event.player.motion.update.PreMotionUpdate
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.ext.isMoving
import wtf.nebula.util.motion.MotionUtil
import kotlin.IndexOutOfBoundsException
import kotlin.doubleArrayOf
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class LongJump : Module(ModuleCategory.MOVEMENT, "Makes you go long asf aka best way to pussy out a fight LOL") {
    val mode by setting("Mode", Mode.NORMAL)
    val boost by double("Boost", 6.2, 1.0..10.0)

    private var stage = LongJumpStage.COLLIDE
    private var moveSpeed = 0.0
    private var distance = 0.0

    private var airTicks = 0
    private var groundTicks = 0

    override fun getDisplayInfo(): String? {
        return Setting.formatEnum(mode)
    }

    override fun onActivated() {
        super.onActivated()
        stage = LongJumpStage.START
        moveSpeed = 0.0
        distance = 0.0
    }

    override fun onDeactivated() {
        super.onDeactivated()
        Nebula.serverManager.speed = 1.0f

        airTicks = 0
        groundTicks = 0
    }

    @EventListener
    private val motionListener = listener<MotionEvent> {
        if (mode == Mode.NORMAL) {

            when (stage) {
                LongJumpStage.START -> {
                    stage = LongJumpStage.JUMP
                    moveSpeed = boost * MotionUtil.getBaseNCPSpeed() - 0.01
                }

                LongJumpStage.JUMP -> {
                    stage = LongJumpStage.SPEED

                    mc.player.motionY = 0.42
                    it.y = 0.42

                    moveSpeed *= 2.149
                }

                LongJumpStage.SPEED -> {
                    stage = LongJumpStage.COLLIDE

                    val adjusted = 0.66 * (distance - MotionUtil.getBaseNCPSpeed())
                    moveSpeed = distance - adjusted
                }

                LongJumpStage.COLLIDE -> {
                    val boxes = mc.world.getCollisionBoxes(mc.player, mc.player.entityBoundingBox.offset(0.0, mc.player.motionY, 0.0))
                    if (boxes.isNotEmpty() && mc.player.collidedVertically) {
                        stage = LongJumpStage.START
                    }

                    moveSpeed -= moveSpeed / 159.0
                }
            }

            moveSpeed = max(moveSpeed, MotionUtil.getBaseNCPSpeed())

            val strafe = MotionUtil.strafe(moveSpeed)

            if (mc.player.isMoving()) {
                it.x = strafe[0]
                it.z = strafe[1]
            }

            else {
                it.x = 0.0
                it.z = 0.0
            }
        }

        else if (mode == Mode.COWABUNGA) {
            val dir = MotionUtil.getMovement()[2]

            val xDir = cos((dir + 90.0f) * Math.PI / 180.0).toFloat()
            val zDir = sin((dir + 90.0f) * Math.PI / 180.0).toFloat()

            if (!mc.player.collidedVertically) {
                airTicks++
                groundTicks = 0

                when (mc.player.motionY) {
                    -0.07190068807140403 -> mc.player.motionY *= 0.3499999940395355
                    -0.10306193759436909 -> mc.player.motionY *= 0.550000011920929
                    -0.13395038817442878 -> mc.player.motionY *= 0.6700000166893005
                    -0.16635183030382 -> mc.player.motionY *= 0.6899999976158142
                    -0.19088711097794803 -> mc.player.motionY *= 0.7099999785423279
                    -0.21121925191528862 -> mc.player.motionY *= 0.20000000298023224
                    -0.11979897632390576 -> mc.player.motionY *= 0.9300000071525574
                    -0.18758479151225355 -> mc.player.motionY *= 0.7200000286102295
                    -0.21075983825251726 -> mc.player.motionY *= 0.7599999904632568
                }

                if (mc.player.motionY < -0.2 && mc.player.motionY > -0.24) {
                    mc.player.motionY *= 0.7
                }

                if (mc.player.motionY < -0.25 && mc.player.motionY > -0.32) {
                    mc.player.motionY *= 0.8
                }

                if (mc.player.motionY < -0.35 && mc.player.motionY > -0.8) {
                    mc.player.motionY *= 0.98
                }

                if (mc.player.motionY < -0.8 && mc.player.motionY > -1.6) {
                    mc.player.motionY *= 0.99
                }

                Nebula.serverManager.speed = 0.8f

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode)) {
                    try {
                        val speedVal = AIR_SPEED[airTicks - 1] * 3.0
                        mc.player.motionX = xDir * speedVal
                        mc.player.motionZ = zDir * speedVal
                    } catch (ignored: IndexOutOfBoundsException) {
                    }
                }

                else {
                    mc.player.motionX = 0.0
                    mc.player.motionZ = 0.0
                }

            }

            else {
                Nebula.serverManager.speed = 1.0f

                airTicks = 0
                groundTicks++

                mc.player.motionX /= 13.0
                mc.player.motionZ /= 13.0

                if (groundTicks == 1) {
                    sendPacketPos(0.0, 0.0, 0.0)
                    sendPacketPos(0.0624, 0.0, 0.0)
                    sendPacketPos(0.0, 0.419, 0.0)
                    sendPacketPos(0.0624, 0.0, 0.0)
                    sendPacketPos(0.0, 0.419, 0.0)
                }

                if (groundTicks > 2) {
                    groundTicks = 0

                    mc.player.motionX = xDir * 0.3
                    mc.player.motionZ = zDir * 0.3
                    mc.player.motionY = 0.42399999499320984
                }
            }

            it.x = mc.player.motionX
            it.y = mc.player.motionY
            it.z = mc.player.motionZ
        }
    }

    @EventListener
    private val preMotionUpdate = listener<PreMotionUpdate> {
        val diffX = mc.player.posX - mc.player.prevPosX
        val diffZ = mc.player.posZ - mc.player.prevPosZ

        distance = sqrt(diffX * diffX + diffZ * diffZ)
    }

    @EventListener
    private val packetReceiveListener = listener<PacketReceiveEvent> {
        if (it.packet is SPacketPlayerPosLook) {
            toggled = false
        }
    }

    private fun sendPacketPos(x: Double, y: Double, z: Double) {
        mc.player.connection.sendPacket(CPacketPlayer.Position(x, y, z, false))
    }

    enum class Mode {
        NORMAL, COWABUNGA
    }

    enum class LongJumpStage {
        START, JUMP, SPEED, COLLIDE
    }

    companion object {
        private val AIR_SPEED = doubleArrayOf(
            0.420606, 0.417924, 0.415258, 0.412609, 0.409977, 0.407361, 0.404761, 0.402178, 0.399611, 0.39706,
            0.394525, 0.392, 0.3894, 0.38644, 0.383655, 0.381105, 0.37867, 0.37625, 0.37384, 0.37145,
            0.369, 0.3666, 0.3642, 0.3618, 0.35945, 0.357, 0.354, 0.351, 0.348, 0.345,
            0.342, 0.339, 0.336, 0.333, 0.33, 0.327, 0.324, 0.321, 0.318, 0.315,
            0.312, 0.309, 0.307, 0.305, 0.303, 0.3, 0.297, 0.295, 0.293, 0.291,
            0.289, 0.287, 0.285, 0.283, 0.281, 0.279, 0.277, 0.275, 0.273, 0.271,
            0.269, 0.267, 0.265, 0.263, 0.261, 0.259, 0.257, 0.255, 0.253, 0.251,
            0.249, 0.247, 0.245, 0.243, 0.241, 0.239, 0.237
        )
    }
}