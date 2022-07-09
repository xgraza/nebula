package wtf.nebula.asm.transform

import net.minecraft.launchwrapper.IClassTransformer
import wtf.nebula.asm.transform.api.Injection
import wtf.nebula.asm.transform.api.TransformerManager
import wtf.nebula.asm.transform.api.util.ASMUtil

class NebulaTransformer : IClassTransformer {
    private val transformerManager = TransformerManager.getInstance()

    override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
        if (name == null) {
            return basicClass
        }

        // get our class transformer for the class, or return its default bytecode
        val classTransformer = transformerManager.findClassTransformer(name) ?: return basicClass

        // the class node from the class bytes
        val classNode = ASMUtil.toClassNode(basicClass!!)

        classTransformer.javaClass.methods.forEach {

            if (it.isAnnotationPresent(Injection::class.java)) {

                val inject = it.getAnnotation(Injection::class.java)

                val hookMethodName = transformerManager.getMethod(name, inject.name, inject.descriptor)
                if (hookMethodName != null) {

                }
            }
        }

        // return our basic class bytes if we do not transform this class
        return basicClass
    }
}