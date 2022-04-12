package cope.nebula.client.feature.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.asm.duck.IEntityPlayer;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.renderer.FontUtil;
import cope.nebula.util.renderer.RenderUtil;
import cope.nebula.util.world.entity.EntityUtil;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Nametags extends Module {
    public static Nametags INSTANCE;

    public Nametags() {
        super("Nametags", ModuleCategory.RENDER, "Renders nametags above players heads");
        INSTANCE = this;
    }

    public static final Value<Boolean> held = new Value<>("Held", false);

    public static final Value<Boolean> armor = new Value<>("Armor", true);
    public static final Value<Boolean> reversed = new Value<>(armor, "Reversed", true);
    public static final Value<Boolean> enchants = new Value<>(armor, "Enchants", true);
    public static final Value<Boolean> percent = new Value<>(armor, "Percent", true);

    public static final Value<Boolean> health = new Value<>("Health", true);
    public static final Value<Boolean> ping = new Value<>("Ping", true);
    public static final Value<Boolean> pops = new Value<>("Pops", true);

    public static final Value<Double> scaling = new Value<>("Scaling", 2.0, 1.0, 5.0);

    @Override
    public void onRender3d() {
        if (mc.getRenderViewEntity() != null) {
            for (EntityPlayer player : mc.world.playerEntities) {
                if (player.equals(mc.player) || player.isDead) {
                    continue;
                }

                RenderManager renderManager = mc.getRenderManager();

                double x = AngleUtil.interpolate(player.posX, player.prevPosX) - renderManager.viewerPosX;
                double y = AngleUtil.interpolate(player.posY, player.prevPosY) - renderManager.viewerPosY;
                double z = AngleUtil.interpolate(player.posZ, player.prevPosZ) - renderManager.viewerPosZ;

                renderNametag(player, x, y, z);
            }
        }
    }

    private void renderNametag(EntityPlayer player, double x, double y, double z) {
        double offset = player.isSneaking() ? 0.5 : 0.7;

        RenderManager renderManager = mc.getRenderManager();

        double distance = mc.getRenderViewEntity().getDistance(x + renderManager.viewerPosX, y + renderManager.viewerPosY, z + renderManager.viewerPosZ);
        double scale = Math.max((scaling.getValue() / 10.0) * 4.0, (scaling.getValue() / 10.0) * distance) / 50.0;

        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();

        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);

        GlStateManager.disableLighting();
        GlStateManager.translate(x, 1.4 + y + offset, z);

        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(renderManager.playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);

        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        String info = getNametagInfo(player);
        float width = FontUtil.getWidth(info) / 2.0f;

        RenderUtil.drawRectangle(-(width - 4.0), -(FontUtil.getHeight() - 1.0f), (width * 2.0f) + 4.0f, FontUtil.getHeight() + 2.0f, 0x70000000);
        FontUtil.drawString(info, -(width - 6.0f), -(FontUtil.getHeight() - 2.0f), -1);

        int xOffset = -24 / 2 * player.inventory.armorInventory.size();

        if (held.getValue()) {
            ItemStack stack = player.getHeldItemMainhand();
            if (!stack.isEmpty()) {
                renderItem(stack, xOffset);
            }

            xOffset += 16;
        }

        if (armor.getValue()) {
            List<ItemStack> items = new ArrayList<>(player.inventory.armorInventory);
            if (reversed.getValue()) {
                Collections.reverse(items);
            }

            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    renderItem(stack, xOffset);
                    xOffset += 16;
                }
            }
        }

        if (held.getValue()) {
            ItemStack stack = player.getHeldItemOffhand();
            if (!stack.isEmpty()) {
                renderItem(stack, xOffset);
                xOffset += 16;
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();

        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.disablePolygonOffset();

        GlStateManager.popMatrix();
    }

    private void renderItem(ItemStack stack, int x) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        RenderHelper.enableStandardItemLighting();
        mc.getRenderItem().zLevel = -150.0f;
        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.disableCull();

        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, -26);
        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, -26);

        mc.getRenderItem().zLevel = 0.0f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private String getNametagInfo(EntityPlayer player) {
        StringBuilder builder = new StringBuilder();
        if (((IEntityPlayer) player).isFriend()) {
            builder.append(ChatFormatting.AQUA);
        } else if (player.isSneaking()) {
            builder.append(ChatFormatting.GOLD);
        }

        builder.append(player.getName());

        if (health.getValue()) {
            builder.append(" ");

            float health = EntityUtil.getHealth(player);

            if (health >= 20.0f) {
                builder.append(ChatFormatting.GREEN);
            } else if (health < 15.0f) {
                builder.append(ChatFormatting.YELLOW);
            } else {
                builder.append(ChatFormatting.RED);
            }

            builder.append(String.format("%.2f", health)).append(ChatFormatting.RESET);
        }

        if (ping.getValue()) {
            builder.append(" ")
                    .append(getNebula().getServerManager().getLatency(player.getUniqueID()))
                    .append("ms");
        }

        if (pops.getValue()) {
            // TODO
        }

        return builder.toString();
    }
}
