package net.sxlver.jrpc.client;

import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.sxlver.jrpc.client.config.JRPCClientConfiguration;
import net.sxlver.jrpc.client.config.JRPCDefaultConfiguration;
import net.sxlver.jrpc.client.protocol.*;
import net.sxlver.jrpc.client.protocol.codec.JRPCClientHandshakeEncoder;
import net.sxlver.jrpc.client.protocol.codec.JRPCClientMessageEncoder;
import net.sxlver.jrpc.core.InternalLogger;
import net.sxlver.jrpc.core.LogProvider;
import net.sxlver.jrpc.core.config.ConfigurationManager;
import net.sxlver.jrpc.core.config.DataFolderProvider;
import net.sxlver.jrpc.core.protocol.*;
import net.sxlver.jrpc.core.protocol.codec.JRPCMessageDecoder;
import net.sxlver.jrpc.core.protocol.impl.JRPCClientHandshakeMessage;
import net.sxlver.jrpc.core.protocol.impl.JRPCHandshake;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.protocol.packet.ClusterInformationConversation;
import net.sxlver.jrpc.core.serialization.CentralGson;
import net.sxlver.jrpc.core.serialization.PacketDataSerializer;
import net.sxlver.jrpc.core.util.Callback;
import net.sxlver.jrpc.core.util.StringUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

public class JRPCClient implements DataFolderProvider, ProtocolInformationProvider, LogProvider, DataSource {

    public static final ProtocolVersion PROTOCOL_VERSION = ProtocolVersion.V0_1;
    private final ConfigurationManager configurationManager;
    private final JRPCClientConfiguration config;

    private final InternalLogger logger;
    private final CentralGson centralGson;

    private ChannelFuture connectedChannel;
    private EventLoopGroup loopGroup;
    private InetSocketAddress remoteAddress;

    private JRPCClientChannelHandler handler;

    private final Set<RawDataReceiver> dataReceivers = ConcurrentHashMap.newKeySet();

    private final String dataFolder;

    /**
     * Instantiates a new Jrpc client.
     */
    public JRPCClient() {
        this(null, true);
    }

