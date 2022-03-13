package cope.nebula.client.feature.module.render;

import com.google.common.collect.Lists;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.feature.module.render.hud.IHUDComponent;
import cope.nebula.client.feature.module.render.hud.impl.Coordinates;
import cope.nebula.client.feature.module.render.hud.impl.Watermark;
import cope.nebula.client.value.Value;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;

public class HUD extends Module {
    private final ArrayList<IHUDComponent> components = Lists.newArrayList(
            new Coordinates(),
            new Watermark()
    );

    public HUD() {
        super("HUD", ModuleCategory.RENDER, "Renders an overlay on the clients HUD");
    }

    public static final Value<Boolean> coordinates = new Value<>("Coordinates", true);
    public static final Value<Boolean> direction = new Value<>(coordinates, "Direction", false);
    public static final Value<Boolean> netherCoordinates = new Value<>(coordinates, "Nether", true);

    @Override
    public void onRender2d() {
        GlStateManager.pushMatrix();

        components.forEach((component) -> {
            Value value = getValue(component.getName());
            if ((value != null && value.getValue() instanceof Boolean && (boolean) value.getValue()) || component.isAlwaysActive()) {
                component.render(new ScaledResolution(mc));
            }
        });

        GlStateManager.popMatrix();
    }
}
