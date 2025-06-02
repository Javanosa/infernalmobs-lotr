package atomicstryker.infernalmobs.common.mods;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;

import java.util.Random;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Ender extends MobModifier
{
    public MM_Ender(EntityLivingBase mob)
    {
        this(mob, null);
    }

    public MM_Ender(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Ender";
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
	        
	        if(attacker != null && attacker != mob && teleportToEntity(mob, attacker) && !InfernalMobsCore.instance().isInfiniteLoop(mob, attacker))
	        
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
        Vec3 vector =
                Vec3.createVectorHelper(mob.posX - par1Entity.posX, mob.boundingBox.minY + (double) (mob.height / 2.0F) - par1Entity.posY
                        + (double) par1Entity.getEyeHeight(), mob.posZ - par1Entity.posZ);
        vector = vector.normalize();
        double telDist = 16.0D;
        double destX = mob.posX + (mob.worldObj.rand.nextDouble() - 0.5D) * 8.0D - vector.xCoord * telDist;
        double destY = mob.posY + vector.yCoord + 8;
        double destZ = mob.posZ + (mob.worldObj.rand.nextDouble() - 0.5D) * 8.0D - vector.zCoord * telDist;
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
                
                int range = 16;
                Random rand = mob.getRNG();
                WorldServer worldserver = ((WorldServer) mob.worldObj);
                for (int i = 0; i < range; i++) {
                    double distance = i / (double) (range - 1);
                    float motionX = (rand.nextFloat() - 0.5F) * 0.2F;
                    float motionY = (rand.nextFloat() - 0.5F) * 0.2F;
                    float motionZ = (rand.nextFloat() - 0.5F) * 0.2F;
                    double posX = oldX + (mob.posX - oldX) * distance + (rand.nextDouble() - 0.5D) * mob.width * 2.0D;
                    double posY = oldY + (mob.posY - oldY) * distance + rand.nextDouble() * mob.height;
                    double posZ = oldZ + (mob.posZ - oldZ) * distance + (rand.nextDouble() - 0.5D) * mob.width * 2.0D;
                    //mob.worldObj.spawnParticle("portal", posX, posY, posZ, motionX, motionY, motionZ);
                    worldserver.func_147487_a("portal", 
                    	// pos
                    	posX, posY, posZ, 
                    	// amount
    	    			8, 	
    	    			// motion
    	    			motionX, motionY, motionZ, 		
    	    			// variation / random movement
    	    			0.1F);
                }
                
                
                
                

                mob.worldObj.playSoundEffect(oldX, oldY, oldZ, "mob.endermen.portal", 1.0F, 1.0F);
                mob.worldObj.playSoundAtEntity(mob, "mob.endermen.portal", 1.0F, 1.0F);
                return true;
            }
        
        return false;
    }

    

    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }

    private static String[] suffix = { "theEnderborn", "theTrickster" };

    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }

    private static String[] prefix = { "enderborn", "tricky" };

}
