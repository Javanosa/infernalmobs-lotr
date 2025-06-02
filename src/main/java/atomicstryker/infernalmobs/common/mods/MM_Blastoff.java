package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Blastoff extends MobModifier
{
    public MM_Blastoff(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Blastoff(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Blastoff";
        this.nextMod = prevMod;
    }
    
    private final static int coolDown = 300; // 15 seconds
    
    private int nextAbilityUse;
    
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
    	EntityLivingBase target = getTargetFor(mob);
        if (target != null)
        {
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

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        if (!mob.canEntityBeSeen(target))
        {
            return;
        }
        
        int timeTicks = getTickTime();
        if (timeTicks >= nextAbilityUse)
        {
            nextAbilityUse = timeTicks + coolDown;
            mob.worldObj.playSoundAtEntity(mob, "mob.slime.big", 1.0F, (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
            
            if (target.worldObj.isRemote || !(target instanceof EntityPlayerMP))
            {
                target.addVelocity(0, 1.1D, 0);
            }
            else
            {
                InfernalMobsCore.instance().sendVelocityPacket((EntityPlayerMP) target, 0f, 1.1f, 0f);
            }
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
    private static String[] suffix = { "ofMissionControl", "theNASA", "ofWEE" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "thumping", "trolling", "byebye" };
    
}
