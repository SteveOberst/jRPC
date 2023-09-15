package net.sxlver.jrpc.core.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.sxlver.jrpc.core.protocol.impl.JRPCClientAuthMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.packet.PacketDataSerializer;

public abstract class JRPCAuthEncoder extends MessageToByteEncoder<JRPCClientAuthMessage> {
    @Override
    protected void encode(final ChannelHandlerContext context, final JRPCClientAuthMessage message, ByteBuf out) throws Exception {
        final byte[] data = PacketDataSerializer.serialize(message);
        out.writeInt(getVersionNumber())
                .writeInt(data.length)
                .writeBytes(data);
    }

    protected abstract int getVersionNumber();
}
