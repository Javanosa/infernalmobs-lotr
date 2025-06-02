package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Cloaking extends MobModifier
{
	private final static int coolDown = 200; // 10 seconds
	
	private int nextAbilityUse;
	
    public MM_Cloaking(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Cloaking(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Cloaking";
        this.nextMod = prevMod;
    }
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
    	EntityLivingBase target = getTargetFor(mob);
        if (target != null
        && target instanceof EntityPlayer) {
            tryAbility(mob);
        }
        
        return super.onUpdate(mob);
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null
        && source.getEntity() instanceof EntityLivingBase)
        {
            tryAbility(mob);
        }
        
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob)
    {
        int timeTicks = getTickTime();
        if (timeTicks >= nextAbilityUse)
        {
            nextAbilityUse = timeTicks + coolDown;
            mob.addPotionEffect(new PotionEffect(Potion.invisibility.id, 220)); // was 200, now doesnt "flicker" when usetime is up
        }
    }
    
    @Override
    public Class<?>[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class<?>[] disallowed = { EntitySpider.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofStalking", "theUnseen", "thePredator" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "stalking", "unseen", "hunting" };
    
}
