package wtf.nebula.client.feature.module.render

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.event.render.HurtCameraRenderEvent
import wtf.nebula.client.event.render.RenderItemActivationEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class NoRender : Module(ModuleCategory.RENDER, "stops things from rendering") {
    val hurtCam by bool("Hurt Camera", true)
    val totems by bool("Totem Pop", true)
    val blocks by bool("Inside Block", true)
    val dynamicFov by bool("Dynamic FoV", true)

    @EventListener
    private val hurtCameraRenderListener = listener<HurtCameraRenderEvent> {
        it.cancelled = hurtCam
    }

    @EventListener
    private val renderItemActivationListener = listener<RenderItemActivationEvent> {
        it.cancelled = totems
    }
}