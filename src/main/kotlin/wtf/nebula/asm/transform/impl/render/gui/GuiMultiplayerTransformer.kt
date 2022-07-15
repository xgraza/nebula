package wtf.nebula.asm.transform.impl.render.gui

import net.minecraft.client.gui.GuiButton
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection

@ClassInjection("net.minecraft.client.gui.GuiMultiplayer")
class GuiMultiplayerTransformer : ClassTransformer() {
    @Injection("createButtons")
    fun createButtons(node: MethodNode) {
        val instructions = InsnList()



        node.instructions.insert(instructions)
    }

    private fun createGuiButton(): GuiButton {
        return GuiButton(69420, 15, 5, 75, 20, "Alts")
    }
}