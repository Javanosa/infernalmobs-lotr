package atomicstryker.infernalmobs.client;

import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockFire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.particle.EntitySpellParticleFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.MM_Arsonist;
import atomicstryker.infernalmobs.common.mods.MM_Bomber;
import atomicstryker.infernalmobs.common.mods.MM_Ghastly;
import atomicstryker.infernalmobs.common.mods.MM_Storm;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTREntityMoredain;

public class RendererBossGlow
{
    private static long lastRender = 0L;
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        renderBossGlow(event.partialTicks);
    }
    
    private static Frustrum frustrum;
    
    private void renderBossGlow(float renderTick) {
    	boolean glowThisTime = !Minecraft.getMinecraft().isGamePaused() && System.currentTimeMillis() > lastRender+10L;
    	Minecraft mc = Minecraft.getMinecraft();
    	double d0 = RenderManager.renderPosX;
	    double d1 = RenderManager.renderPosY;
	    double d2 = RenderManager.renderPosZ;
	    int particleMode = mc.gameSettings.particleSetting;
	    
	    boolean renderInit = false;
	    
	    if(frustrum == null)
        	frustrum = new Frustrum();
	    
    	if(glowThisTime) {
    		lastRender = System.currentTimeMillis();
	        
    		EntityLivingBase camera = mc.renderViewEntity;
	        
	        
	        
	        double cameraX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * renderTick;
            double cameraY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * renderTick;
            double cameraZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * renderTick;
	        
            frustrum.setPosition(cameraX, cameraY, cameraZ);
            //InfernalMobsCore.instance().checkExist(true);
    	}
        
        Map<EntityLivingBase, MobModifier> mobsmap = InfernalMobsCore.proxy.getRareMobs(true);
        
        for (EntityLivingBase ent : mobsmap.keySet()) {
        	
            if (ent.getDistanceSq(d0, d1, d2) < (32D * 32D)
            && (ent.ignoreFrustumCheck || frustrum.isBoundingBoxInFrustum(ent.boundingBox))
            && ent.isEntityAlive())  {
            	
            	if(glowThisTime) {
            		// if not 0 (all), then choose to lower count for 1 and even more for 2 (minimal)
            		Random rand = ent.getRNG();
	                if(particleMode != 0 && rand.nextInt(3 * particleMode) == 0)
	                	particleMode = 2;
	                
	                if(particleMode < 2) {
	                	
	                	EnityBossFX entitySpellParticleFX = new EnityBossFX(mc.theWorld, ent.posX + (rand.nextDouble() - 0.5D) * ent.width,
		                        ent.posY + rand.nextDouble() * ent.height - 0.1D,
		                        ent.posZ + (rand.nextDouble() - 0.5D) * ent.width,
		                        0D, 0D, 0D);
		            	
		            	

		            	// special rule for moredain because particles are hard to see here
		            	if(InfernalMobsCore.lotr && ent instanceof LOTREntityMoredain) {
		            		//233, 9, 46
		            		// 250, 5, 45
		            		entitySpellParticleFX.setRBGColorF(0.98f, 0.02f, 0.18f);
		            	}
		            	else {
		            		//233, 97, 9;
		            		entitySpellParticleFX.setRBGColorF(0.9f, 0.4f, 0f);
		            	}
		            	entitySpellParticleFX.motionX = ent.motionX;
		            	//entitySpellParticleFX.motionY = ent.motionY;
		            	entitySpellParticleFX.motionZ = ent.motionZ;
		                mc.effectRenderer.addEffect(entitySpellParticleFX);
		            }
	            }
            	
            	MobModifier mod = InfernalMobsCore.getMobModifiers(ent, true);
                if (mod != null && !ent.isInvisible())
                {
                	if(mod.containsModifierClass(MM_Ghastly.class) || mod.containsModifierClass(MM_Storm.class)
                		|| mod.containsModifierClass(MM_Arsonist.class) || mod.containsModifierClass(MM_Bomber.class)) {
                		if(!renderInit) {
                			GL11.glDisable(GL11.GL_LIGHTING);
                    		//GL11.glEnable(GL11.GL_BLEND); // Enable blending
                    		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // Set blend function
                    		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                    		renderInit = true;
                		}
                		
                		BlockFire fireBlock = InfernalMobsCore.lotr ? (BlockFire) LOTRMod.rhunFire : Blocks.fire;
                		
                        IIcon iicon = fireBlock.getFireIcon(0);
                        IIcon iicon1 = fireBlock.getFireIcon(1);
                        //ent.extinguish();
                        GL11.glPushMatrix();
                        
                        
                        float x = (float) (ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * renderTick - RenderManager.renderPosX) + 0.01f;
                		float y = (float) (ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * renderTick - RenderManager.renderPosY) + 0.01f;
                		float z = (float) (ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * renderTick - RenderManager.renderPosZ) + 0.01f;
                        
                        GL11.glTranslatef(x, y, z);
                        float f1 = ent.width * 1.4F;
                        GL11.glScalef(f1, f1, f1);
                        Tessellator tessellator = Tessellator.instance;
                        float f2 = 0.5F;
                        float f3 = 0.0F;
                        float f4 = ent.height / f1;
                        float f5 = (float)(ent.posY - ent.boundingBox.minY);
                        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
                        GL11.glTranslatef(0.0F, 0.0F, -0.3F + (float)((int)f4) * 0.02F);
                        if(InfernalMobsCore.lotr)
                        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
                        else
                        	GL11.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
                        	//GL11.glColor4f(0.0F, 0.8F, 1.0F, 1F); // light blueish tint
                        float f6 = 0.0F;
                        int i = 0;
                        
                        tessellator.startDrawingQuads();

                        while (f4 > 0.0F) {
                            IIcon iicon2 = i % 2 == 0 ? iicon : iicon1;
                            
                            float f7 = iicon2.getMinU();
                            float f8 = iicon2.getMinV();
                            float f9 = iicon2.getMaxU();
                            float f10 = iicon2.getMaxV();

                            if ((i / 2) % 2 == 0)
                            {
                                float f11 = f9;
                                f9 = f7;
                                f7 = f11;
                            }

                            tessellator.addVertexWithUV((double)(f2 - f3), (double)(0.0F - f5), (double)f6, (double)f9, (double)f10);
                            tessellator.addVertexWithUV((double)(-f2 - f3), (double)(0.0F - f5), (double)f6, (double)f7, (double)f10);
                            tessellator.addVertexWithUV((double)(-f2 - f3), (double)(1.4F - f5), (double)f6, (double)f7, (double)f8);
                            tessellator.addVertexWithUV((double)(f2 - f3), (double)(1.4F - f5), (double)f6, (double)f9, (double)f8);
                            f4 -= 0.45F;
                            f5 -= 0.45F;
                            f2 *= 0.9F;
                            f6 += 0.03F;
                            ++i;
                        }

                        tessellator.draw();
                        GL11.glPopMatrix();
                        
                        
                	}
                }
            }
        }
        
        if(renderInit) {
        	//GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }
}
