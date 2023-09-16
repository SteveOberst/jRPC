package net.sxlver.jrpc.client;

import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.sxlver.jrpc.client.config.JRPCClientConfig;
import net.sxlver.jrpc.client.protocol.JRPCClientHandshakeHandler;
import net.sxlver.jrpc.client.protocol.MessageReceiver;
import net.sxlver.jrpc.client.protocol.codec.JRPCClientHandshakeMessageEncoder;
import net.sxlver.jrpc.core.protocol.impl.JRPCClientHandshakeMessage;
import net.sxlver.jrpc.client.protocol.JRPCClientChannelHandler;
import net.sxlver.jrpc.client.protocol.codec.JRPCClientMessageEncoder;
import net.sxlver.jrpc.core.InternalLogger;
import net.sxlver.jrpc.core.LogProvider;
import net.sxlver.jrpc.core.config.ConfigurationManager;
import net.sxlver.jrpc.core.config.DataFolderProvider;
import net.sxlver.jrpc.core.protocol.*;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.codec.JRPCMessageDecoder;
import net.sxlver.jrpc.core.protocol.packet.PacketDataSerializer;
import net.sxlver.jrpc.core.serialization.CentralGson;
import net.sxlver.jrpc.core.util.StringUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JRPCClient implements DataFolderProvider, ProtocolInformationProvider, LogProvider, DataSource {

    public static final ProtocolVersion PROTOCOL_VERSION = ProtocolVersion.V0_1;
    private final ConfigurationManager configurationManager;
    private final JRPCClientConfig config;

    private final InternalLogger logger;
    private final CentralGson centralGson;

    private ChannelFuture connectedChannel;
    private EventLoopGroup loopGroup;
    private InetSocketAddress remoteAddress;

    private JRPCClientChannelHandler handler;

    private final Set<MessageReceiver<?>> messageReceivers = ConcurrentHashMap.newKeySet();

    public JRPCClient() {
        this.logger = new InternalLogger(getClass());
        this.centralGson = CentralGson.PROTOCOL_INSTANCE;
        this.configurationManager = new ConfigurationManager(this);
        this.config = configurationManager.getConfig(JRPCClientConfig.class, true);
    }

    @Nullable
    public ChannelFuture open() {
        final String host = config.getServerAddress();
        final int port = config.getServerPort();
        this.remoteAddress = new InetSocketAddress(host, port);
        loopGroup = new NioEventLoopGroup();
        logger.info("Opening socket...");
        try {
            final Bootstrap bootstrap = new Bootstrap()
                    .group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new JRPCChannelInitializer());

            logger.info("Attempting to connect to {}:{}", host, port);
            this.connectedChannel = bootstrap.connect(host, port).sync();
            logger.info("Successfully opened connection.", host, port);

            authenticate(new JRPCHandshake(config.getAuthenticationToken(), config.getUniqueId(), ""));

        } catch (final InterruptedException exception) {
            logger.fatal("A fatal error occurred whilst trying to open connection to server: {}", exception.getMessage());
            logger.info(ExceptionUtils.getStackTrace(exception));
        } finally {
            loopGroup.shutdownGracefully();
        }
        return this.connectedChannel;
    }

    public void shutdown() {
        if(connectedChannel != null) {
            connectedChannel.channel().closeFuture();
            connectedChannel = null;
        }

        try {
            logger.info("shutting down event loop group");
            loopGroup.shutdownGracefully().sync();
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private void authenticate(final @NonNull JRPCHandshake handshake) {
        logger.info("Attempting to handshake server {}:{}. [Auth Token: {}]", remoteAddress.getHostName(), remoteAddress.getPort(), StringUtil.cypherString(config.getAuthenticationToken()));
        final JRPCClientHandshakeMessage message = new JRPCClientHandshakeMessage(getSource(), PacketDataSerializer.serialize(handshake));
        connectedChannel.channel().pipeline().get("handshake_handler");
        final JRPCClientHandshakeHandler handshakeHandler = (JRPCClientHandshakeHandler) connectedChannel.channel().pipeline().get("handshake_handler");
        handshakeHandler.handshake(message);
        handler.awaitHandshakeResponse();
    }


    public void onChannelClose(final ChannelHandlerContext context) {
        logger.warn("Channel has been closed! Communication with the server will no longer be possible.");
        this.connectedChannel = null;
    }

    public void write(final @NonNull Packet packet, final @NonNull MessageTarget target, final @NonNull Class<? extends Packet> expectedResponse) {
        write(packet, target, expectedResponse, null);
    }

    public void write(final @NonNull Packet packet, final @NonNull MessageTarget target, final @NonNull Class<? extends Packet> expectedResponse, final ConversationUID conversationUID) {
        if(!isChannelOpen()) throw new IllegalStateException("Channel not open");
        handler.write(packet, target, expectedResponse, conversationUID);
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> void registerMessageReceiver(final @NonNull MessageReceiver<T> receive) {
        final Optional<MessageReceiver<T>> registered = messageReceivers.stream()
                .filter(target -> target.getClass() == receive.getClass())
                .map(messageReceiver -> (MessageReceiver<T>) messageReceiver).findAny();

        registered.ifPresent(target -> unregisterMessageReceiver((Class<? extends MessageReceiver<T>>) target.getClass()));
        messageReceivers.add(receive);
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> void unregisterMessageReceiver(final MessageReceiver<T> receiver) {
        unregisterMessageReceiver((Class<? extends MessageReceiver<T>>) receiver.getClass());
    }

    public <T extends Packet> void unregisterMessageReceiver(final @NonNull Class<? extends MessageReceiver<T>> cls) {
        messageReceivers.removeIf(receiver -> receiver.getClass() == cls);
    }

    @Override
    @SneakyThrows
    public String getDataFolder() {
        return new File(getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI().getPath())
                .getParent();
    }

    public JRPCClientConfig getConfig() {
        return config;
    }

    public InternalLogger getLogger() {
        return logger;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    @Override
    public boolean isAllowVersionMismatch() {
        return false;
    }

    public Gson getGson() {
        return centralGson.getGson();
    }

    @Override
    public String getSource() {
        return config.getUniqueId();
    }

    public Set<MessageReceiver<?>> getMessageReceivers() {
        return messageReceivers;
    }

    public JRPCClientChannelHandler getNetHandler() {
        return handler;
    }

    public ChannelFuture getConnectedChannel() {
        return connectedChannel;
    }

    private boolean isChannelOpen() {
        return connectedChannel != null;
    }

    public void publishMessage(final @NonNull JRPCMessage message) {
        for (MessageReceiver<?> messageReceiver : messageReceivers) {
            messageReceiver.onReceive(message.source(), message.target(), message.targetType(), message.conversationId(), message.data());
        }
    }

    private class JRPCChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(final @NotNull SocketChannel channel) {
            try {
                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException exception) {
                logger.fatal("Error whilst setting TCP_NODELAY option: {}", ExceptionUtils.getStackTrace(exception));
            }
            channel.pipeline().addLast("message_decoder", new JRPCMessageDecoder<>(JRPCClient.this));
            channel.pipeline().addLast("handshake_handler", new JRPCClientHandshakeHandler(JRPCClient.this));
            channel.pipeline().addLast("message_handler", JRPCClient.this.handler = new JRPCClientChannelHandler(JRPCClient.this));
            channel.pipeline().addLast("message_encoder", new JRPCClientMessageEncoder(JRPCClient.PROTOCOL_VERSION.getVersionNumber()));
            channel.pipeline().addLast("auth_encoder", new JRPCClientHandshakeMessageEncoder(JRPCClient.PROTOCOL_VERSION.getVersionNumber()));
        }
    }
}
