package wtf.nebula.asm.transform.impl.entity.player

import jdk.internal.org.objectweb.asm.Type
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import wtf.nebula.asm.hooks.EntityPlayerSPHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.client.entity.EntityPlayerSP")
class EntityPlayerSPTransformer : ClassTransformer() {
    @Injection(name = "onUpdateWalkingPlayer")
    fun onUpdateWalkingPlayer(node: MethodNode) {

        // pre
        val preInstructions = InsnList()

        preInstructions.add(MethodInsnNode(
            Opcodes.INVOKESTATIC,
            Type.getInternalName(EntityPlayerSPHook::class.java),
            "preMotionUpdate",
            "()Z",
            false))

        val label = LabelNode()

        preInstructions.add(JumpInsnNode(Opcodes.IFEQ, label))
        preInstructions.add(InsnNode(Opcodes.RETURN))
        preInstructions.add(label)

        // add our edited instructions
        node.instructions.insert(preInstructions)

        // post
        val postInstructions = InsnList()
        postInstructions.add(MethodInsnNode(
            Opcodes.INVOKESTATIC,
            Type.getInternalName(EntityPlayerSPHook::class.java),
            "postMotionUpdate",
            "()V",
            false))

        node.instructions.insertBefore(
            node.instructions.get(node.instructions.size() - 2),
            postInstructions)
    }
}