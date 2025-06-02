package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Vengeance extends MobModifier
{
    public MM_Vengeance(EntityLivingBase mob)
    {
        this(mob, null);
    }

    public MM_Vengeance(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Vengeance";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null && source.getEntity() != mob && !InfernalMobsCore.instance().isInfiniteLoop(mob, source.getEntity()))
        {
            source.getEntity().attackEntityFrom(DamageSource.causeThornsDamage(mob),
                    InfernalMobsCore.instance().getLimitedDamage(Math.max(damage / 2, 1)));
            
            // source.getEntity().playSound("damage.thorns", 0.5F, 1.0F);
            // rn this sound doesnt exists in older versions
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }

    private static String[] suffix = { "ofRetribution", "theThorned", "ofStrikingBack" };

    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }

    private static String[] prefix = { "thorned", "thorny", "spiky" };

}
