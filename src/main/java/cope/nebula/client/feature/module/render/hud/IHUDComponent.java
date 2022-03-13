package cope.nebula.client.feature.module.render.hud;

import cope.nebula.util.Globals;
import net.minecraft.client.gui.ScaledResolution;

/**
 * Represents a hud component
 *
 * @author aesthetical
 * @since 3/12/22
 */
public interface IHUDComponent extends Globals {
    void render(ScaledResolution resolution);

    String getName();

    boolean isAlwaysActive();
}
