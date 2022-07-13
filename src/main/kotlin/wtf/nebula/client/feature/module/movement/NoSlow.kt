package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemShield
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.client.settings.IKeyConflictContext
import net.minecraftforge.client.settings.KeyConflictContext
import org.lwjgl.input.Keyboard
import wtf.nebula.client.event.input.KeyBindingPressedEvent
import wtf.nebula.client.event.input.MovementInputEvent
import wtf.nebula.client.event.packet.PacketReceiveEvent
import wtf.nebula.client.event.player.motion.update.Era
import wtf.nebula.client.event.player.motion.update.MotionUpdateBase
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.Globals

class NoSlow : Module(ModuleCategory.MOVEMENT, "Makes you not slow") {
    val mode by setting("Mode", Mode.NCP)
    val inventoryWalk by bool("Inventory Walk", true)
    val inventoryStrict by bool("Inventory Strict", false)

    private var sneakState = false
    private var sprintState = false

    override fun onDeactivated() {
        super.onDeactivated()

        BINDS.forEach { it.keyConflictContext = KeyConflictContext.IN_GAME }

        if (sneakState && !mc.player.isSneaking) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
        }

        if (sprintState && mc.player.isSprinting) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING))
        }

        sneakState = false
        sprintState = false
    }

    @EventListener
    private val movementInputListener = listener<MovementInputEvent> {
        if (mc.player.isHandActive && !mc.player.isRiding) {
            it.movementInput.moveForward *= 5.0f
            it.movementInput.moveStrafe *= 5.0f
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (inventoryWalk) {
            if (mc.currentScreen != null) {
                for (bind in BINDS) {
                    KeyBinding.setKeyBindState(bind.keyCode, Keyboard.isKeyDown(bind.keyCode))
                    bind.keyConflictContext = KeyConflictOverrides.FAKE
                }
            } else BINDS.forEach { it.keyConflictContext = KeyConflictContext.IN_GAME }
        }

        if (mode == Mode.AIRSTRICT && !mc.player.isHandActive) {
            if (sneakState && !mc.player.isSneaking) {
                sneakState = false
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
            }

            if (sprintState && mc.player.isSprinting) {
                sprintState = false
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING))
            }
        }
    }

    @EventListener
    private val motionUpdateListener = listener<MotionUpdateBase> {
        if (mode == Mode.NCP && mc.player.activeItemStack.item is ItemShield) {
            if (it.era == Era.PRE) {
                mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }

            else {
                mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(BlockPos(-1, -1, -1), EnumFacing.DOWN, mc.player.activeHand, 0.0f, 0.0f, 0.0f))
            }
        }
    }

    @EventListener
    private val packetReceiveListener = listener<PacketReceiveEvent> {
        if (it.packet is CPacketPlayer) {

            when (mode) {
                Mode.VANILLA, Mode.NCP -> return@listener
                Mode.STRICT -> mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
                Mode.AIRSTRICT -> {
                    if (!sneakState && !mc.player.isSneaking) {
                        sneakState = true
                        mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
                    }

                    if (!sprintState && mc.player.isSprinting) {
                        sprintState = true
                        mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING))
                    }
                }
            }
        }

        else if (it.packet is CPacketClickWindow) {
            if (!inventoryStrict) {
                return@listener
            }

            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING))
        }
    }

    @EventListener
    private val keyBindingPressedListener = listener<KeyBindingPressedEvent> {
        if (inventoryWalk && mc.currentScreen != null) {
            it.pressed = Keyboard.isKeyDown(it.keyBinding.keyCode)
        }
    }

    enum class Mode {
        VANILLA, NCP, STRICT, AIRSTRICT
    }

    companion object : Globals {
        private val BINDS = arrayOf<KeyBinding>(
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindSneak,
            mc.gameSettings.keyBindJump
        )
    }
}

enum class KeyConflictOverrides : IKeyConflictContext {
    FAKE {
        override fun isActive(): Boolean = false
        override fun conflicts(other: IKeyConflictContext?): Boolean = false
    }
}