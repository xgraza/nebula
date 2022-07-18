package wtf.nebula.asm.transform.impl.render

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import wtf.nebula.asm.hooks.EntityRendererHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.client.renderer.EntityRenderer")
class EntityRendererTransformer : ClassTransformer() {
    @Injection(name = "renderWorldPass", descriptor = "(IFJ)V")
    fun renderWorldPass(node: MethodNode) {
        var lastNode: MethodInsnNode? = null
        for (i in 0 until node.instructions.size()) {
            val instruction = node.instructions[i]
            if (instruction is MethodInsnNode) {
                if (instruction.name == "dispatchRenderLast") {
                    lastNode = instruction
                }
            }
        }

        if (lastNode != null) {
            val instructions = InsnList()
            instructions.add(invokeStatic(EntityRendererHook::class.java, "renderWorld", "()V"))
            node.instructions.insertBefore(lastNode, instructions)
        }
    }

    @Injection(name = "hurtCameraEffect", descriptor = "(F)V")
    fun hurtCameraEffect(node: MethodNode) {
        val instructions = InsnList()
        instructions.add(invokeStatic(EntityRendererHook::class.java, "renderHurtcam", "()Z"))

        val label = LabelNode()

        instructions.add(JumpInsnNode(Opcodes.IFEQ, label))
        instructions.add(InsnNode(Opcodes.RETURN))
        instructions.add(label)

        node.instructions.insert(instructions)
    }

    @Injection(name = "displayItemActivation", descriptor = "(Lnet/minecraft/item/ItemStack;)V")
    fun displayItemActivation(node: MethodNode) {
        val instructions = InsnList()
        instructions.add(invokeStatic(EntityRendererHook::class.java, "displayItemActivation", "()Z"))

        val label = LabelNode()

        instructions.add(JumpInsnNode(Opcodes.IFEQ, label))
        instructions.add(InsnNode(Opcodes.RETURN))
        instructions.add(label)

        node.instructions.insert(instructions)
    }

    @Injection(name = "drawNameplate", descriptor = "(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;FFFIFFZZ)V")
    fun drawNameplate(node: MethodNode) {
        val instructions = InsnList()

        instructions.add(invokeStatic(EntityRendererHook::class.java, "shouldRenderTag", "()Z"))

        val label = LabelNode()
        instructions.add(JumpInsnNode(Opcodes.IFEQ, label))
        instructions.add(InsnNode(Opcodes.RETURN))
        instructions.add(label)

        node.instructions.insert(instructions)
    }
}