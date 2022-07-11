package wtf.nebula.asm.transform.impl.entity.player

import jdk.internal.org.objectweb.asm.Type
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import wtf.nebula.asm.hooks.EntityPlayerSPHook
import wtf.nebula.asm.transform.api.ClassInjection
import wtf.nebula.asm.transform.api.ClassTransformer
import wtf.nebula.asm.transform.api.Injection
import wtf.nebula.client.NebulaCompatibility
import wtf.nebula.client.event.player.motion.MotionEvent

@ClassInjection("net.minecraft.client.entity.EntityPlayerSP")
class EntityPlayerSPTransformer : ClassTransformer() {

    @Injection(name = "move", descriptor = "(Lnet/minecraft/entity/MoverType;DDD)V")
    fun move(node: MethodNode) {
        val instructions = InsnList()

        instructions.add(TypeInsnNode(NEW, Type.getInternalName(MotionEvent::class.java)))
        instructions.add(InsnNode(DUP))

        // load x, y, z into MotionEvent
        instructions.add(VarInsnNode(DLOAD, 2))
        instructions.add(VarInsnNode(DLOAD, 4))
        instructions.add(VarInsnNode(DLOAD, 6))

        // invoke the constructor
        instructions.add(MethodInsnNode(INVOKESPECIAL, Type.getInternalName(MotionEvent::class.java), "<init>", "(DDD)V", false))
        instructions.add(VarInsnNode(ASTORE, 11))

        // invoke NebulaCompatibility#post (we love kotlin)
        instructions.add(VarInsnNode(ALOAD, 11))
        instructions.add(invokeStatic(NebulaCompatibility::class.java, "post", "(Lme/bush/eventbuskotlin/Event;)Z"))
        // instructions.add(VarInsnNode(ASTORE, 3))
        instructions.add(InsnNode(POP))

        // load our event into move(L...MoverType;DDD)V
        instructions.add(VarInsnNode(ALOAD, 11))
        instructions.add(MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(MotionEvent::class.java), "getX", "()D", false))
        instructions.add(VarInsnNode(DSTORE, 2))

        instructions.add(VarInsnNode(ALOAD, 11))
        instructions.add(MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(MotionEvent::class.java), "getY", "()D", false))
        instructions.add(VarInsnNode(DSTORE, 4))

        instructions.add(VarInsnNode(ALOAD, 11))
        instructions.add(MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(MotionEvent::class.java), "getZ", "()D", false))
        instructions.add(VarInsnNode(DSTORE, 6))

        // if the result of post is true, we'll return
        instructions.add(VarInsnNode(ALOAD, 11))
        instructions.add(MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(MotionEvent::class.java), "getCancelled", "()Z", false))

        val label = LabelNode()
        instructions.add(JumpInsnNode(IFEQ, label))
        instructions.add(InsnNode(RETURN))
        instructions.add(label)

        node.instructions.insert(instructions)
    }

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

        preInstructions.add(JumpInsnNode(IFEQ, label))
        preInstructions.add(InsnNode(RETURN))
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