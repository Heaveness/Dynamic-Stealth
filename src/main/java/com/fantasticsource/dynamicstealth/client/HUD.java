package com.fantasticsource.dynamicstealth.client;

import com.fantasticsource.dynamicstealth.common.DynamicStealth;
import com.fantasticsource.tools.PNG;
import com.fantasticsource.tools.datastructures.Color;
import com.fantasticsource.tools.datastructures.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.opengl.GL11;

import static com.fantasticsource.dynamicstealth.common.DynamicStealthConfig.clientSettings;
import static com.fantasticsource.dynamicstealth.common.HUDData.*;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class HUD extends Gui
{
    private static final ResourceLocation ICON_LOCATION = new ResourceLocation(DynamicStealth.MODID, "Indicator.png");

    public HUD(Minecraft mc)
    {
        ScaledResolution sr = new ScaledResolution(mc);
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();
        FontRenderer fontRender = mc.fontRenderer;

        drawDetailHUD(width, height, fontRender);

        GlStateManager.color(1, 1, 1, 1);
    }

    @SubscribeEvent
    public static void clearHUD(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        detailColor = COLOR_NULL;
        detailSearcher = EMPTY;
        detailTarget = EMPTY;
        detailPercent = 0;

        onPointDataMap.clear();
    }

    @SubscribeEvent
    public static void entityRender(RenderLivingEvent.Post event)
    {
        EntityLivingBase livingBase = event.getEntity();
        Pair<Integer, Integer> data = onPointDataMap.get(livingBase.getEntityId());

        if (data != null) drawOnPointHUDElement(event.getRenderer().getRenderManager(), event.getX(), event.getY(), event.getZ(), livingBase, data.getKey(), data.getValue());
    }

    private static void drawOnPointHUDElement(RenderManager renderManager, double x, double y, double z, Entity entity, int color, int percent)
    {
        float viewerYaw = renderManager.playerViewY;
        float viewerPitch = renderManager.playerViewX;

        Color c = new Color(color, true);
        GlStateManager.color(c.rf(), c.gf(), c.bf(), c.af());

        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.disableTexture2D();

        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y + entity.height / 2, z);
        GlStateManager.rotate(-viewerYaw, 0, 1, 0);
        GlStateManager.rotate((float) (renderManager.options.thirdPersonView == 2 ? -1 : 1) * viewerPitch, 1, 0, 0);
        GlStateManager.translate(entity.width * 1.415, 0, 0);
        GlStateManager.scale(-0.025, -0.025, 0.025);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL_QUADS, POSITION);
        bufferbuilder.pos(-8, -4, 0).endVertex();
        bufferbuilder.pos(-8, 4, 0).endVertex();
        bufferbuilder.pos(0, 4, 0).endVertex();
        bufferbuilder.pos(0, -4, 0).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();

        GlStateManager.color(1, 1, 1, 1);
    }

    private void drawDetailHUD(int width, int height, FontRenderer fontRender)
    {
        if (clientSettings.threat.displayDetailHUD)
        {
            if (detailSearcher.equals(EMPTY))
            {
                drawString(fontRender, EMPTY, (int) (width * 0.75), height - 30, detailColor);
                drawString(fontRender, EMPTY, (int) (width * 0.75), height - 20, detailColor);
                drawString(fontRender, EMPTY, (int) (width * 0.75), height - 10, detailColor);
            }
            else
            {
                if (detailPercent == -1) //Special code for threat bypass mode
                {
                    drawString(fontRender, detailSearcher, (int) (width * 0.75), height - 30, COLOR_ALERT);
                    drawString(fontRender, UNKNOWN, (int) (width * 0.75), height - 20, COLOR_ALERT);
                    drawString(fontRender, UNKNOWN, (int) (width * 0.75), height - 10, COLOR_ALERT);
                }
                else if (detailPercent == 0)
                {
                    drawString(fontRender, detailSearcher, (int) (width * 0.75), height - 30, detailColor);
                    drawString(fontRender, EMPTY, (int) (width * 0.75), height - 20, detailColor);
                    drawString(fontRender, EMPTY, (int) (width * 0.75), height - 10, detailColor);
                }
                else if (detailTarget.equals(EMPTY))
                {
                    drawString(fontRender, detailSearcher, (int) (width * 0.75), height - 30, detailColor);
                    drawString(fontRender, EMPTY, (int) (width * 0.75), height - 20, detailColor);
                    drawString(fontRender, detailPercent + "%", (int) (width * 0.75), height - 10, detailColor);
                }
                else
                {
                    drawString(fontRender, detailSearcher, (int) (width * 0.75), height - 30, detailColor);
                    drawString(fontRender, detailTarget, (int) (width * 0.75), height - 20, detailColor);
                    drawString(fontRender, detailPercent + "%", (int) (width * 0.75), height - 10, detailColor);
                }
            }
        }
    }
}
