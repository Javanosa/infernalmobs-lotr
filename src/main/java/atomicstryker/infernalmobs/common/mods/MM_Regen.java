package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Regen extends MobModifier
{
    public MM_Regen(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Regen(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Regen";
        this.nextMod = prevMod;
    }
    
    
    private final static int coolDown = 50; // 2.5 seconds
    private int nextAbilityUse;
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        int timeTicks = getTickTime();
        if (timeTicks >= nextAbilityUse)
        {
            nextAbilityUse = timeTicks + coolDown;
            //InfernalMobsCore.instance().setEntityHealthPastMax(mob, mob.getHealth()+1);
            // new start
            // this mode should we called gainHealth in time
            float health = mob.getHealth();
            if(health >= mob.getMaxHealth() && health < getActualMaxHealth(mob)) {
            	// if we are at max health we can increase the max health
            	IAttributeInstance attr = mob.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            	attr.setBaseValue(Math.max(attr.getBaseValue(), health+1));
            }
            mob.heal(1);
            // new end
        }
        
        return super.onUpdate(mob);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofWTFIMBA", "theCancerous", "ofFirstAid" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "regenerating", "healing", "nighunkillable" };
}
