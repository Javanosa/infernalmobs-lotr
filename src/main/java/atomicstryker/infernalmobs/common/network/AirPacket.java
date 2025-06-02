package atomicstryker.infernalmobs.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;

public class AirPacket implements IPacket
{
    
    private int air;
    private boolean keep;
    
    public AirPacket() {}
    
    public AirPacket(int a, boolean k)
    {
        air = a;
        keep = k;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
    	
        bytes.writeShort(air);
        bytes.writeBoolean(keep);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
    	
        air = bytes.readShort();
        keep = bytes.readBoolean();
        InfernalMobsCore.proxy.onAirPacket(air, keep);
    }

}
