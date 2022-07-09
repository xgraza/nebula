package wtf.nebula.asm.transform.impl

import jdk.internal.org.objectweb.asm.Type
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import wtf.nebula.asm.hooks.MinecraftHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.client.Minecraft")
class MinecraftTransformer : ClassTransformer() {
    @Injection(name = "runTick")
    fun runTick(node: MethodNode) {
        val instructions = InsnList()

        // invoke MinecraftHook#runTick()V
        instructions.add(MethodInsnNode(
            INVOKESTATIC,
            Type.getInternalName(MinecraftHook::class.java),
            "runTick",
            "()V",
            false
        ))

        // add instructions to the method node
        node.instructions.insert(instructions)
    }
}