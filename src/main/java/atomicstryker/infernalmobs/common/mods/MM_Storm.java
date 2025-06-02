package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import lotr.common.LOTRBannerProtection;

public class MM_Storm extends MobModifier
{
    public MM_Storm(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Storm(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Storm";
        this.nextMod = prevMod;
    }
    
    
    private final static int coolDown = 300; // 15 seconds
    private final static float MIN_DISTANCE = 3F;
    
    private int nextAbilityUse;
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.isFireDamage() && getTickTime() < nextAbilityUse - coolDown + 60) { // 3 seconds fire protection
        	mob.extinguish();
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
    	int timeTicks = getTickTime();
    	if(timeTicks >= nextAbilityUse)  {
    		EntityLivingBase target = getTargetFor(mob, true, false, -1F);
    		
    		if(target != null 
    		&& mob.getDistanceSqToEntity(target) > MIN_DISTANCE * MIN_DISTANCE
    		&& target.worldObj.canBlockSeeTheSky(MathHelper.floor_double(target.posX), MathHelper.floor_double(target.posY), MathHelper.floor_double(target.posZ))) {
    			nextAbilityUse = timeTicks + coolDown;
    			tryAbility(mob, target);
    		}
    	}
    	
        return super.onUpdate(mob);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        boolean allow = false;
        if(target instanceof EntityPlayer) {
        	if(target.ridingEntity == null) // dont kill our mounts
        		allow = true;
        }
        // if a player is close then its ok
        else if(mob.worldObj.getClosestPlayerToEntity(target, 10F) != null) {
        	allow = true;
        }
        
        if(allow && (!InfernalMobsCore.lotr || !LOTRBannerProtection.isProtected(mob.worldObj, MathHelper.floor_double(target.posX), MathHelper.floor_double(target.posY-1), MathHelper.floor_double(target.posZ), mob instanceof EntityLiving ? LOTRBannerProtection.forNPC((EntityLiving) mob) : LOTRBannerProtection.anyBanner(), false)))
        	mob.worldObj.addWeatherEffect(new EntityLightningBolt(mob.worldObj, target.posX, target.posY-1, target.posZ));
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofLightning", "theRaiden" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "striking", "thundering", "electrified" };
    
}
