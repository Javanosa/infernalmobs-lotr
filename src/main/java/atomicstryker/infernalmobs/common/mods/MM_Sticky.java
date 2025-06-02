package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

import java.lang.reflect.Field;

import atomicstryker.infernalmobs.common.MobModifier;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class MM_Sticky extends MobModifier
{
    public MM_Sticky(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Sticky(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Sticky";
        this.nextMod = prevMod;
    }
    
    private static Field field_invulnerable;
    private final static int coolDown = 300; // 15 seonds
    
    private int nextAbilityUse;
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
    	Entity attacker = source.getEntity();
        if (attacker != null && !(source instanceof EntityDamageSourceIndirect) && attacker instanceof EntityPlayer)
        {
            EntityPlayer p = (EntityPlayer) attacker;
            ItemStack weapon = p.inventory.getStackInSlot(p.inventory.currentItem);
            if (weapon != null)
            {
                int timeTicks = getTickTime();
                if (timeTicks >= nextAbilityUse)
                {
                    nextAbilityUse = timeTicks + coolDown;
                    EntityItem drop = p.dropPlayerItemWithRandomChoice(p.inventory.decrStackSize(p.inventory.currentItem, 1), false);
                    if (drop != null)
                    {
                    	// prevent the item from being burnt or exploded (can still despawn)
                    	if(field_invulnerable == null)
                    		field_invulnerable = ReflectionHelper.findField(Entity.class, "invulnerable", "field_83001_bt", "i");
                        
                        try {
                        	field_invulnerable.setBoolean(drop, true);
                		} catch (Exception e) {
                			e.printStackTrace();
                		}
                    	
                    	drop.delayBeforeCanPickup = 50; // 2.5 seconds for pickup
                        p.worldObj.playSoundAtEntity(mob, "mob.slime.big", 1.0F, (p.worldObj.rand.nextFloat() - p.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
                    }
                }
            }
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    private Class<?>[] disallowed = { EntityCreeper.class };
    
    @Override
    public Class<?>[] getBlackListMobClasses()
    {
        return disallowed;
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofSnagging", "theQuickFingered", "ofPettyTheft", "yoink" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "thieving", "snagging", "quickfingered" };
    
}
