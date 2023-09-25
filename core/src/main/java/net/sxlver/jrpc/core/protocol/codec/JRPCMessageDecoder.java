package net.sxlver.jrpc.core.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.stream.ChunkedStream;
import net.sxlver.jrpc.core.LogProvider;
import net.sxlver.jrpc.core.protocol.MessageType;
import net.sxlver.jrpc.core.protocol.impl.HeaderData;
import net.sxlver.jrpc.core.protocol.impl.JRPCClientHandshakeMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.ProtocolInformationProvider;
import net.sxlver.jrpc.core.protocol.ProtocolVersion;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;

import java.util.List;

public class JRPCMessageDecoder<T extends ProtocolInformationProvider & LogProvider> extends ByteToMessageDecoder {
    private final T provider;

    public JRPCMessageDecoder(final T provider) {
        this.provider = provider;
    }

    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        final int length = in.readableBytes();

        if(!(length > 0)) {
            provider.getLogger().fatal("Malformed packet received from {}. Closing connection.", context.channel().remoteAddress());
            context.close();
            return;
        }

        final byte[] data = new byte[length];
        in.readBytes(data);

        final HeaderData header = PacketDataSerializer.deserialize(data, HeaderData.class);
        if(header == null) {
            provider.getLogger().warn("Error whilst deserializing header. Closing connection to {}.", context.channel().remoteAddress());
            return;
        }

        final int versionNumber = header.getProtocolVersion();
        final ProtocolVersion version = ProtocolVersion.getByVersionNumber(versionNumber);
        if(version != provider.getProtocolVersion()) {
            final String message = "Message Protocol version mismatch! Received: {} Current Version: {}";
            if(!provider.isAllowVersionMismatch()) {
                provider.getLogger().warn(message, version, provider.getProtocolVersion());
                in.resetReaderIndex();
                return;
            }else {
                provider.getLogger().debug(message, version, provider.getProtocolVersion());
            }
        }

        final JRPCMessage message;
        if(header.getMessageType() == MessageType.HANDSHAKE.getId()) {
            message = PacketDataSerializer.deserialize(data, JRPCClientHandshakeMessage.class);
        }else if (header.getMessageType() == MessageType.MESSAGE.getId()) {
            message = PacketDataSerializer.deserialize(data, JRPCMessage.class);
        }else {
            provider.getLogger().warn("Invalid message format. Closing connection to {}", context.channel().remoteAddress());
            context.close();
            return;
        }
        out.add(message);
    }
}
