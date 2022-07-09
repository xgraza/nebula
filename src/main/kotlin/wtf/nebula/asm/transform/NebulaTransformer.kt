package wtf.nebula.asm.transform

import net.minecraft.launchwrapper.IClassTransformer
import org.apache.logging.log4j.LogManager
import wtf.nebula.asm.transform.api.Injection
import wtf.nebula.asm.transform.api.TransformerManager
import wtf.nebula.asm.transform.api.TransformerManager.Companion.CLASS_REGEX
import wtf.nebula.asm.transform.api.util.ASMUtil

class NebulaTransformer : IClassTransformer {
    private val transformerManager = TransformerManager.getInstance()
    private val logger = LogManager.getLogger("NebulaTransformer")

    override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
        if (name == null) {
            return basicClass
        }

        // get our class transformer for the class, or return its default bytecode
        val classTransformer = transformerManager.findClassTransformer(name) ?: return basicClass

        logger.info("Found class transformer ${classTransformer::class.java.simpleName} for class $name")

        // the class node from the class bytes
        val classNode = ASMUtil.toClassNode(basicClass!!) ?: return basicClass

        // this will not be the final code, this will never work when compiling into a forge mod cause of searge mappings...
        classTransformer.javaClass.methods.forEach {

            if (it.isAnnotationPresent(Injection::class.java)) {

                val inject = it.getAnnotation(Injection::class.java)

                classNode.methods.forEach { methodNode ->
                    var name = methodNode.name
                    var desc = methodNode.desc

                    if (transformerManager.runtimeObfuscation) {
                        val clazzMatcher = CLASS_REGEX.matcher(desc)
                        val groups = clazzMatcher.groupCount()

                        if (groups > 0) {
                            for (i in 0..groups) {
                                val group = clazzMatcher.group(i)
                            }
                        }
                    }

                    if (name == inject.name && desc == inject.descriptor) {
                        it.invoke(classTransformer, methodNode)
                    }
                }
            }
        }

        return ASMUtil.toTransformed(classNode)
    }
}