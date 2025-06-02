package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Lifesteal extends MobModifier
{
    public MM_Lifesteal(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Lifesteal(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "LifeSteal";
        this.nextMod = prevMod;
    }
    
    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage)
    {
        EntityLivingBase mob = (EntityLivingBase) source.getEntity();
        if (entity != null
        && mob.getHealth() < getActualMaxHealth(mob))
        {
            // InfernalMobsCore.instance().setEntityHealthPastMax(mob, mob.getHealth()+damage);
            // new start
        	// this ensure that we dont end up having lower than possible health, additionally we can increase max health always by one if we want?
        	float dmg = damage;
        	if(!(entity instanceof EntityPlayer)) {
        		dmg *= 0.2f; // 20% for non player targets
        	}
            float newhealth = mob.getHealth() + dmg;
            IAttributeInstance attr = mob.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            attr.setBaseValue(Math.max(attr.getBaseValue(), newhealth));
            mob.setHealth(newhealth);
            // new end
        }
        
        return super.onAttack(entity, source, damage);
    }
        
    @Override
    public Class<?>[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class<?>[] disallowed = { EntityCreeper.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "theVampire", "ofTransfusion", "theBloodsucker" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "vampiric", "transfusing", "bloodsucking" };
    
}
