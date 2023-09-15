package net.sxlver.jrpc.core.protocol.decoder;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.sxlver.jrpc.core.LogProvider;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.ProtocolInformationProvider;
import net.sxlver.jrpc.core.protocol.ProtocolVersion;
import net.sxlver.jrpc.core.serialization.CentralGson;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class JRPCMessageDecoder<T extends ProtocolInformationProvider & LogProvider> extends ByteToMessageDecoder {
    private final T provider;

    public JRPCMessageDecoder(final T provider) {
        this.provider = provider;
    }

    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) throws Exception {
        final int versionNumber = in.readInt();
        final ProtocolVersion version = ProtocolVersion.getByVersionNumber(versionNumber);
        if(version != provider.getProtocolVersion()) {
            final String message = "Message Protocol version mismatch! Received: {} Current Version: {}";
            if(!provider.isAllowVersionMismatch()) {
                provider.getLogger().warn(message, version, provider.getProtocolVersion());
                return;
            }else {
                provider.getLogger().debug(message, version, provider.getProtocolVersion());
            }
        }

        final int length = in.readInt();
        final byte[] data = new byte[length];
        in.readBytes(data);

        final String json = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
        final Gson gson = CentralGson.PROTOCOL_INSTANCE.getGson();
        final JRPCMessage message = gson.fromJson(json, JRPCMessage.class);
        out.add(message);
    }
}
