package atomicstryker.infernalmobs.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.chunk.Chunk;

import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.ChunkEvent;

public class SaveEventHandler {

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) 
    {
    	
        Chunk chunk = event.getChunk();
        if(chunk.hasEntities) {
        	boolean client = event.world.isRemote;
	        Entity entity;
	        @SuppressWarnings("unchecked")
			List<Entity>[] lists = chunk.entityLists;
	        for (int i = 0; i < lists.length; i++)
	        {
	            for (int j = 0; j < lists[i].size(); j++)
	            {
	                entity = lists[i].get(j);
	                if (entity instanceof EntityLivingBase)
	                {
	                    if (InfernalMobsCore.getIsRareEntity((EntityLivingBase) entity, client))
	                    {
	                    	InfernalMobsCore.removeEntFromElites((EntityLivingBase) entity, client);
	                    }
	                }
	            }
	        }
        }
    }
}
