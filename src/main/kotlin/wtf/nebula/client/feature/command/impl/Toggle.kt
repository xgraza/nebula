package wtf.nebula.client.feature.command.impl

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import wtf.nebula.client.feature.command.Command
import wtf.nebula.client.registry.impl.CommandRegistry.Companion.INVALID_USAGE
import wtf.nebula.client.registry.impl.ModuleRegistry

class Toggle : Command(arrayOf("toggle", "t", "on", "off"), "[module]", "Toggles a module") {
    override fun build(builder: LiteralArgumentBuilder<Any>) {
        builder.then(
            RequiredArgumentBuilder.argument<Any, String>("module", StringArgumentType.string()).executes {
                val moduleName = StringArgumentType.getString(it, "module")
                if (moduleName.isNullOrEmpty()) {
                    return@executes send("Invalid module name")
                }

                val module = ModuleRegistry.INSTANCE!!.registers.find {
                        mod -> mod.name.equals(moduleName, ignoreCase = true)
                } ?: return@executes send("Invalid module name")

                module.toggled = !module.toggled
                return@executes SINGLE_SUCCESS
            }
        ).executes {
            return@executes INVALID_USAGE
        }
    }
}