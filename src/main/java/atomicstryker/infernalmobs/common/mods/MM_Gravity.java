package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Gravity extends MobModifier
{
    public MM_Gravity(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Gravity(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Gravity";
        this.nextMod = prevMod;
        // 0 = pull, 1 = push, 2 = pull & push
        actionFlag = mob instanceof EntityCreeper ? 0 : mob.getRNG().nextInt(3);
        
    }
    
    private final static int coolDown = 100; // 5 seconds
    
    private final int actionFlag;    
    private int nextAbilityUse;
    
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
    	// pull
        if((actionFlag & 1) == 0) {
        	EntityLivingBase target = getTargetFor(mob);
        	
        	if(target != null)
	        {
	            tryAbility(mob, target, false);
	        }
        }
        
        return super.onUpdate(mob);
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
    	// push
        if((actionFlag & 2) == 0) {
        	Entity attacker = source.getEntity();
	        
        	if(attacker != null && attacker instanceof EntityLivingBase)
	        {
	            tryAbility(mob, (EntityLivingBase) attacker, true);
	        }
        }
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target, boolean push)
    {
    	
        /*
        // we dont need to see it
        if (!mob.canEntityBeSeen(target))
        {
            return;
        }*/
        
        int timeTicks = getTickTime();
        if (timeTicks >= nextAbilityUse)
        {
            nextAbilityUse = timeTicks + coolDown;
            
            EntityLivingBase source = push ? mob : target;
            EntityLivingBase destination = push ? target : mob;
            double diffX = destination.posX - source.posX;
            double diffZ;
            for (diffZ = destination.posZ - source.posZ; diffX * diffX + diffZ * diffZ < 1.0E-4D; diffZ = (Math.random() - Math.random()) * 0.01D)
            {
                diffX = (Math.random() - Math.random()) * 0.01D;
            }
            
            mob.worldObj.playSoundAtEntity(mob, "mob.irongolem.throw", 1.0F, (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
            
            if (mob.worldObj.isRemote || !(target instanceof EntityPlayerMP))
            {
                knockBack(target, diffX, diffZ);
            }
            else
            {
                InfernalMobsCore.instance().sendKnockBackPacket((EntityPlayerMP) target, (float) diffX, (float) diffZ);
            }
        }
    }
    
    public static void knockBack(EntityLivingBase target, double x, double z)
    {
        target.isAirBorne = true;
        float normalizedPower = MathHelper.sqrt_double(x * x + z * z);
        float knockPower = 0.8F;
        target.motionX /= 2.0D;
        target.motionY /= 2.0D;
        target.motionZ /= 2.0D;
        target.motionX -= x / (double)normalizedPower * (double)knockPower;
        target.motionY += (double)knockPower;
        target.motionZ -= z / (double)normalizedPower * (double)knockPower;

        if (target.motionY > 0.4000000059604645D)
        {
            target.motionY = 0.4000000059604645D;
        }
    }
    
    @Override
    public Class<?>[] getModsNotToMixWith()
    {
        return modBans;
    }
    private static Class<?>[] modBans = { MM_Webber.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofRepulsion", "theFlipper" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "repulsing", "sproing" };
    
}
