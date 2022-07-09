package wtf.nebula.asm.transform.impl.network

import jdk.internal.org.objectweb.asm.Type
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import wtf.nebula.asm.hooks.NetworkManagerHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.network.NetworkManager")
class NetworkManagerTransformer : ClassTransformer() {
    @Injection(name = "sendPacket", descriptor = "(Lnet/minecraft/network/Packet;)V")
    fun sendPacket(node: MethodNode) {
        val instructions = InsnList()

        // our instruction call for NetworkManagerHook#sendPacket(packetIn)Z
        instructions.add(VarInsnNode(ALOAD, 1))
        instructions.add(MethodInsnNode(
            INVOKESTATIC,
            Type.getInternalName(NetworkManagerHook::class.java),
            "sendPacket",
            "(Lnet/minecraft/network/Packet;)Z", // TODO: remap packet names
            false
        ))

        // wrap in the if statement, so we can have cancelable packet events
        val label = LabelNode()

        instructions.add(JumpInsnNode(IFEQ, label))
        instructions.add(InsnNode(RETURN))
        instructions.add(label)

        // add our edited instructions
        node.instructions.insert(instructions)
    }

    @Injection(name = "channelRead0", descriptor = "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V")
    fun channelRead0(node: MethodNode) {
        val instructions = InsnList()

        // our instruction call for NetworkManagerHook#channelRead0(packetIn)Z
        instructions.add(VarInsnNode(ALOAD, 1))
        instructions.add(MethodInsnNode(
            INVOKESTATIC,
            Type.getInternalName(NetworkManagerHook::class.java),
            "channelRead0",
            "(Lnet/minecraft/network/Packet;)Z", // TODO: remap packet names
            false
        ))

        // wrap in the if statement, so we can have cancelable packet events
        val label = LabelNode()

        instructions.add(JumpInsnNode(IFEQ, label))
        instructions.add(InsnNode(RETURN))
        instructions.add(label)

        // add our edited instructions
        node.instructions.insert(instructions)
    }
}