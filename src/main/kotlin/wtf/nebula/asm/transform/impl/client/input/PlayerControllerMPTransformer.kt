package wtf.nebula.asm.transform.impl.client.input

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import wtf.nebula.asm.hooks.PlayerControllerMPHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.client.multiplayer.PlayerControllerMP")
class PlayerControllerMPTransformer : ClassTransformer() {
    // clickBlock(BlockPos loc, EnumFacing face)
    @Injection(name = "clickBlock", descriptor = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z")
    fun clickBlock(node: MethodNode) {
        val instructions = InsnList()

        instructions.add(VarInsnNode(ALOAD, 1))
        instructions.add(VarInsnNode(ALOAD, 2))

        instructions.add(invokeStatic(
            PlayerControllerMPHook::class.java,
            "clickBlock",
            "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z"))

        val label = LabelNode()
        instructions.add(JumpInsnNode(IFEQ, label))
        instructions.add(InsnNode(ICONST_0))
        instructions.add(InsnNode(IRETURN))
        instructions.add(label)

        node.instructions.insert(instructions)
    }
}