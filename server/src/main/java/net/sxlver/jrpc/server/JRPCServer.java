package net.sxlver.jrpc.server;

import com.google.common.collect.Lists;
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
import lombok.SneakyThrows;
import net.sxlver.jrpc.core.InternalLogger;
import net.sxlver.jrpc.core.LogProvider;
import net.sxlver.jrpc.core.protocol.DataSource;
import net.sxlver.jrpc.core.protocol.JRPCHandshake;
import net.sxlver.jrpc.core.protocol.ProtocolInformationProvider;
import net.sxlver.jrpc.core.protocol.ProtocolVersion;
import net.sxlver.jrpc.core.config.ConfigurationManager;
import net.sxlver.jrpc.core.config.DataFolderProvider;
import net.sxlver.jrpc.core.protocol.codec.JRPCHandshakeDecoder;
import net.sxlver.jrpc.core.serialization.CentralGson;
import net.sxlver.jrpc.server.config.JRPCServerConfig;
import net.sxlver.jrpc.core.protocol.codec.JRPCMessageDecoder;
import net.sxlver.jrpc.server.model.JRPCClientInstance;
import net.sxlver.jrpc.server.protocol.JRPCServerChannelHandler;
import net.sxlver.jrpc.server.protocol.JRPCServerHandshakeHandler;
import net.sxlver.jrpc.server.util.LazyInitVar;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;

public class JRPCServer implements DataFolderProvider, ProtocolInformationProvider, LogProvider, DataSource {

    public static final ProtocolVersion PROTOCOL_VERSION = ProtocolVersion.V0_1;
    private final InternalLogger logger;

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

    private final List<JRPCClientInstance> connected = Collections.synchronizedList(Lists.newArrayList());

    public JRPCServer() {
        this.logger = new InternalLogger(getClass());
        this.configurationManager = new ConfigurationManager(this);
        this.config = configurationManager.getConfig(JRPCServerConfig.class, true);
        this.localAddress = new InetSocketAddress("localhost", config.getPort());
        this.centralGson = CentralGson.PROTOCOL_INSTANCE;
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            logger.fatal("An unexpected Exception occurred. {}", ExceptionUtils.getStackTrace(throwable));
        });
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

            listeningChannel.channel().closeFuture().sync();
        }
    }

    public void shutdown() {
        listeningChannel.channel().closeFuture();
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

    public boolean handshake(final SocketAddress remoteAddress, final JRPCHandshake handshake) {
        final JRPCClientInstance client = getBySource(remoteAddress);
        if(client == null) return false;
        return client.getNetHandler().handshake(handshake);
    }

    private class JRPCChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(final @NotNull SocketChannel channel) {
            try {
                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException exception) {
                logger.fatal("Error whilst setting TCP_NODELAY option: {}", ExceptionUtils.getStackTrace(exception));
            }
            channel.pipeline().addLast("message_decoder", new JRPCMessageDecoder<>(JRPCServer.this));
            channel.pipeline().addLast("auth_decoder", new JRPCHandshakeDecoder<>(JRPCServer.this));
            channel.pipeline().addLast("handshake_handler", new JRPCServerHandshakeHandler(JRPCServer.this));
            channel.pipeline().addLast("message_handler", new JRPCServerChannelHandler(JRPCServer.this));
        }
    }

    public JRPCClientInstance getBySource(final SocketAddress address) {
        return connected.stream().filter(client -> client.getNetHandler().getRemoteAddress().equals(address)).findFirst().orElse(null);
    }

    public void addConnected(final JRPCClientInstance instance) {
        connected.add(instance);
    }

    public void removeConnected(final JRPCClientInstance instance) {
        connected.remove(instance);
    }
}
