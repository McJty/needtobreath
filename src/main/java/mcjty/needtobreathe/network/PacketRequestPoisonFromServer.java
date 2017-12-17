package mcjty.needtobreathe.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.needtobreathe.data.CleanAirManager;
import mcjty.needtobreathe.data.DimensionData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestPoisonFromServer implements IMessage {

    private BlockPos pos;

    public PacketRequestPoisonFromServer() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
    }

    public PacketRequestPoisonFromServer(BlockPos pos) {
        this.pos = pos;
    }

    public static class Handler implements IMessageHandler<PacketRequestPoisonFromServer, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestPoisonFromServer message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestPoisonFromServer message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            DimensionData data = CleanAirManager.getManager().getDimensionData(player.getEntityWorld().provider.getDimension());
            int poison;
            if (data != null) {
                poison = data.getPoison(message.pos);
            } else {
                poison = 0;
            }
            PacketPoisonFromServer msg = new PacketPoisonFromServer(poison);
            NTBMessages.INSTANCE.sendTo(msg, player);
        }

    }
}