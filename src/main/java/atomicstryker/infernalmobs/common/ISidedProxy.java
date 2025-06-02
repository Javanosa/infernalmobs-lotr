package atomicstryker.infernalmobs.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;

public interface ISidedProxy
{
    public void preInit();
    
    public void load();
    
    public Map<EntityLivingBase, MobModifier> getRareMobs(boolean client);
    
    public void onHealthPacketForClient(String stringData, int entID, float health, float maxhealth);
    
    public void onKnockBackPacket(float xv, float zv);
    
    public void onMobModsPacketToClient(String stringData, int entID);
    
    public void onVelocityPacket(float xv, float yv, float zv);

    public void onAirPacket(int air, boolean keep);
}
