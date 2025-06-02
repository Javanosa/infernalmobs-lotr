package atomicstryker.infernalmobs.client;

import net.minecraft.client.particle.EntitySpellParticleFX;
import net.minecraft.world.World;

public class EnityBossFX extends EntitySpellParticleFX {

	// modified to glow in darkness
	public EnityBossFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z, motionX, motionY, motionZ);
	}
	
	public int getBrightnessForRender(float partialTicks)
    {
		float ratioHealth = (particleAge + partialTicks) / particleMaxAge;

        if (ratioHealth < 0.0F)
        {
            ratioHealth = 0.0F;
        }

        if (ratioHealth > 1.0F)
        {
            ratioHealth = 1.0F;
        }

        int i = super.getBrightnessForRender(partialTicks);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += ratioHealth * 15.0F * 16.0F;

        if (j > 240)
        {
            j = 240;
        }

        return j | k << 16;
    }


}
