package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import atomicstryker.infernalmobs.common.MobModifier;
import lotr.common.LOTRMod;

public class MM_Bomber extends MobModifier {

	private EntityLivingBase host;
	private int bombfuse = 35;
	private int bomblvl = 0;
	private double distance;

	public MM_Bomber(EntityLivingBase mob)
    {
		this(mob, null);
    }
    
    public MM_Bomber(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Bomber";
        this.nextMod = prevMod;
        this.host = mob;
        this.bomblvl = mob.getRNG().nextInt(2); // explosion size 4 to 8
        this.distance = 3.25D + mob.width;
        this.distance *= distance;
        
    }
    
    @Override
    public boolean disableDamage(DamageSource source) {
    	if(source.damageType.equals("mob")) {
    		return true;
    	}
        return super.disableDamage(source);
    }
    
    @Override
    public boolean onUpdate(EntityLivingBase mob) {
    	EntityLivingBase target = getTargetFor(mob);
    	if(target != null) {
    		
    		
	    	if(mob.getDistanceSq(target.posX, target.boundingBox.minY, target.posZ) <= distance) {
	    		if((mob.ticksExisted & 3) == 0)
	    			mob.worldObj.playSoundAtEntity(mob, "game.tnt.primed", 1.0F, 1.0F); 
	    		if(bombfuse > 20) {
	    	        while (bombfuse > 20)
	    	        	bombfuse -= 10; 
	    	    }
	    		else if(bombfuse > 0) {
			    	bombfuse--;
			    }
			    else {
			    	mob.worldObj.createExplosion(mob, mob.posX, mob.posY, mob.posZ, (bomblvl + 1) * 4F, mob.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing") && mob.worldObj.getClosestPlayerToEntity(mob, 10F) != null);
			        mob.setDead();
			    }
			    
	    	}
	    	else if(bombfuse <= 20) {
	    		 while (bombfuse <= 20)
	    			 bombfuse += 10;
	    	}
	    	
	    	if(bombfuse < 35 && (mob.ticksExisted % 4) == 0) {
	    		
	    			((WorldServer) mob.worldObj).func_147487_a("smoke", mob.posX, mob.posY + mob.height + 0.5D, mob.posZ, 
	    				4, 0.0D, 0.0D, 0.0D, 0.01F);
	    	}
    	}
    	else {
    		bombfuse = 35;
    	}
    	
    	
    	
    	
    	
    	return super.onUpdate(mob);
    }
    
    @Override
    public void onSetAttackTarget(EntityLivingBase target) {
    	if(target != null) {
    		host.worldObj.playSoundAtEntity(host, "game.tnt.primed", 2.0F, 1.0F); 
    	}
    	super.onSetAttackTarget(target);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofC4" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "explosive" };
    
}
