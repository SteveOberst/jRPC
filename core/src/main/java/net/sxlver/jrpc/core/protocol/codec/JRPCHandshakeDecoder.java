package net.sxlver.jrpc.core.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.sxlver.jrpc.core.LogProvider;
import net.sxlver.jrpc.core.protocol.MessageType;
import net.sxlver.jrpc.core.protocol.ProtocolInformationProvider;
import net.sxlver.jrpc.core.protocol.ProtocolVersion;
import net.sxlver.jrpc.core.protocol.impl.JRPCClientHandshakeMessage;
import net.sxlver.jrpc.core.protocol.packet.PacketDataSerializer;

import java.util.List;

public class JRPCHandshakeDecoder<T extends ProtocolInformationProvider & LogProvider> extends ByteToMessageDecoder {

    private final T provider;

    public JRPCHandshakeDecoder(final T provider) {
        this.provider = provider;
    }

    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) throws Exception {
        if(MessageType.of(in.readInt()) != MessageType.HANDSHAKE) {
            in.resetReaderIndex();
            return;
        }

        final int versionNumber = in.readInt();
        final ProtocolVersion version = ProtocolVersion.getByVersionNumber(versionNumber);
        if(version != provider.getProtocolVersion()) {
            final String message = "Message Protocol version mismatch whilst authenticating! Received: {} Current Version: {}";
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

        final JRPCClientHandshakeMessage message = PacketDataSerializer.deserialize(data, JRPCClientHandshakeMessage.class);
        out.add(message);
    }
}
