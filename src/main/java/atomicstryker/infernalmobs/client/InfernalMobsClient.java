package atomicstryker.infernalmobs.client;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import atomicstryker.infernalmobs.common.ISidedProxy;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.MM_Gravity;
import atomicstryker.infernalmobs.common.network.HealthPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.client.LOTRClientProxy;
import lotr.client.LOTRTickHandlerClient;
import lotr.common.LOTRConfig;
import lotr.common.world.LOTRWorldProvider;

public class InfernalMobsClient implements ISidedProxy
{
    private final double NAME_VISION_DISTANCE = 32D;
    private Minecraft mc;
    //private World lastWorld;
    private long nextPacketTime;
    
    //@SideOnly(Side.CLIENT)
    private Map<EntityLivingBase, MobModifier> rareMobsClient;
    private Map<EntityLivingBase, MobModifier> rareMobsServer;
    private int airOverrideValue = -999;
    private boolean keepAir;
    
    private long healthBarRetainTime;
    private EntityLivingBase retainedTarget;
    
    @Override
    public void preInit()
    {
        FMLCommonHandler.instance().bus().register(this);
        mc = FMLClientHandler.instance().getClient();
    }

    @Override
    public void load()
    {
        nextPacketTime = 0;
        rareMobsClient = new HashMap<EntityLivingBase, MobModifier>();
        rareMobsServer = new HashMap<EntityLivingBase, MobModifier>();
        
        MinecraftForge.EVENT_BUS.register(new RendererBossGlow());
        MinecraftForge.EVENT_BUS.register(this);
        
        healthBarRetainTime = 0;
        retainedTarget = null;
    }
	
