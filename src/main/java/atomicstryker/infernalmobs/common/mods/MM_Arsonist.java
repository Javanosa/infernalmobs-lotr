package atomicstryker.infernalmobs.common.mods;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.lang.reflect.Field;


import atomicstryker.infernalmobs.common.MobModifier;
import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.common.LOTRBannerProtection;

public class MM_Arsonist extends MobModifier
{
	private static Field field_isImmuneToFire;
	
	public MM_Arsonist(EntityLivingBase mob)
    {
		this(mob, null);
    }

    public MM_Arsonist(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Arsonist";
        this.nextMod = prevMod;
        if(field_isImmuneToFire == null)
        	field_isImmuneToFire = ReflectionHelper.findField(Entity.class, "isImmuneToFire", "field_70178_ae", "ae");
        
        try {
			field_isImmuneToFire.setBoolean(mob, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }
    
    
    private final static int targetCoolDown = 20;
    private final static int coolDown = 5;
    
    
    private boolean hasPlayerTarget;
    private int nextTargetCheck;
    private int nextAbilityUse;
    
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
    	int timeTicks = MinecraftServer.getServer().getTickCounter();
    	if(timeTicks >= nextTargetCheck) {
        	nextTargetCheck = timeTicks + targetCoolDown;
        	EntityLivingBase target = getTargetFor(mob, true, true, 15F);
        	hasPlayerTarget = target != null && mob.canEntityBeSeen(target);
        }
        
        if(hasPlayerTarget && timeTicks >= nextAbilityUse) {
        	nextAbilityUse = timeTicks + coolDown;
        	tryAbility(mob);
        }
        
        return super.onUpdate(mob);
    }
    
    private void tryAbility(EntityLivingBase mob) {
        int x = MathHelper.floor_double(mob.posX);
        int y = MathHelper.floor_double(mob.posY);
        int z = MathHelper.floor_double(mob.posZ);
        World world = mob.worldObj;
        
        Block block = world.getBlock(x, y, z);
        
        if(block.isReplaceable(world, x, y, z) && !block.getMaterial().isLiquid()) {
        	
    		if(Blocks.fire.canPlaceBlockAt(world, x, y, z) && !LOTRBannerProtection.isProtected(world, x, y, z, mob instanceof EntityLiving ? LOTRBannerProtection.forNPC((EntityLiving) mob) : LOTRBannerProtection.anyBanner(), false)) {
    			if(block != Blocks.fire) {
    				world.playSoundAtEntity(mob, "fire.ignite", 1.0F, mob.getRNG().nextFloat() * 0.4F + 0.8F);
    			}
    			world.setBlock(x, y, z, Blocks.fire);
    		}
        }
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "theSeaOfFlames" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "flaming" };
    
}
