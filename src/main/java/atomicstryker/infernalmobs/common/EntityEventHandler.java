package atomicstryker.infernalmobs.common;

import java.util.HashMap;
import java.util.Map.Entry;

import atomicstryker.infernalmobs.common.network.MobModsPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.LOTRConfig;

public class EntityEventHandler
{

    private final boolean antiMobFarm;
    private final long mobFarmCheckIntervals;
    private final float mobFarmDamageTrigger;

    private final HashMap<ChunkCoordIntPair, Float> damageMap;
    private long nextMapEvaluation;

    /**
     * Links the Forge Event Handler to the registered Entity MobModifier Events
     * (if present) Also keeps track of the anti mobfarm mechanic if enabled
     * 
     * @param antiMobfarming
     *            enables or disables
     */
    public EntityEventHandler()
    {
        Configuration config = InfernalMobsCore.instance().config;

        config.load();
        antiMobFarm =
                config.get(Configuration.CATEGORY_GENERAL, "AntiMobfarmingEnabled", true,
                        "Anti Mob farming mechanic. Might cause overhead if enabled.").getBoolean(true);
        mobFarmCheckIntervals =
                config.get(Configuration.CATEGORY_GENERAL, "AntiMobFarmCheckInterval", 30,
                        "time in seconds between mob check intervals. Higher values cost more performance, but might be more accurate.").getInt() * 1000l;
        mobFarmDamageTrigger =
                (float) config.get(Configuration.CATEGORY_GENERAL, "mobFarmDamageThreshold", 150D,
                        "Damage in chunk per interval that triggers anti farm effects").getDouble(150D);
        config.save();

        damageMap = new HashMap<ChunkCoordIntPair, Float>();
        nextMapEvaluation = System.currentTimeMillis();
    }
    
    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
    	
    	
    	if (event.target instanceof EntityLivingBase)
        {
            EntityLivingBase e = (EntityLivingBase) event.target;
            MobModifier mod = InfernalMobsCore.getMobModifiers(e, false);
            
            if (mod != null)
            {
            	String stringData = mod.getLinkedModNameUntranslated();
                InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(new MobModsPacket(stringData, e.getEntityId(), (byte) 1), (EntityPlayerMP) event.entityPlayer);
                InfernalMobsCore.instance().sendHealthPacket(e, mod.getActualHealth(e));
                System.out.println("Add "+e.getCommandSenderName()+" with ID "+e.getEntityId());
            }
        }
    
    }
    
    @SubscribeEvent
    public void onStopTracking(PlayerEvent.StopTracking event) {
    	if (event.target instanceof EntityLivingBase)
        {
            EntityLivingBase e = (EntityLivingBase) event.target;
            MobModifier mod = InfernalMobsCore.getMobModifiers(e, false);
            
            if (mod != null)
            {
            	System.out.println("Stop "+event.target.getCommandSenderName()+" with ID "+event.target.getEntityId());
            }
        }
    	
    }

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event)
    {
        if (!event.world.isRemote && event.entity instanceof EntityLivingBase)
        {
            NBTTagCompound nbt = event.entity.getEntityData();
            if(nbt.hasNoTags()) {
            	InfernalMobsCore.instance().processEntitySpawn((EntityLivingBase) event.entity);
            	if(!(InfernalMobsCore.lotr && LOTRConfig.enchantingLOTR)) {
            		nbt.setBoolean("Tags", true); // because otherwise it will always be empty
            	}
            }
            else if(nbt.hasKey(InfernalMobsCore.instance().getNBTTag())) {
            	String savedMods = nbt.getString(InfernalMobsCore.instance().getNBTTag());
            	InfernalMobsCore.instance().addEntityModifiersByString((EntityLivingBase) event.entity, savedMods, false);
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingDeath(LivingDeathEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving, false);
            if (mod != null)
            {
                if (mod.onDeath())
                {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingSetAttackTarget(LivingSetAttackTargetEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving, false);
            if (mod != null)
            {
                mod.onSetAttackTarget(event.target);
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingAttacked(LivingAttackEvent event)
    {
        /* fires both client and server before hurt, but we dont need this */
    	
    	Entity attacker = event.source.getEntity();
        if(attacker != null && attacker instanceof EntityLivingBase) {
        	boolean client = event.entityLiving.worldObj.isRemote;
	    	MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) attacker, client);
	        if (mod != null && mod.disableDamage(event.source)) {
	        	event.setCanceled(true);
	        }
        }
    }

    /**
     * Hook into EntityLivingHurt. Is always serverside, assured by mc itself
     */
    @SubscribeEvent
    public void onEntityLivingHurt(LivingHurtEvent event)
    {
    	boolean client = event.entityLiving.worldObj.isRemote;
        // dont allow masochism
        if (event.source.getEntity() != event.entityLiving)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving, client);
            if (mod != null)
            {
                event.ammount = mod.onHurt(event.entityLiving, event.source, event.ammount);
            }

            /*
             * We use the Hook two-sided, both with the Mob as possible target
             * and attacker
             */
            Entity attacker = event.source.getEntity();
            if (attacker != null && attacker instanceof EntityLivingBase)
            {
                mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) attacker, client);
                if (mod != null)
                {
                    event.ammount = mod.onAttack(event.entityLiving, event.source, event.ammount);
                    /*if(event.ammount < 0F) {
                    	System.out.println("cancelled");
                    	event.setCanceled(true);
                    }*/
                }
            }

            if (antiMobFarm)
            {
                /*
                 * check for an environmental/automated damage type, aka mob farms
                 */
                if (event.source == DamageSource.cactus || event.source == DamageSource.drown || event.source == DamageSource.fall
                        || event.source == DamageSource.inWall || event.source == DamageSource.lava || event.source.getEntity() instanceof FakePlayer)
                {
                    ChunkCoordIntPair cpair = new ChunkCoordIntPair((int) event.entityLiving.posX, (int) event.entityLiving.posZ);
                    Float value = damageMap.get(cpair);
                    if (value == null)
                    {
                        for (Entry<ChunkCoordIntPair, Float> e : damageMap.entrySet())
                        {
                            if (Math.abs(e.getKey().chunkXPos - cpair.chunkXPos) < 3)
                            {
                                if (Math.abs(e.getKey().chunkZPos - cpair.chunkZPos) < 3)
                                {
                                    e.setValue(e.getValue() + event.ammount);
                                    break;
                                }
                            }
                        }
                    }
                    else
                    {
                        damageMap.put(cpair, value + event.ammount);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingFall(LivingFallEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving, false);
            if (mod != null)
            {
                event.setCanceled(mod.onFall(event.distance));
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingJump(LivingEvent.LivingJumpEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving, false);
            if (mod != null)
            {
                mod.onJump(event.entityLiving);
            }
        }
    }

    @SubscribeEvent
    public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event)
    {
        if (!event.entityLiving.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving, false);
            if (mod != null)
            {
                mod.onUpdate(event.entityLiving);
            }

            if (antiMobFarm && System.currentTimeMillis() > nextMapEvaluation)
            {
                if (!damageMap.isEmpty())
                {
                    float maxDamage = 0f;
                    float val;
                    ChunkCoordIntPair maxC = null;
                    for (Entry<ChunkCoordIntPair, Float> e : damageMap.entrySet())
                    {
                        val = e.getValue();
                        if (val > maxDamage)
                        {
                            maxC = e.getKey();
                            maxDamage = val;
                        }
                    }

                    System.out.println("Infernal Mobs AntiMobFarm damage check, max detected chunk damage value " + maxDamage + " near coords "
                            + maxC.getCenterXPos() + ", " + maxC.getCenterZPosition());
                    if (maxDamage > mobFarmDamageTrigger)
                    {
                        MinecraftForge.EVENT_BUS.post(new MobFarmDetectedEvent(event.entityLiving.worldObj.getChunkFromChunkCoords(maxC.chunkXPos,
                                maxC.chunkZPos), mobFarmCheckIntervals, maxDamage));
                    }
                    damageMap.clear();
                }
                nextMapEvaluation = System.currentTimeMillis() + mobFarmCheckIntervals;
            }
        }
    }

    public static class MobFarmDetectedEvent extends ChunkEvent
    {
        public final long triggeringInterval;
        public final float triggeringDamage;

        public MobFarmDetectedEvent(Chunk chunk, long ti, float td)
        {
            super(chunk);
            triggeringInterval = ti;
            triggeringDamage = td;
        }
    }

    @SubscribeEvent
    public void onEntityLivingDrops(LivingDropsEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.entityLiving, false);
            if (mod != null)
            {
                mod.onDropItems(event.entityLiving, event.source, event.drops, event.lootingLevel, event.recentlyHit, event.specialDropValue);
                InfernalMobsCore.removeEntFromElites(event.entityLiving, false);
            }
        }
    }
}
