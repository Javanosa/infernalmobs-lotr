package atomicstryker.infernalmobs.common.mods;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Ninja extends MobModifier
{
    public MM_Ninja(EntityLivingBase mob)
    {
        this(mob, null);
    }
    
    public MM_Ninja(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Ninja";
        this.nextMod = prevMod;
    }
    
   
    private final static int coolDown = 300; // 15 seconds
    
    private int nextAbilityUse;
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        int timeTicks = getTickTime();
        if (timeTicks >= nextAbilityUse) {
        	Entity attacker = source.getEntity();
	        if(attacker != null
	        && attacker != mob
	        && !InfernalMobsCore.instance().isInfiniteLoop(mob, attacker)
	        && teleportToEntity(mob, attacker))
	        {
	            nextAbilityUse = timeTicks + coolDown;
	            attacker.attackEntityFrom(DamageSource.causeMobDamage(mob), InfernalMobsCore.instance().getLimitedDamage(damage));
	            return super.onHurt(mob, source, 0);
	        }
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    private boolean teleportToEntity(EntityLivingBase mob, Entity par1Entity)
    {
        Vec3 vector = Vec3.createVectorHelper(mob.posX - par1Entity.posX, 
        		// mid of mob  							- 			target eye pos
        		mob.boundingBox.minY + (mob.height / 2.0F) - par1Entity.posY + par1Entity.getEyeHeight()
        		, mob.posZ - par1Entity.posZ);
        vector = vector.normalize();
        double telDist = 8D;
        double destX = mob.posX + (mob.worldObj.rand.nextDouble() - 0.5D) * 4.0D - vector.xCoord * telDist;
        double destY = mob.posY + vector.yCoord + 3;
        double destZ = mob.posZ + (mob.worldObj.rand.nextDouble() - 0.5D) * 4.0D - vector.zCoord * telDist;
        return teleportTo(mob, destX, destY, destZ);
    }
    
    private boolean teleportTo(EntityLivingBase mob, double destX, double destY, double destZ)
    {
        double oldX = mob.posX;
        double oldY = mob.posY;
        double oldZ = mob.posZ;
        mob.posX = destX;
        mob.posY = destY;
        mob.posZ = destZ;
        int x = MathHelper.floor_double(mob.posX);
        int y = MathHelper.floor_double(mob.posY);
        int z = MathHelper.floor_double(mob.posZ);
        Block blockID;
        
        boolean hitGround = false;
            int trys = 0;
            while (!hitGround && y < 96 && trys < 10)
            {
            	++trys;
                blockID = mob.worldObj.getBlock(x, y - 1, z);
                if (blockID.getMaterial().blocksMovement())
                {
                    hitGround = true;
                }
                else
                {
                    --mob.posY;
                    --y;
                }
            }
            
            

            if (hitGround && !mob.worldObj.getBlock(x, y, z).getMaterial().blocksMovement())
            {
                mob.setPosition(mob.posX, mob.posY, mob.posZ);
                
                mob.worldObj.playSoundEffect(oldX, oldY, oldZ, "random.explode", 2.0F, (1.0F + (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
                //mob.worldObj.spawnParticle("hugeexplosion", oldX, oldY, oldZ, 0D, 0D, 0D);
                ((WorldServer) mob.worldObj).func_147487_a("hugeexplosion", oldX, oldY, oldZ, 
	    				1, 0.0D, 0.0D, 0.0D, 0.01F);
                
                return true;
            }
        
        return false;
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "theZenMaster", "ofEquilibrium", "ofInnerPeace" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "totallyzen", "innerlypeaceful", "Ronin" };
    
}
