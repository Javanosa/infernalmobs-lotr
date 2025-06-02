package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Blocks;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.MM_Webber.TrackInfo;

public class MM_Ghastly extends MobModifier
{
	// todo: fireball add enttiy to explosion for lotr protection, needs eventhandler and coremod
	// kill fireball in a better way, some tick handling with map
	
	private final static int coolDown = 120; // 6 seconds
    private final static float MIN_DISTANCE = 3F;
    private final static float MAX_DISTANCE = 15F;
	
	private final int explosionpower;
	private int nextAbilityUse;
    
	
    public MM_Ghastly(EntityLivingBase mob)
    {
    	this(mob, null);
    }
    
    public MM_Ghastly(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Ghastly";
        this.nextMod = prevMod;
        this.explosionpower = 1 + mob.getRNG().nextInt(3); // 1 to 3 explosion Size
    }
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        int timeTicks = getTickTime();
        if (timeTicks >= nextAbilityUse)
        {
        	EntityLivingBase target = getTargetFor(mob, true, true, MAX_DISTANCE);
        	if(target != null && mob.getDistanceSqToEntity(target) > MIN_DISTANCE * MIN_DISTANCE && mob.canEntityBeSeen(target)) {
        		nextAbilityUse = timeTicks + coolDown;
                tryAbility(mob, target);
        	}
        }
        return super.onUpdate(mob);
    }
    
    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        double diffX = target.posX - mob.posX;
        double diffY = target.boundingBox.minY + (double)(target.height / 2.0F) - (mob.posY + (double)(mob.height / 2.0F));
        double diffZ = target.posZ - mob.posZ;
        mob.renderYawOffset = mob.rotationYaw = -((float)Math.atan2(diffX, diffZ)) * 180.0F / (float)Math.PI;

        mob.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1008, (int)mob.posX, (int)mob.posY, (int)mob.posZ, 0);
        EntityLargeFireball entFB = new EntityLargeFireball(mob.worldObj, mob, diffX, diffY, diffZ);
        entFB.field_92057_e = explosionpower;
        double spawnOffset = 2.0D;
        Vec3 mobLook = mob.getLook(1.0F);
        entFB.posX = mob.posX + mobLook.xCoord * spawnOffset;
        entFB.posY = mob.posY + (double)(mob.height / 2.0F) + 0.5D;
        entFB.posZ = mob.posZ + mobLook.zCoord * spawnOffset;
        mob.worldObj.spawnEntityInWorld(entFB);
        
        /*Executors.newScheduledThreadPool(1).schedule(new Runnable() {

				@Override
				public void run() {
					entFB.setDead();
				}
        		
        	}, 9, TimeUnit.SECONDS);*/
        
        if(InfernalMobsCore.toRemove == null) {
        	InfernalMobsCore.toRemove = new ArrayList<>();
        }
        
        TrackInfo info = new TrackInfo(mob.worldObj, getTickTime() + 180);
        info.entity = entFB;
        InfernalMobsCore.toRemove.add(info);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "OMFGFIREBALLS", "theBomber", "ofBallsofFire" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "bombing", "fireballsy" };
    
}