	@SubscribeEvent
    public void onClientDisconnectedToServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		airOverrideValue = -999; // reset for next world/server
		InfernalMobsCore.proxy.getRareMobs(true).clear(); // dont keep outdated data when we are back in main menu
	}
    
   
    /*private void askServerHealth(Entity ent)
    {
        if (System.currentTimeMillis() > nextPacketTime)
        {
            InfernalMobsCore.instance().networkHelper.sendPacketToServer(new HealthPacket(mc.thePlayer.getGameProfile().getName(),ent.getEntityId(), 0f, 0f));
            nextPacketTime = System.currentTimeMillis() + 100l;
        }
    }*/
    
    @SubscribeEvent
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
    	if (event.type == RenderGameOverlayEvent.ElementType.AIR)
        {
    		renderAirOverride(event);
        }
    	else if(event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH) {
    		renderBossBar(event);
    	}
    }
    
    @SubscribeEvent
    public void onClientTIck(ClientTickEvent event) {
    	if(event.phase == Phase.END && airOverrideValue != -999 && !keepAir) {
    		airOverrideValue--;
    	}
    }
    
    static Field field_alignmentYCurrent;
    static Field field_alignmentYBase;

    public void renderBossBar(RenderGameOverlayEvent.Pre event)
    {
    	// abort if disabled, or if boss is active
        if (InfernalMobsCore.instance().getIsHealthBarDisabled())     
        {
            return;
        }
        
        // abort if Tablist is shown
        if(mc.gameSettings.keyBindPlayerList.getIsKeyPressed()) {
        	if(!mc.isIntegratedServerRunning() || mc.thePlayer.sendQueue.playerInfoList.size() > 1 || mc.theWorld.getScoreboard().func_96539_a(0) != null) {
        		return;
        	}
        }

        Entity ent = getEntityCrosshairOver(event.partialTicks, mc);
        boolean retained = false;
        
        if (ent == null && System.currentTimeMillis() < healthBarRetainTime)
        {
            ent = retainedTarget;
            retained = true;
        }

        if (ent != null && ent instanceof EntityLivingBase)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) ent, true);
            if (mod != null)
            {
            	int y = 6;
            	
            	if(BossStatus.bossName != null && BossStatus.statusBarTime > 0)
            		y += 20;
            	
            	// out of area
            	if(InfernalMobsCore.lotr) {
            		if(mc.theWorld.provider instanceof LOTRWorldProvider || LOTRConfig.alwaysShowAlignment) {
            			if(field_alignmentYCurrent == null) {
            				//field_alignmentYBase = ReflectionHelper.findField(LOTRTickHandlerClient.class, "alignmentYBase");
            				field_alignmentYCurrent = ReflectionHelper.findField(LOTRTickHandlerClient.class, "alignmentYCurrent");
            			}
            			
            			try {
            				//int base = field_alignmentYBase.getInt(LOTRClientProxy.tickHandler);
            				int current = field_alignmentYCurrent.getInt(LOTRClientProxy.tickHandler);
            				if(current > 0) {
            					y += 38;
            				}
            			}
            			catch(Exception ec) {
            				
            			}
            		}
            		
            		if(LOTRTickHandlerClient.watchedInvasion.isActive()) {
            			y += 20;
            		}
            	}

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(Gui.icons);
                GL11.glDisable(GL11.GL_BLEND);

                EntityLivingBase target = (EntityLivingBase) ent;
                
                mod.setActualHealth(target.getHealth(), target.getMaxHealth());
                
                String buffer = mod.getEntityDisplayName(target);

                ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
                int screenwidth = resolution.getScaledWidth();
                FontRenderer fontR = mc.fontRenderer;

                GuiIngame gui = mc.ingameGUI;
                short lifeBarLength = 182;
                int x = screenwidth / 2 - lifeBarLength / 2;

                int lifeBarLeft = (int) ((float) mod.getActualHealth(target) / (float) mod.getActualMaxHealth(target) * (float) (lifeBarLength + 1));
                
                gui.drawTexturedModalRect(x, y, 0, 64, lifeBarLength, 5);
                gui.drawTexturedModalRect(x, y, 0, 64, lifeBarLength, 5);

                if (lifeBarLeft > 0)
                {
                    gui.drawTexturedModalRect(x, y, 0, 69, lifeBarLeft, 5);
                }

                y += 6;
                fontR.drawStringWithShadow(buffer, screenwidth / 2 - fontR.getStringWidth(buffer) / 2, y, 0x9c6717);

                String[] display = mod.getDisplayNames();
                int i = 0;
                while (i < display.length && display[i] != null)
                {
                    y += 10;
                    fontR.drawStringWithShadow(display[i], screenwidth / 2 - fontR.getStringWidth(display[i]) / 2, y, 0xffffff);
                    i++;
                }

                GL11.glColor4f(1.0F, 0.3F, 0.0F, 1.0F); // change color to green
                this.mc.getTextureManager().bindTexture(Gui.icons);
                
                if (!retained)
                {
                    retainedTarget = target;
                    healthBarRetainTime = System.currentTimeMillis() + 3000l;
                }
                // reset color
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                
            }
        }
    }

    private Entity getEntityCrosshairOver(float renderTick, Minecraft mc)
    {
        Entity returnedEntity = null;

        if (mc.renderViewEntity != null)
        {
            if (mc.theWorld != null)
            {
                double reachDistance = NAME_VISION_DISTANCE;
                final MovingObjectPosition mopos = mc.renderViewEntity.rayTrace(reachDistance, renderTick);
                double reachDist2 = reachDistance;
                final Vec3 viewEntPositionVec = mc.renderViewEntity.getPosition(renderTick);

                if (mopos != null)
                {
                    reachDist2 = mopos.hitVec.distanceTo(viewEntPositionVec);
                }

                final Vec3 viewEntityLookVec = mc.renderViewEntity.getLook(renderTick);
                final Vec3 actualReachVector =
                        viewEntPositionVec.addVector(viewEntityLookVec.xCoord * reachDistance, viewEntityLookVec.yCoord * reachDistance,
                                viewEntityLookVec.zCoord * reachDistance);
                float expandBBvalue = 1.0F;
                double lowestDistance = reachDist2;
                Entity iterEnt;
                Entity pointedEntity = null;
                for (Object obj : mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                        mc.renderViewEntity,
                        mc.renderViewEntity.boundingBox.addCoord(viewEntityLookVec.xCoord * reachDistance, viewEntityLookVec.yCoord * reachDistance,
                                viewEntityLookVec.zCoord * reachDistance).expand((double) expandBBvalue, (double) expandBBvalue,
                                (double) expandBBvalue)))
                {
                    iterEnt = (Entity) obj;
                    if (iterEnt.canBeCollidedWith())
                    {
                        float entBorderSize = iterEnt.getCollisionBorderSize();
                        AxisAlignedBB entHitBox = iterEnt.boundingBox.expand((double) entBorderSize, (double) entBorderSize, (double) entBorderSize);
                        MovingObjectPosition interceptObjectPosition = entHitBox.calculateIntercept(viewEntPositionVec, actualReachVector);

                        if (entHitBox.isVecInside(viewEntPositionVec))
                        {
                            if (0.0D < lowestDistance || lowestDistance == 0.0D)
                            {
                                pointedEntity = iterEnt;
                                lowestDistance = 0.0D;
                            }
                        }
                        else if (interceptObjectPosition != null)
                        {
                            double distanceToEnt = viewEntPositionVec.distanceTo(interceptObjectPosition.hitVec);

                            if (distanceToEnt < lowestDistance || lowestDistance == 0.0D)
                            {
                                pointedEntity = iterEnt;
                                lowestDistance = distanceToEnt;
                            }
                        }
                    }
                }

                if (pointedEntity != null && (lowestDistance < reachDist2 || mopos == null))
                {
                    returnedEntity = pointedEntity;
                }
            }
        }

        return returnedEntity;
    }
    
    /*@SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent tick)
    {
        if (mc.theWorld == null || (mc.currentScreen != null && mc.currentScreen.doesGuiPauseGame()))
            return;

        // client reset in case of swapping worlds
        if (mc.theWorld != lastWorld)
        {
            boolean newGame = lastWorld == null;
            lastWorld = mc.theWorld;

            if (!newGame)
            {
            	//rareMobsClient.clear();
            }
        }
    }*/

    @Override
    public Map<EntityLivingBase, MobModifier> getRareMobs(boolean client)
    {
    	return client ? rareMobsClient : rareMobsServer;
    }

    @Override
    public void onHealthPacketForClient(String stringData, int entID, float health, float maxhealth)
    {
        Entity ent = FMLClientHandler.instance().getClient().theWorld.getEntityByID(entID);
        if (ent != null && ent instanceof EntityLivingBase)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) ent, true);
            if (mod != null)
            {
                //System.out.printf("health packet [%f of %f] for %s\n", health, maxhealth, ent);
                mod.setActualHealth(health, maxhealth);
            }
        }
    }

    @Override
    public void onKnockBackPacket(float xv, float zv)
    {
        MM_Gravity.knockBack(FMLClientHandler.instance().getClient().thePlayer, xv, zv);
    }

    @Override
    public void onMobModsPacketToClient(String stringData, int entID)
    {
        InfernalMobsCore.instance().addRemoteEntityModifiers(FMLClientHandler.instance().getClient().theWorld, entID, stringData);
        
    }

    @Override
    public void onVelocityPacket(float xv, float yv, float zv)
    {
        FMLClientHandler.instance().getClient().thePlayer.addVelocity(xv, yv, zv);
    }

    @Override
    public void onAirPacket(int air, boolean keep)
    {
        airOverrideValue = air;
        keepAir = keep;
    }
    
    public void renderAirOverride(RenderGameOverlayEvent.Pre event)
    {
        if (airOverrideValue != -999 && !mc.thePlayer.isInsideOfMaterial(Material.water))
        {
        	
            GL11.glEnable(GL11.GL_BLEND);
            final int left = event.resolution.getScaledWidth() / 2 + 91;
            final int top = event.resolution.getScaledHeight() - GuiIngameForge.right_height;
            final int full = MathHelper.ceiling_double_int((airOverrideValue - 2) * 10.0D / 300.0D);
            final int partial = MathHelper.ceiling_double_int(airOverrideValue * 10.0D / 300.0D) - full;

            for (int i = 0; i < full + partial; ++i)
            {
                mc.ingameGUI.drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
            }
            GuiIngameForge.right_height += 10;
            GL11.glDisable(GL11.GL_BLEND);
            event.setCanceled(true);
        }
        
    }
}
