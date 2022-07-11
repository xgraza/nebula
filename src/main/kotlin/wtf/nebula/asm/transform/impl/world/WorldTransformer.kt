package wtf.nebula.asm.transform.impl.world

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import wtf.nebula.asm.hooks.WorldHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.world.World")
class WorldTransformer : ClassTransformer() {
    @Injection(name = "onEntityAdded", descriptor = "(Lnet/minecraft/entity/Entity)V")
    fun onEntityAdded(node: MethodNode) {
        val instructions = InsnList()
        instructions.add(VarInsnNode(Opcodes.AALOAD, 1))
        instructions.add(invokeStatic(WorldHook::class.java, "addedEntityEvent", "(Lnet/minecraft/entity/Entity)V"))
        node.instructions.insert(instructions)
    }

    @Injection(name = "removeEntity", descriptor = "(Lnet/minecraft/entity/Entity)V")
    fun onEntityRemoved(node: MethodNode) {
        val instructions = InsnList()
        instructions.add(VarInsnNode(Opcodes.AALOAD, 1))
        instructions.add(invokeStatic(WorldHook::class.java, "removeEntityEvent", "(Lnet/minecraft/entity/Entity)V"))
        node.instructions.insert(instructions)
    }
}