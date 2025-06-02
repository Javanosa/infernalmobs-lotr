package atomicstryker.infernalmobs.common.mods;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Webber extends MobModifier
{
	public static class TrackInfo {
		public World world;
		public int timeTicks;
		public ChunkPosition pos;
		public Block compare;
		public Entity entity;
		
		public TrackInfo(World world, int timeTicks) {
			this.world = world;
			this.timeTicks = timeTicks;
		}
	}
	
    public MM_Webber(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Webber(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Webber";
        this.nextMod = prevMod;
    }
    
    private int nextAbilityUse;
    private final static int coolDown = 300; // 15 seconds
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
    	EntityLivingBase target = getTargetFor(mob);
    	if(target != null) {
    		tryAbility(mob, target);
    	}
    	
        return super.onUpdate(mob);
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
    	Entity attacker = source.getEntity();
        if (attacker != null
        && attacker instanceof EntityLivingBase)
        {
            tryAbility(mob, (EntityLivingBase) attacker);
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    public static boolean isReplaceableBlock(World world, Chunk chunk, int x, int y, int z) {
    	if(y < 0 || y >= 256) 
    		return false;
    	
    	if(chunk.getBlock(x & 15, y, z & 15).isReplaceable(world, x, y, z)) {
    		return true;
    	}
		return false;
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        if (!mob.canEntityBeSeen(target))
        {
            return;
        }
        
        int x = MathHelper.floor_double(target.posX);
        int y = MathHelper.floor_double(target.posY);
        int z = MathHelper.floor_double(target.posZ);
        
        int timeTicks = getTickTime();
        if (timeTicks >= nextAbilityUse)
        {
        	World world = target.worldObj;
        	Chunk chunk = world.getChunkFromBlockCoords(x, z);
        	
        	int offset;
            if (isReplaceableBlock(world, chunk, x, y, z))
            {
                offset = 0;
            }
            else if (isReplaceableBlock(world, chunk, x, y - 1, z))
            {
                offset = -1;
            }
            else if (isReplaceableBlock(world, chunk, x, y + 1, z))
            {
                offset = 1;
            }
            else
            {
                return;
            }
            
            nextAbilityUse = timeTicks + coolDown;
            y += offset;
            world.setBlock(x, y, z, Blocks.web);
            
            if(InfernalMobsCore.toRemove == null) {
            	InfernalMobsCore.toRemove = new ArrayList<>();
            }
            
            TrackInfo info = new TrackInfo(world, timeTicks + 1200);
            info.pos = new ChunkPosition(x, y, z);
            info.compare = Blocks.web;
            InfernalMobsCore.toRemove.add(info);
            
            
        	/*Executors.newScheduledThreadPool(1).schedule(new Runnable() {

				@Override
				public void run() {
					if(target.worldObj.getBlock(x, y+offset, z) == Blocks.web)
						target.worldObj.setBlockToAir(x, y+offset, z);
				}
        		
        	}, 60, TimeUnit.SECONDS);*/
            
            
            mob.worldObj.playSoundAtEntity(mob, "mob.spider.say", 1.0F, (mob.getRNG().nextFloat() - mob.getRNG().nextFloat()) * 0.2F + 1.0F);
        }
    }
    
    @Override
    public Class<?>[] getModsNotToMixWith()
    {
        return modBans;
    }
    private static Class<?>[] modBans = { MM_Gravity.class, MM_Blastoff.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofTraps", "theMutated", "theSpider" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "ensnaring", "webbing" };
    
}
