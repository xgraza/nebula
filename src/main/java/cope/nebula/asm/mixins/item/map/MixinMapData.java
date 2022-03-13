package cope.nebula.asm.mixins.item.map;

import cope.nebula.client.feature.module.render.AntiRender;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MapData.class)
public class MixinMapData {
    @Shadow public Map<String, MapDecoration> mapDecorations;

    @Inject(method = "updateDecorations", at = @At("HEAD"), cancellable = true)
    private void onUpdateDecorations(MapDecoration.Type type, World worldIn, String decorationName, double worldX, double worldZ, double rotationIn, CallbackInfo info) {
        if (AntiRender.INSTANCE.isOn() && AntiRender.maps.getValue()) {
            info.cancel();

            if (!mapDecorations.isEmpty()) {
                mapDecorations.clear();
            }
        }
    }
}
