package wtf.nebula.asm.transform.impl.render.gui

import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import wtf.nebula.asm.hooks.GuiIngameHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.client.gui.GuiIngame")
class GuiIngameTransformer : ClassTransformer() {
    @Injection(name = "renderGameOverlay", descriptor = "(F)V")
    fun renderGameOverlay(node: MethodNode) {
        val instructions = InsnList()
        instructions.add(invokeStatic(GuiIngameHook::class.java, "renderHUD", "()V"))
        node.instructions.insertBefore(node.instructions.get(node.instructions.size() - 2), instructions)
    }
}