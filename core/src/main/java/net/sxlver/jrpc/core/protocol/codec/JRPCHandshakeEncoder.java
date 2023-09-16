package net.sxlver.jrpc.core.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.sxlver.jrpc.core.protocol.MessageType;
import net.sxlver.jrpc.core.protocol.impl.JRPCClientHandshakeMessage;
import net.sxlver.jrpc.core.protocol.packet.PacketDataSerializer;

public abstract class JRPCHandshakeEncoder extends MessageToByteEncoder<JRPCClientHandshakeMessage> {
    @Override
    protected void encode(final ChannelHandlerContext context, final JRPCClientHandshakeMessage message, ByteBuf out) throws Exception {
        final byte[] data = PacketDataSerializer.serialize(message);
        out.writeInt(MessageType.HANDSHAKE.getId())
                .writeInt(getVersionNumber())
                .writeInt(data.length)
                .writeBytes(data);
    }

    protected abstract int getVersionNumber();
}
