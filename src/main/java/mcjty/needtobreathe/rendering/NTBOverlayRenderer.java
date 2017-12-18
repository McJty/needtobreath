package mcjty.needtobreathe.rendering;

import mcjty.needtobreathe.NeedToBreathe;
import mcjty.needtobreathe.items.InformationGlasses;
import mcjty.needtobreathe.items.ProtectiveHelmet;
import mcjty.needtobreathe.network.NTBMessages;
import mcjty.needtobreathe.network.PacketRequestPoisonFromServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class NTBOverlayRenderer {

    private static Map<Long, Byte> cleanAir;

    public static void setCleanAir(Map<Long, Byte> cleanAir) {
        NTBOverlayRenderer.cleanAir = cleanAir;
    }


    private static int prevCnt = -1;

    public static void onRenderWorld(RenderWorldLastEvent event) {
//        EntityPlayerSP player = Minecraft.getMinecraft().player;
//        if (player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
//            return;
//        }

//        if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() != ModItems.radiationMonitorItem) {
//            return;
//        }


        if (cleanAir != null) {
            EntityPlayerSP p = Minecraft.getMinecraft().player;
            if (hasGlasses()) {
                int cnt = cleanAir.size();
                if (cnt != prevCnt) {
                    System.out.println("cleanAir = " + cnt);
                    prevCnt = cnt;
                }
                renderHighlightedBlocks(event, p, cleanAir);
            }
        }
    }

    private static boolean hasGlasses() {
        EntityPlayerSP p = Minecraft.getMinecraft().player;
        ItemStack helmet = p.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return !helmet.isEmpty() && helmet.getItem() instanceof InformationGlasses;
    }

    private static boolean hasHelmet() {
        EntityPlayerSP p = Minecraft.getMinecraft().player;
        ItemStack helmet = p.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return !helmet.isEmpty() && helmet.getItem() instanceof ProtectiveHelmet;
    }

    // Poison from server
    private static int[] poison = new int[10];
    private static int poisonIdx = 0;
    private static int poisonTicks = 5;

    static {
        for (int i = 0 ; i < poison.length ; i++) {
            poison[i] = 0;
        }
    }

    public static void setPoison(int p) {
        poison[poisonIdx] = p;
        poisonIdx = (poisonIdx+1)%10;
    }

    public static void onRenderGame(RenderGameOverlayEvent event) {
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        if (!(hasGlasses() || hasHelmet())) {
            return;
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        poisonTicks--;
        if (poisonTicks <= 0) {
            poisonTicks = 9;   // Not in sync with the world poison ticker to get more accurate averages
            BlockPos pos = Minecraft.getMinecraft().player.getPosition().up();
            NTBMessages.INSTANCE.sendToServer(new PacketRequestPoisonFromServer(pos));
        }

        int p = 0;
        int maxp = 0;
        for (int pois : poison) {
            p += pois;
            if (pois > maxp) {
                maxp = pois;
            }
        }
        p = p / poison.length;
        int x = 200;
        x = fontRenderer.drawString("Poison ", x, 10, 0xffffffff);

        if (p > 0) {
            x = fontRenderer.drawString("avg ", x, 10, 0xffffffff);
            x = fontRenderer.drawString("" + (p * 100 / 255) + "%", x, 10, 0xffff0000);
            x = fontRenderer.drawString("  max ", x, 10, 0xffffffff);
            x = fontRenderer.drawString("" + (maxp * 100 / 255) + "%", x, 10, 0xffff0000);
        } else {
            x = fontRenderer.drawString("NONE", x, 10, 0xff00ff00);
        }
    }



    public static final ResourceLocation BLUEGLOW = new ResourceLocation(NeedToBreathe.MODID, "textures/effects/blueglow.png");

    private static void renderHighlightedBlocks(RenderWorldLastEvent evt, EntityPlayerSP p, Map<Long, Byte> cleanAir) {
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.getPartialTicks();
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Minecraft.getMinecraft().getTextureManager().bindTexture(BLUEGLOW);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (Map.Entry<Long, Byte> entry : cleanAir.entrySet()) {
            int value = entry.getValue() & 0xff;
            BlockPos coordinate = BlockPos.fromLong(entry.getKey());
            float x = coordinate.getX();
            float y = coordinate.getY();
            float z = coordinate.getZ();
            buffer.setTranslation(buffer.xOffset + x, buffer.yOffset + y, buffer.zOffset + z);

            int alpha = value;
            float mult = 0.4f;          // 1.1f
            if (alpha < 25) {
                mult = 0.025f;
            } else if (alpha < 50) {
                mult = 0.05f;
            } else if (alpha < 80) {
                mult = 0.1f;
            } else if (alpha < 110) {
                mult = 0.15f;
            } else if (alpha < 130) {
                mult = 0.2f;
            } else if (alpha < 150) {
                mult = 0.25f;
            } else if (alpha < 180) {
                mult = 0.3f;
            } else if (alpha < 210) {
                mult = 0.35f;
            }

            float offset = 0.5f - (mult/2);

            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.UP.ordinal(), mult, offset, alpha);
            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.DOWN.ordinal(), mult, offset, alpha);
            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.NORTH.ordinal(), mult, offset, alpha);
            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.SOUTH.ordinal(), mult, offset, alpha);
            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.WEST.ordinal(), mult, offset, alpha);
            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.EAST.ordinal(), mult, offset, alpha);
            buffer.setTranslation(buffer.xOffset - x, buffer.yOffset - y, buffer.zOffset - z);
        }
        tessellator.draw();

        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }


}
