package wtf.nebula.asm.transform.impl.render

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import wtf.nebula.asm.hooks.RenderPlayerHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.client.renderer.entity.RenderPlayer")
class RenderPlayerTransformer : ClassTransformer() {

    // i wanted to kill myself writing this
    @Injection(name = "doRender", descriptor = "(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V")
    fun doRender(node: MethodNode) {
        val preInstructions = InsnList()

        preInstructions.add(MethodInsnNode(INVOKESTATIC, "net/minecraft/client/Minecraft", "getMinecraft", "()Lnet/minecraft/client/Minecraft;", false))
        preInstructions.add(FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "player", "Lnet/minecraft/client/entity/EntityPlayerSP;"))

        preInstructions.add(VarInsnNode(ALOAD, 1))
        preInstructions.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false))

        val preLabel = LabelNode()
        preInstructions.add(JumpInsnNode(IFEQ, preLabel))
        preInstructions.add(VarInsnNode(ALOAD, 1))
        preInstructions.add(invokeStatic(RenderPlayerHook::class.java, "updatePlayerRotations", "(Lnet/minecraft/entity/player/EntityPlayer;)V"))
        preInstructions.add(preLabel)

        node.instructions.insert(preInstructions)

        val postInstructions = InsnList()

        postInstructions.add(MethodInsnNode(INVOKESTATIC, "net/minecraft/client/Minecraft", "getMinecraft", "()Lnet/minecraft/client/Minecraft;", false))
        postInstructions.add(FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "player", "Lnet/minecraft/client/entity/EntityPlayerSP;"))

        postInstructions.add(VarInsnNode(ALOAD, 1))
        postInstructions.add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false))

        val postLabel = LabelNode()
        postInstructions.add(JumpInsnNode(IFEQ, postLabel))
        postInstructions.add(VarInsnNode(ALOAD, 1))
        postInstructions.add(invokeStatic(RenderPlayerHook::class.java, "resetPlayerRotations", "(Lnet/minecraft/entity/player/EntityPlayer;)V"))
        postInstructions.add(postLabel)

        node.instructions.insertBefore(node.instructions.get(node.instructions.size() - 2), postInstructions)
    }
}