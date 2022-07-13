package wtf.nebula.asm.transform.impl.client.input

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import wtf.nebula.asm.hooks.KeyBindingHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.client.settings.KeyBinding")
class KeyBindingTransformer : ClassTransformer() {
    @Injection(name = "isKeyDown", descriptor = "()Z")
    fun isKeyDown(node: MethodNode) {
//        val instructions = InsnList()
//
//        instructions.add(VarInsnNode(ALOAD, 0))
//        instructions.add(invokeStatic(KeyBindingHook::class.java, "pressedHook", "(Lnet/minecraft/client/settings/KeyBinding;)Z"))
//        instructions.add(VarInsnNode(ASTORE, 1))
//
//        instructions.add(VarInsnNode(ALOAD, 1))
//
//        val label = LabelNode()
//        instructions.add(JumpInsnNode(IFEQ, label))
//        instructions.add(InsnNode(ARETURN))
//        instructions.add(label)
//
//        node.instructions.insert(instructions)
    }
}