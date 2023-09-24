package net.sxlver.jrpc.core.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.sxlver.jrpc.core.LogProvider;
import net.sxlver.jrpc.core.protocol.MessageType;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.ProtocolInformationProvider;
import net.sxlver.jrpc.core.protocol.ProtocolVersion;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;

import java.util.List;


public class LegacyJRPCMessageDecoder <T extends ProtocolInformationProvider & LogProvider> extends ByteToMessageDecoder {
    private final T provider;

    public LegacyJRPCMessageDecoder(final T provider) {
        this.provider = provider;
    }

    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        if(MessageType.of(in.readInt()) != MessageType.MESSAGE) {
            in.resetReaderIndex();
            return;
        }

        final int versionNumber = in.readInt();
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

        final int length = in.readInt();
        final byte[] data = new byte[length];
        in.readBytes(data);

        final JRPCMessage message = PacketDataSerializer.deserialize(data, JRPCMessage.class);
        out.add(message);
    }
}
