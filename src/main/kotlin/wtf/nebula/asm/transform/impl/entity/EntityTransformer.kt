package wtf.nebula.asm.transform.impl.entity

import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import wtf.nebula.asm.hooks.EntityHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.entity.Entity")
class EntityTransformer : ClassTransformer() {

    @Injection(name = "move", descriptor = "(Lnet/minecraft/entity/MoverType;DDD)V")
    fun move(node: MethodNode) {
        var injectNode: MethodInsnNode? = null
        for (i in 0 until node.instructions.size()) {
            val instruction = node.instructions[i]
            if (instruction is MethodInsnNode && instruction.name == "endSection") { // TODO: remap
                injectNode = instruction
                break
            }
        }

        if (injectNode != null) {
            val instructions = InsnList()
            instructions.add(invokeStatic(EntityHook::class.java, "stepEvent", "()V"))
            node.instructions.insertBefore(injectNode, instructions)
        }
    }
}