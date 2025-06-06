package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Fiery extends MobModifier
{
    public MM_Fiery(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Fiery(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Fiery";
        this.nextMod = prevMod;
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
    	Entity attacker = source.getEntity();
        if (attacker != null
        && (attacker instanceof EntityLivingBase)
        && !(source instanceof EntityDamageSourceIndirect))
        {
        	attacker.setFire(3);
        }
        
        mob.extinguish();
        return super.onHurt(mob, source, damage);
    }
    
    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage)
    {
        if (entity != null)
        {
            entity.setFire(3);
        }
        
        return super.onAttack(entity, source, damage);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofConflagration", "thePhoenix", "ofCrispyness" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "burning", "toasting" };
    
}
