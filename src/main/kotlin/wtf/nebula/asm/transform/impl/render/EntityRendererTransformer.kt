package wtf.nebula.asm.transform.impl.render

import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
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
}