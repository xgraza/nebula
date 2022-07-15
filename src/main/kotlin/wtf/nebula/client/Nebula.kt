package wtf.nebula.client

import me.bush.eventbuskotlin.Config
import me.bush.eventbuskotlin.EventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import org.apache.logging.log4j.LogManager
import org.lwjgl.opengl.Display
import wtf.nebula.client.event.translate.ForgeEventListener
import wtf.nebula.client.feature.alt.Alt
import wtf.nebula.client.feature.alt.AltType
import wtf.nebula.client.manager.InventoryManager
import wtf.nebula.client.manager.RotationManager
import wtf.nebula.client.manager.server.ServerManager
import wtf.nebula.client.manager.TotemPopManager
import wtf.nebula.client.registry.RegistryContainer
import wtf.nebula.client.registry.impl.CommandRegistry
import wtf.nebula.client.registry.impl.ModuleRegistry
import wtf.nebula.util.versioning.BuildConfig

@Mod(useMetadata = true, clientSideOnly = true, modid = Nebula.ID)
class Nebula {
    companion object {
        const val NAME = "Nebula"
        const val ID = BuildConfig.NAME
        const val VERSION = BuildConfig.VERSION
        const val HASH = BuildConfig.HASH

        const val FULL = "$NAME v$VERSION/git-$HASH"

        val logger = LogManager.getLogger(NAME)
        val BUS = EventBus(Config(logger, annotationRequired = true))

        val inventoryManager = InventoryManager()
        val rotationManager = RotationManager()
        val serverManager = ServerManager()
        val totemPopManager = TotemPopManager()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        logger.info("Loading $FULL")

        RegistryContainer.register(ModuleRegistry())
        RegistryContainer.register(CommandRegistry())

        BUS.subscribe(ForgeEventListener())
        BUS.subscribe(inventoryManager)
        BUS.subscribe(rotationManager)
        BUS.subscribe(serverManager)
        BUS.subscribe(totemPopManager)
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        logger.info("Loaded $NAME.")
        Display.setTitle(FULL)
    }
}