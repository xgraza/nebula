package cope.nebula.client.feature.module.render.hud.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.client.feature.module.render.HUD;
import cope.nebula.client.feature.module.render.hud.IHUDComponent;
import cope.nebula.util.renderer.FontUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.world.biome.BiomeHell;

public class Coordinates implements IHUDComponent {
    @Override
    public void render(ScaledResolution resolution) {
        int posY = resolution.getScaledHeight() - FontUtil.getHeight() - 1;

        if (mc.currentScreen instanceof GuiChat) {
            posY -= 13;
        }

        int x = (int) mc.player.posX;
        int y = (int) mc.player.posY;
        int z = (int) mc.player.posZ;

        FontUtil.drawString(ChatFormatting.GRAY + "XYZ: " + ChatFormatting.RESET + x + ", " + y + ", " + z, 2.0f, posY, -1);

        if (HUD.netherCoordinates.getValue()) {
            boolean inNether = mc.world.getBiome(mc.player.getPosition()) instanceof BiomeHell;

            x = inNether ? x * 8 : x / 8;
            z = inNether ? z * 8 : z / 8;

            posY -= (FontUtil.getHeight() + 2);

            FontUtil.drawString(
                    (inNether ? ChatFormatting.BLUE : ChatFormatting.RED) +
                            "XYZ: " + ChatFormatting.RESET + x + ", " + y + ", " + z,
                    2.0f, posY, -1);
        }

        if (HUD.direction.getValue()) {
            EnumFacing facing = mc.player.getHorizontalFacing();

            posY -= (FontUtil.getHeight() + 2);

            FontUtil.drawString(
                    ChatFormatting.GRAY +
                            String.valueOf(facing.name().charAt(0)) +
                            (facing.getAxisDirection().equals(AxisDirection.NEGATIVE) ? "-" : "+"),
                    2.0f,
                    posY, -1
            );
        }
    }

    @Override
    public String getName() {
        return "Coordinates";
    }

    @Override
    public boolean isAlwaysActive() {
        return false;
    }
}
