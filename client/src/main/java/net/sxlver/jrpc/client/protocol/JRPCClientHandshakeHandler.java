package net.sxlver.jrpc.client.protocol;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.NonNull;
import net.sxlver.jrpc.client.JRPCClient;
import net.sxlver.jrpc.client.protocol.codec.JRPCClientMessageEncoder;
import net.sxlver.jrpc.core.protocol.MessageType;
import net.sxlver.jrpc.core.protocol.impl.JRPCClientHandshakeMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.packet.HandshakeStatusPacket;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;
import net.sxlver.jrpc.core.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class JRPCClientHandshakeHandler extends SimpleChannelInboundHandler<JRPCMessage> {

    private final JRPCClient client;
    private Channel channel;

    public JRPCClientHandshakeHandler(final @NonNull JRPCClient client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext context, final JRPCMessage message) {
        try {
            final byte[] data = message.data();
            final HandshakeStatusPacket packet = PacketDataSerializer.deserialize(data, HandshakeStatusPacket.class);
            client.getNetHandler().setHandshaked(true);
            if (!packet.isSuccess()) {
                client.getLogger().fatal("Error authenticating with the server. Error: '{}' [Auth key: {}]", packet.getErrorMessage(), StringUtil.cypherString(client.getConfig().getAuthenticationToken()));
                client.close();
                return;
            }

            client.getLogger().info("Successfully authenticated with the server.");
            finish();
        } catch (final Exception exception) {
            client.getLogger().warn("Received data before successful authentication. [Source: {}] [Target: {}] [Message Type: {}] [Conversation ID: {}] [Content Length: {}]",
                    message.source(), message.target(), message.targetType(), message.conversationId(), message.data().length
            );
        }
    }

    public void finish() {
        channel.pipeline().remove("handshake_handler");
        channel.pipeline().remove("handshake_encoder");
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext context) {
        this.channel = context.channel();
        client.getLogger().debugFine("Handshake Channel opened.");
        context.fireChannelActive();
    }

    public void handshake(final @NonNull JRPCClientHandshakeMessage message) {
        channel.writeAndFlush(message);
        client.getLogger().debugFine("Handshake sent.");
    }
}
