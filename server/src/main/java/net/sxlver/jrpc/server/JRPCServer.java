package net.sxlver.jrpc.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.sxlver.jrpc.core.InternalLogger;
import net.sxlver.jrpc.core.LogProvider;
import net.sxlver.jrpc.core.config.ConfigurationManager;
import net.sxlver.jrpc.core.config.DataFolderProvider;
import net.sxlver.jrpc.core.protocol.*;
import net.sxlver.jrpc.core.protocol.Errors;
import net.sxlver.jrpc.core.protocol.codec.JRPCMessageDecoder;
import net.sxlver.jrpc.core.protocol.impl.JRPCHandshake;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessageBuilder;
import net.sxlver.jrpc.core.protocol.model.JRPCClientInformation;
import net.sxlver.jrpc.core.protocol.packet.ErrorInformationPacket;
import net.sxlver.jrpc.core.protocol.packet.HandshakeStatusPacket;
import net.sxlver.jrpc.core.protocol.packet.UpdateClientStatusPacket;
import net.sxlver.jrpc.core.serialization.CentralGson;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;
import net.sxlver.jrpc.core.util.StringUtil;
import net.sxlver.jrpc.server.config.JRPCServerConfig;
import net.sxlver.jrpc.server.model.JRPCClientInstance;
import net.sxlver.jrpc.server.protocol.*;
import net.sxlver.jrpc.server.protocol.codec.JRPCServerMessageEncoder;
import net.sxlver.jrpc.server.selector.TargetSelector;
import net.sxlver.jrpc.server.selector.TargetSelectors;
import net.sxlver.jrpc.server.util.LazyInitVar;
import net.sxlver.jrpc.server.util.Loadbalancer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class JRPCServer implements DataFolderProvider, ProtocolInformationProvider, LogProvider, DataSource {

    public static final ProtocolVersion PROTOCOL_VERSION = ProtocolVersion.V0_1;
    private final InternalLogger logger;

    private Collection<ServerMessageHandler<Packet>> defaultServerMessageHandler;

    private static final LazyInitVar<NioEventLoopGroup> nioLazyVar = new LazyInitVar<>(()
            -> new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty JRPC IO #%d").setDaemon(true).build()));
    private static final LazyInitVar<EpollEventLoopGroup> epollLazyVar = new LazyInitVar<>(()
            -> new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll JRPC IO #%d").setDaemon(true).build()));

    private final ConfigurationManager configurationManager;
    private final JRPCServerConfig config;
    private CentralGson centralGson;

    private ChannelFuture listeningChannel;
    private EventLoopGroup loopGroup;
    private SocketAddress localAddress;

    private final Set<JRPCClientInstance> connected = Sets.newSetFromMap(new ConcurrentHashMap<>());

    public JRPCServer() {
        this.logger = new InternalLogger(getClass());
        this.configurationManager = new ConfigurationManager(this);
        this.config = configurationManager.getConfig(JRPCServerConfig.class, true);
        this.localAddress = new InetSocketAddress("localhost", config.getPort());
        this.centralGson = CentralGson.PROTOCOL_INSTANCE;
        this.logger.setLogLevel(config.getLoggingLevel());
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> logger.fatal("An unexpected Exception occurred. {}", ExceptionUtils.getStackTrace(throwable)));
        this.defaultServerMessageHandler = DefaultHandlerRegistry.getMessageHandlers();
    }

    public void run() throws Exception {
        Class<? extends ServerSocketChannel> channelClass;
        LazyInitVar<? extends MultithreadEventLoopGroup> loopGroup;
        synchronized (this) {
            channelClass = Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
            loopGroup = Epoll.isAvailable() ? epollLazyVar : nioLazyVar;
            logger.info("Using {} for network connections.", channelClass);

            this.listeningChannel = new ServerBootstrap()
                    .channel(channelClass)
                    .childHandler(new JRPCChannelInitializer())
                    .group(this.loopGroup = loopGroup.get()
                    ).localAddress(localAddress).bind().syncUninterruptibly();

            logger.info("Running server on {}", localAddress);
            listeningChannel.channel().closeFuture().sync();
        }
    }

    public void close() {
        listeningChannel.channel().close().syncUninterruptibly();
        connected.stream().map(JRPCClientInstance::getNetHandler).forEach(JRPCServerChannelHandler::shutdown);

        try {
            logger.info("shutting down event loop group");
            loopGroup.shutdownGracefully().sync();
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
        }
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

    public ChannelFuture getChannel() {
        return listeningChannel;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    @Override
    public boolean isAllowVersionMismatch() {
        return config.isAllowVersionMismatch();
    }

    public JRPCServerConfig getConfig() {
        return config;
    }

    public InternalLogger getLogger() {
        return logger;
    }

    public Gson getGson() {
        return centralGson.getGson();
    }

    @Override
    public String getSource() {
        return config.getServerId();
    }

    public Collection<JRPCClientInformation> getRegisteredClients() {
        return connected.stream().map(JRPCClientInstance::getInformation).collect(Collectors.toList());
    }

    public Collection<JRPCClientInstance> getRegisteredClientsRaw() {
        return connected;
    }

    public void onReceive(final JRPCServerChannelHandler source, JRPCMessage sourceMessage, final Packet packet) {
        for (final ServerMessageHandler<Packet> handler : defaultServerMessageHandler) {
            if(!handler.getTarget().isAssignableFrom(packet.getClass())) continue;
            final ServerMessageContext<Packet> context = new ServerMessageContext<>(packet, source, sourceMessage);
            handler.handle(this, context);
        }
    }

    public HandshakeStatusPacket handshake(final ChannelPipeline pipeline, final JRPCHandshake handshake) {
        return tryHandshake(pipeline, handshake);
    }

    private HandshakeStatusPacket tryHandshake(final @NonNull ChannelPipeline pipeline, final @NonNull JRPCHandshake handshake) {
        final JRPCServerChannelHandler channelHandler = (JRPCServerChannelHandler) pipeline.get("message_handler");
        if(!verifyHandshake(handshake)) {
            logger.warn("Received invalid handshake. [Client: {}] [Token: {}]", handshake.getUniqueId(), StringUtil.cypherString(handshake.getToken()));
            return new HandshakeStatusPacket(false, "Received invalid handshake packet.");
        }

        if(clientExists(handshake.getUniqueId())) {
            logger.warn("Client {} tried to open connection but is already authenticated. Closing connection...", handshake.getUniqueId());
            return new HandshakeStatusPacket(false, String.format("Client with unique id '%s' is already authenticated.", handshake.getUniqueId()));
        }

        final boolean success = channelHandler.onHandshakeSuccess(handshake);
        return new HandshakeStatusPacket(success);
    }

    public boolean clientExists(final String uniqueId) {
        return !selectDirect(uniqueId).isEmpty();
    }

    private boolean verifyHandshake(final JRPCHandshake handshake) {
        return handshake.getToken() != null && handshake.getType() != null && handshake.getUniqueId() != null;
    }

    public void forward(final @NonNull JRPCMessage message, final @NonNull JRPCServerChannelHandler invoker) {
        final Message.TargetType targetType = message.targetType();
        final TargetSelector targetSelector = TargetSelectors.getByTargetType(targetType);
        Collection<JRPCClientInstance> sendTo = targetSelector.select(message.target(), getRegisteredClientsRaw());

        final String target = (targetType == Message.TargetType.ALL || targetType == Message.TargetType.BROADCAST) ? "*" : message.target();
        if(sendTo.isEmpty() || sendTo.stream().noneMatch(Objects::nonNull)) {
            final JRPCMessage errorMessage = buildDirectResponse(new ErrorInformationPacket(Errors.ERR_NO_TARGET_FOUND, "No suitable target found."), message.source(), message.conversationId());
            invoker.write(errorMessage);
            logger.info("{} No suitable target found whilst forwarding message. [Type: {}] [Source: {}] [Target: {}]", "[MESSAGE FORWARD]", targetType, message.source(), target);
            return;
        }

        if(!config.isAllowSelfForward()) {
            sendTo.removeIf(jrpcClientInstance -> jrpcClientInstance.getUniqueId().equals(message.source()));
            if(targetType == Message.TargetType.DIRECT && sendTo.isEmpty()) {
                logger.warn("Client {} tried to forward a message to themselves but allow-self-forward is set to false.", target);
                final JRPCMessage errorMessage = buildDirectResponse(new ErrorInformationPacket(Errors.ERR_SELF_REFERENCE, "Clients are not allowed to directly reference themselves."), message.source(), message.conversationId());
                invoker.write(errorMessage);
                return;
            }
        }

        sendTo.stream().filter(Objects::nonNull).forEach(jrpcClientInstance -> jrpcClientInstance.getNetHandler().write(message));
        logForward(targetType, message.source(), target, message.data().length);
    }

    public Collection<JRPCClientInstance> selectDirect(final String uniqueId) {
        return TargetSelectors.TARGET_SELECTOR_DIRECT.select(uniqueId, getRegisteredClientsRaw());
    }

    private void logForward(final Message.TargetType targetType, final String source, final String target, final int dataLen) {
        logger.info("{} Forwarding Message of type {} [{} -> {}] [length: {}]","[MESSAGE FORWARD]" , targetType, source, target, dataLen);
    }

    public JRPCMessage buildDirectResponse(final @NonNull Packet packet, final @NonNull String target, final @NonNull ConversationUID uid) {
        return JRPCMessageBuilder.builder()
                .source(this)
                .targetType(Message.TargetType.DIRECT)
                .target(target)
                .conversationUid(uid)
                .data(PacketDataSerializer.serialize(packet))
                .build();
    }

    private class JRPCChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(final @NotNull SocketChannel channel) {
            try {
                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException exception) {
                logger.fatal("Error whilst setting TCP_NODELAY option: {}", ExceptionUtils.getStackTrace(exception));
            }
            channel.pipeline().addLast("frame_decoder", new LengthFieldBasedFrameDecoder(Message.MAX_PACKET_LENGTH, 0, 4,0,4));
            channel.pipeline().addLast("message_decoder", new JRPCMessageDecoder<>(JRPCServer.this));
            //channel.pipeline().addLast("timeout_handler", new ReadTimeoutHandler(config.getReadTimeout(), TimeUnit.SECONDS));
            channel.pipeline().addLast("handshake_handler", new JRPCServerHandshakeHandler(JRPCServer.this));
            channel.pipeline().addLast("message_handler", new JRPCServerChannelHandler(JRPCServer.this));
            channel.pipeline().addLast("message_encoder", new JRPCServerMessageEncoder(JRPCServer.this.getProtocolVersion().getVersionNumber()));
        }
    }

    public void addConnected(final @NonNull JRPCClientInstance instance) {
        updateClientStatus(instance, UpdateClientStatusPacket.Operation.REGISTER);
        connected.add(instance);
    }


    public void removeConnected(final @NonNull JRPCClientInstance instance) {
        updateClientStatus(instance, UpdateClientStatusPacket.Operation.UNREGISTER);
        connected.remove(instance);
    }

    private void updateClientStatus(final JRPCClientInstance client, final UpdateClientStatusPacket.Operation operation) {
        final UpdateClientStatusPacket packet = new UpdateClientStatusPacket(operation, client.getInformation());
        // inform connected client instances before adding the client to the cache
        // in order to not inform the newly registered client of its own registration
        for (final JRPCClientInstance jrpcClientInstance : connected) {
            jrpcClientInstance.getNetHandler().write(packet);
        }
    }
}
