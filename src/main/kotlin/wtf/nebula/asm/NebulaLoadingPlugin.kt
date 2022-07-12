package wtf.nebula.asm

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name
import wtf.nebula.asm.transform.NebulaTransformer
import wtf.nebula.asm.transform.api.TransformerManager

@MCVersion("1.12.2")
@Name("NebulaLoadingPlugin")
class NebulaLoadingPlugin : IFMLLoadingPlugin {
    var transformerManager = TransformerManager.getInstance()

    override fun getASMTransformerClass(): Array<String> {
        return arrayOf(NebulaTransformer::class.java.name)
    }

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun injectData(data: MutableMap<String, Any>?) {
        if (data == null) {
            return
        }

        // check if notch obfuscation is being used (aka we're no longer in an IDE and a mod env)
        val notch = data.getOrDefault("runtimeDeobfuscationEnabled", true) as Boolean
        transformerManager.runtimeObfuscation = notch

        if (notch) {
            transformerManager.logger.info("Notch obfuscation detected")
        }
    }

    override fun getAccessTransformerClass(): String {
        return NebulaAccessTransformer::class.java.name
    }
}