    /**
     * Instantiates a new Jrpc client.
     *
     * @param dataFolder                  the data folder in which config files and logs will be saved
     * @param setUncaughtExceptionHandler whether to set an exception handler for the current thread context
     */
    public JRPCClient(final String dataFolder, final boolean setUncaughtExceptionHandler) {
        this.dataFolder = dataFolder;
        this.configurationManager = new ConfigurationManager(this);
        this.config = configurationManager.getConfig(JRPCDefaultConfiguration.class, true);
        this.logger = new InternalLogger(getClass(), Path.of(getStorage(), "logs").toFile());
        this.centralGson = CentralGson.PROTOCOL_INSTANCE;
        this.logger.setLogLevel(config.getLoggingLevel());
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        if(setUncaughtExceptionHandler) {
            Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> logger.fatal("An unexpected Exception occurred. {}", ExceptionUtils.getStackTrace(throwable)));
        }
    }

    public JRPCClient(final JRPCClientConfiguration config, final String dataFolder, final boolean setUncaughtExceptionHandler) {
        this.dataFolder = dataFolder;
        this.configurationManager = new ConfigurationManager(this);
        this.config = config;
        this.logger = new InternalLogger(getClass(), Path.of(getStorage(), "logs").toFile());
        this.centralGson = CentralGson.PROTOCOL_INSTANCE;
        this.logger.setLogLevel(config.getLoggingLevel());
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        if(setUncaughtExceptionHandler) {
            Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> logger.fatal("An unexpected Exception occurred. {}", ExceptionUtils.getStackTrace(throwable)));
        }
    }

    @Nullable
    @Blocking
    public ChannelFuture open() {
        final String host = config.getServerAddress();
        final int port = config.getServerPort();
        this.remoteAddress = new InetSocketAddress(host, port);
        this.loopGroup = new NioEventLoopGroup();
        logger.info("Opening socket...");
        try {
            final Bootstrap bootstrap = new Bootstrap()
                    .group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new JRPCChannelInitializer());

            logger.info("Attempting to connect to {}:{}", host, port);
            this.connectedChannel = bootstrap.connect(host, port);
            connectedChannel.await(5, TimeUnit.SECONDS);

            if(connectedChannel.channel().isOpen()) {
                logger.info("Successfully opened connection.");
                authenticate(new JRPCHandshake(config.getAuthenticationToken(), config.getUniqueId(), config.getType()));
            }else {
                logger.warn("Could not establish connection to the server.");
                if(config.isAutoReconnect()) {
                    scheduleReconnect();
                }
            }
        } catch (final InterruptedException exception) {
            logger.fatal("A fatal error occurred whilst trying to open connection to server: {}", exception.getMessage());
            logger.info(ExceptionUtils.getStackTrace(exception));
            loopGroup.shutdownGracefully();
        }
        return this.connectedChannel;
    }

    /**
     * Closes the connection to the server, initiates a shutdown of the loop group and
     * waits until writes have been completed.
     *
     * <p>Always invoke this method when the application shuts down to ensure proper cleanup.
     */
    @Blocking
    @MustBeInvokedByOverriders
    public void close() {
        if(this.connectedChannel != null) {
            connectedChannel.channel().close().syncUninterruptibly();
            this.connectedChannel = null;
        }

        try {
            logger.info("shutting down event loop group");
            loopGroup.shutdownGracefully().sync();
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Get clients in the network of the specified type.
     *
     * @param type             the type
     * @param responseCallback the response callback
     */
    @NonBlocking
    public void getServersOfType(final String type, final Callback<ClusterInformationConversation.Response> responseCallback) {
        requestClusterInformation(Message.TargetType.TYPE, type, responseCallback);
    }

    /**
     * Let the load balancer pick a server of the specified type
     *
     * @param type             the type
     * @param responseCallback the response callback
     */
    @NonBlocking
    public void getLoadBalancedServer(final String type, final Callback<ClusterInformationConversation.Response> responseCallback) {
        requestClusterInformation(Message.TargetType.LOAD_BALANCED, type, responseCallback);
    }

    /**
     * Requests information about every server in the network.
     *
     * @param responseCallback the response callback
     */
    @NonBlocking
    public void getAllServers(final Callback<ClusterInformationConversation.Response> responseCallback) {
        requestClusterInformation(Message.TargetType.ALL, "", responseCallback);
    }

    private void requestClusterInformation(final Message.TargetType target,
                                           final String identifier,
                                           final Callback<ClusterInformationConversation.Response> responseCallback) {

        final ClusterInformationConversation.Request request = buildClusterInformationRequest(target, identifier);
        final MessageTarget messageTarget = new MessageTarget(Message.TargetType.SERVER, "");
        publish(request, messageTarget, ClusterInformationConversation.Response.class, null)
                .setResponseHandler(new ClusterInformationResponseHandler(responseCallback))
                .overrideHandlers();
    }

    private ClusterInformationConversation.Request buildClusterInformationRequest(final Message.TargetType type, final String identifier) {
        return new ClusterInformationConversation.Request(type, identifier);
    }

    /**
     * On channel close.
     *
     * @param context the context
     */
    public void onChannelClose(final ChannelHandlerContext context) {
        logger.warn("Channel has been closed! Communication with the server will no longer be possible.");
        if(config.isAutoReconnect()) {
            scheduleReconnect();
            if(config.isQueueMessages()) {
                logger.info("queue-messages is enabled, messages sent from now on will be queued until the connection has been re-established or the timeout was hit.");
            }
        }
    }

    private void scheduleReconnect() {
        logger.info("Scheduling reconnect for {}s", config.getReconnectInterval());
        CompletableFuture.runAsync(this::open, CompletableFuture.delayedExecutor(config.getReconnectInterval(), TimeUnit.SECONDS));
        final Channel channel = connectedChannel.channel();
        if(channel.pipeline().get("handshake_handler") == null && channel.pipeline().get("message_handler") != null) {
            channel.pipeline().addBefore("message_handler", "handshake_handler",new JRPCClientHandshakeHandler(JRPCClient.this));
        }
        if(channel.pipeline().get("handshake_encoder") == null && channel.pipeline().get("message_encoder") != null) {
            channel.pipeline().addAfter("message_encoder", "handshake_encoder",new JRPCClientHandshakeEncoder(JRPCClient.PROTOCOL_VERSION.getVersionNumber()));
        }
    }

    /**
     * Publish a message to the client(s) matching the MessageTarget provided.
     *
     * @param <TRequest>  the type parameter
     * @param <TResponse> the type parameter
     * @param packet      the packet
     * @param target      the target
     * @return the {@link Conversation} object representing the current request - response conversation
     */
    @NonBlocking
    public <TRequest extends Packet, TResponse extends Packet>
    Conversation<TRequest, TResponse> publish(final @NonNull TRequest packet,
                                              final @NonNull MessageTarget target) {

        return publish(packet, target, null);
    }

    /**
     * Publish a message to the client(s) matching the MessageTarget provided.
     *
     * @param <TRequest>       the type parameter
     * @param <TResponse>      the type parameter
     * @param packet           the packet
     * @param target           the target
     * @param expectedResponse the expected response
     * @return the {@link Conversation} object representing the current request - response conversation
     */
    @NonBlocking
    public <TRequest extends Packet, TResponse extends Packet>
    Conversation<TRequest, TResponse> publish(final @NonNull TRequest packet,
                                              final @NonNull MessageTarget target,
                                              final @Nullable Class<TResponse> expectedResponse) {

        return publish(packet, target, expectedResponse, null);
    }

    /**
     * Publish a message to the client(s) matching the MessageTarget provided.
     *
     * @param <TRequest>       the type parameter
     * @param <TResponse>      the type parameter
     * @param packet           the packet
     * @param target           the target
     * @param expectedResponse the expected response
     * @param conversationUID  the conversation uid
     * @return the {@link Conversation} object representing the current request - response conversation
     */
    @NonBlocking
    public <TRequest extends Packet, TResponse extends Packet>
    Conversation<TRequest, TResponse> publish(final @NonNull TRequest packet,
                                              final @NonNull MessageTarget target,
                                              final @Nullable Class<TResponse> expectedResponse,
                                              final @Nullable ConversationUID conversationUID) {

        return handler.write(packet, target, expectedResponse, conversationUID);
    }

    /**
     * Register a message receiver.
     *
     * @param receiver the receiver
     */
    public void registerMessageReceiver(final @NonNull RawDataReceiver receiver) {
        final Optional<RawDataReceiver> registered = dataReceivers.stream()
                .filter(target -> target.getClass() == receiver.getClass()).findAny();

        registered.ifPresent(target -> unregisterMessageReceiver(target.getClass()));
        dataReceivers.add(receiver);
    }

    /**
     * Unregister a message receiver.
     *
     * @param receiver the receiver
     */
    public void unregisterMessageReceiver(final RawDataReceiver receiver) {
        unregisterMessageReceiver( receiver.getClass());
    }

    /**
     * Unregister a message receiver.
     *
     * @param cls the cls
     */
    public void unregisterMessageReceiver(final @NonNull Class<? extends RawDataReceiver> cls) {
        dataReceivers.removeIf(receiver -> receiver.getClass() == cls);
    }

    @Override
    @SneakyThrows
    public String getStorage() {
        return this.dataFolder == null ? getRunningDir() : this.dataFolder;
    }

    /**
     * Gets running dir.
     *
     * @return the running dir
     */
    @SneakyThrows
    public String getRunningDir() {
        return new File(getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI().getPath())
                .getParent();
    }

    /**
     * Gets config.
     *
     * @return the config
     */
    public JRPCClientConfiguration getConfig() {
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
        return config.isAllowVersionMismatch();
    }

    /**
     * Gets the current gson instance.
     *
     * @return the gson
     */
    public Gson getGson() {
        return centralGson.getGson();
    }

    @Override
    public String getSource() {
        return config.getUniqueId();
    }

    /**
     * Gets message receivers.
     *
     * @return the message receivers
     */
    public Set<RawDataReceiver> getMessageReceivers() {
        return dataReceivers;
    }

    /**
     * Gets net handler.
     *
     * @return the net handler
     */
    public JRPCClientChannelHandler getNetHandler() {
        return handler;
    }

    /**
     * Gets connected channel.
     *
     * @return the connected channel
     */
    public ChannelFuture getConnectedChannel() {
        return connectedChannel;
    }

    private boolean isChannelOpen() {
        return connectedChannel != null;
    }

    private int getMessageQueueTimeout() {
        return config.getQueuedMessageTimeout() > 0 ? config.getQueuedMessageTimeout() : Integer.MAX_VALUE;
    }

    /**
     * Publish a message to the data receivers.
     *
     * @param message the message
     */
    public void publishToHandlers(final @NonNull JRPCMessage message) {
        for (final RawDataReceiver dataReceiver : dataReceivers) {
            try {
                dataReceiver.onReceive(message.source(), message.target(), message.targetType(), message.conversationId(), message.data());
            }catch(final Exception exception) {
                logger.fatal("Encountered error whilst publishing message to processor {}.", dataReceiver.getClass());
                logger.fatal(exception);
            }
        }
    }

    /**
     * Attempts to handshake the server and waits for its response.
     *
     * @param handshake the handshake object containing the required data for the handshake
     */
    @Blocking
    private void authenticate(final @NonNull JRPCHandshake handshake) {
        this.logger.info("Attempting to handshake server {}:{}. [Auth Token: {}]", remoteAddress.getHostName(), remoteAddress.getPort(), StringUtil.cypherString(config.getAuthenticationToken()));
        final JRPCClientHandshakeMessage message = new JRPCClientHandshakeMessage(getSource(), PacketDataSerializer.serialize(handshake));
        final JRPCClientHandshakeHandler handshakeHandler = (JRPCClientHandshakeHandler) connectedChannel.channel().pipeline().get("handshake_handler");
        handshakeHandler.handshake(message);
        handler.awaitHandshakeResponse();
    }

    private class JRPCChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(final @NotNull SocketChannel channel) {
            try {
                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException exception) {
                logger.fatal("Encountered error whilst settings options to channel: {}", ExceptionUtils.getStackTrace(exception));
            }
            channel.pipeline().addLast("frame_decoder", new LengthFieldBasedFrameDecoder(Message.MAX_PACKET_LENGTH, 0, 4, 0, 4));
            channel.pipeline().addLast("message_decoder", new JRPCMessageDecoder<>(JRPCClient.this));
            channel.pipeline().addLast("handshake_handler", new JRPCClientHandshakeHandler(JRPCClient.this));
            channel.pipeline().addLast("message_handler", JRPCClient.this.handler = new JRPCClientChannelHandler(JRPCClient.this));
            channel.pipeline().addLast("message_encoder", new JRPCClientMessageEncoder(JRPCClient.PROTOCOL_VERSION.getVersionNumber()));
            channel.pipeline().addLast("handshake_encoder", new JRPCClientHandshakeEncoder(JRPCClient.PROTOCOL_VERSION.getVersionNumber()));
        }
    }

    private static class ClusterInformationResponseHandler
            implements Conversation.ResponseHandler<ClusterInformationConversation.Request, ClusterInformationConversation.Response> {

        private final Callback<ClusterInformationConversation.Response> responseCallback;

        public ClusterInformationResponseHandler(final Callback<ClusterInformationConversation.Response> responseCallback) {
            this.responseCallback = responseCallback;
        }

        @Override
        public void onResponse(final @NonNull ClusterInformationConversation.Request request,
                               final @NonNull MessageContext<ClusterInformationConversation.Response> context) {

            Callback.Internal.complete(responseCallback, context.getResponse());
        }

        @Override
        public <T extends ErrorInformationHolder>
        void onExcept(final @NonNull Throwable throwable, final @NonNull T errorInformationHolder) {
            Callback.Internal.except(responseCallback, throwable);
        }

        @Override
        public void onTimeout(final @NonNull ClusterInformationConversation.Request request,
                              final @NonNull Set<MessageContext<ClusterInformationConversation.Response>> responses) {

            Callback.Internal.except(responseCallback, new TimeoutException("Server has taken too long to respond"));
        }
    }
}
