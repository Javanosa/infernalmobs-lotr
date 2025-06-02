package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;

import java.util.Random;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Choke extends MobModifier
{
    private EntityLivingBase lastTarget;
    private int lastAir;
    private boolean prevCanSee;
    private long nextAirPacket;
    
    public MM_Choke(EntityLivingBase mob)
    {
        this(mob, null);
    }

    public MM_Choke(EntityLivingBase mob, MobModifier prevMod)
    {
    	this.modName = "Choke";
    	this.lastTarget = null;
        this.lastAir = -999;
        this.nextMod = prevMod;
    }
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
    	EntityLivingBase target = getTargetFor(mob);
        if (target != lastTarget)
        {
            lastAir = -999;
            if (lastTarget != null)
            {
            	updateAir(true);
            }
            lastTarget = target;
        }

        if (lastTarget != null)
        {
        	boolean canSee = mob.canEntityBeSeen(lastTarget);
            if (canSee)
            {
            	prevCanSee = true;
                if (lastAir == -999)
                {
                    lastAir = lastTarget.getAir();
                }
                else
                {
                	 
                    lastAir = Math.min(lastAir, lastTarget.getAir());
                    
                }

                if (!(lastTarget instanceof EntityPlayer && ((EntityPlayer) lastTarget).capabilities.disableDamage))
                {
                	
                    lastAir--;
                    if (lastAir < -19)
                    {
                    	
                    	
                        lastAir = 0;
                        
                        Random rand = lastTarget.getRNG();
                        
                        lastTarget.worldObj.playSoundAtEntity(lastTarget, "random.splash", 0.5F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                        
                        lastTarget.attackEntityFrom(DamageSource.drown, 2.0F);
                        
                    }
                    
                    updateAir(false);
                }
            }
            else if(canSee != prevCanSee && lastAir != -999) {
            	prevCanSee = false;
            	updateAir(true);
        	}
            
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (lastTarget != null && source.getSourceOfDamage() == lastTarget && lastAir != -999)
        {
            lastAir = Math.min(300, lastAir + 60); // prevent overflow
            
            updateAir(true);
        }
        
        return damage;
    }
    
    @Override
    public boolean onDeath()
    {
        lastAir = -999;
        if (lastTarget != null)
        {
            updateAir(true);
            lastTarget = null;
        }
        return false;
    }
    
    private void updateAir(boolean force)
    {
    	lastTarget.setAir(lastAir);
        
        if (lastTarget instanceof EntityPlayerMP && (force || nextAirPacket < System.currentTimeMillis()))
        {
            InfernalMobsCore.instance().sendAirPacket((EntityPlayerMP) lastTarget, lastAir, !prevCanSee);
            nextAirPacket = System.currentTimeMillis() + 500;
        }
    }

    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }

    private static String[] suffix = { "ofBreathlessness", "theAnaerobic", "ofDeprivation" };

    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }

    private static String[] prefix = { "Sith Lord", "Dark Lord", "Darth" };

}
