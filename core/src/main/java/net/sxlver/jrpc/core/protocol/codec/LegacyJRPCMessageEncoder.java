package net.sxlver.jrpc.core.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.sxlver.jrpc.core.protocol.MessageType;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;

public abstract class LegacyJRPCMessageEncoder extends MessageToByteEncoder<JRPCMessage> {
    @Override
    protected void encode(final ChannelHandlerContext context, final JRPCMessage message, ByteBuf out) throws Exception {
        final byte[] data = PacketDataSerializer.serialize(message);
        out.writeInt(MessageType.MESSAGE.getId())
                .writeInt(getVersionNumber())
                .writeInt(data.length)
                .writeBytes(data);
    }

    protected abstract int getVersionNumber();
}
