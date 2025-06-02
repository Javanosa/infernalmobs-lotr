package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Alchemist extends MobModifier
{
    public MM_Alchemist(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Alchemist(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Alchemist";
        this.nextMod = prevMod;
    }
    
    
    private final static int coolDown = 120; // 6 seconds
    private final static float MIN_DISTANCE = 2F;
    private final static float MAX_DISTANCE = 12F;
    
    private int nextAbilityUse;
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
    	int timeTicks = getTickTime();
    	if(timeTicks >= nextAbilityUse) {
	    	EntityLivingBase target = getTargetFor(mob, false, false, MAX_DISTANCE);
	    	if(target != null) {
	    		float distance = mob.getDistanceToEntity(target);
	    		if(distance <= MAX_DISTANCE && distance >= MIN_DISTANCE) {
	    			nextAbilityUse = timeTicks+coolDown;
		    		tryAbility(mob, target);
	    		}
	    	}
    	}
        return super.onUpdate(mob);
    }
    
    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
    	
        EntityPotion potion = new EntityPotion(mob.worldObj, mob, 32732); // instant_damage I
        potion.rotationPitch -= -20.0F;
        double diffX = target.posX + target.motionX - mob.posX;
        double diffY = target.posY + (double)target.getEyeHeight() - 1.100000023841858D - mob.posY;
        double diffZ = target.posZ + target.motionZ - mob.posZ;
        float distance = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

        if (distance >= 8.0F && !target.isPotionActive(Potion.moveSlowdown))
        {
            potion.setPotionDamage(32698); // slowness I
        }
        else if (target.getHealth() >= 8 && !target.isPotionActive(Potion.poison))
        {
            potion.setPotionDamage(32660); // poison I
        }
        else if (distance <= 3.0F && !target.isPotionActive(Potion.weakness) && mob.getRNG().nextFloat() < 0.25F)
        {
            potion.setPotionDamage(32696); // weakness I
        }
        else if (target.getHealth() >= 4 && mob.getRNG().nextFloat() < 0.25F)
        {
            potion.setPotionDamage(32636); // instant_damage II
            
        }

        potion.setThrowableHeading(diffX, diffY + (distance * 0.2F), diffZ, 0.75F, 8.0F);
        mob.worldObj.spawnEntityInWorld(potion);
        
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "theWitchkin", "theBrewmaster", "theSinged" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "witchkin", "brewing", "singed" };
    
}
