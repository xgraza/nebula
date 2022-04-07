package cope.nebula.asm;

import cope.nebula.client.Nebula;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;

@Name("MixinLoadingPlugin")
@MCVersion("1.12.2")
public class MixinLoadingPlugin implements IFMLLoadingPlugin {
    public MixinLoadingPlugin() {
        MixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        // Mixins.addConfigurations("mixins.nebula.json", "mixins.baritone.json");
        Mixins.addConfiguration("mixins.nebula.json");

        Nebula.getLogger().info("Loaded mixin configs: " +
                Mixins.getConfigs().stream().map(Config::getName).collect(Collectors.joining(", ")));
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
