package org.ljc.server.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.ljc.common.util.SslUtil;
import org.ljc.server.handler.WebSocketServerHandler;
import org.ljc.server.registry.AgentRegistry;
import org.ljc.server.service.AuthenticationService;
import org.ljc.server.service.HttpProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Netty服务器配置
 * 启动WebSocket和HTTP服务器，支持SSL/TLS
 */
@Configuration
public class NettyServerConfig {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerConfig.class);

    private final ServerConfig serverConfig;
    private final AgentRegistry agentRegistry;
    private final HttpProxyService httpProxyService;
    private final AuthenticationService authService;

    public NettyServerConfig(ServerConfig serverConfig,
                             AgentRegistry agentRegistry,
                             HttpProxyService httpProxyService,
                             AuthenticationService authService) {
        this.serverConfig = serverConfig;
        this.agentRegistry = agentRegistry;
        this.httpProxyService = httpProxyService;
        this.authService = authService;
    }

    @Bean
    public CommandLineRunner startNettyServer() {
        return args -> {
            // 启动WebSocket服务器
            startWebSocketServer();
            // HTTP由Spring Boot处理
            logger.info("HTTP server will start with Spring Boot on port {}", serverConfig.getPort());
        };
    }

    /**
     * 启动WebSocket服务器
     */
    private void startWebSocketServer() {
        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(createInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

                Channel ch = b.bind(serverConfig.getWsPort()).sync().channel();
                String protocol = serverConfig.isSslEnabled() ? "wss" : "ws";
                logger.info("WebSocket server started on {}://0.0.0.0:{}/ws", protocol, serverConfig.getWsPort());

                ch.closeFuture().sync();
            } catch (Exception e) {
                logger.error("WebSocket server failed to start", e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }

    /**
     * 创建通道初始化器
     */
    private ChannelInitializer<io.netty.channel.socket.SocketChannel> createInitializer() {
        return new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
            @Override
            protected void initChannel(io.netty.channel.socket.SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                // 如果启用SSL，添加SSL处理器
                if (serverConfig.isSslEnabled()) {
                    if (!SslUtil.keyStoreExists(serverConfig.getKeyStorePath())) {
                        SslUtil.printSslHelp(serverConfig.getKeyStorePath(), serverConfig.getKeyStorePassword());
                        throw new RuntimeException("SSL enabled but keystore not found: " + serverConfig.getKeyStorePath());
                    }
                    SslContext sslContext = SslUtil.createServerSslContext(
                        serverConfig.getKeyStorePath(),
                        serverConfig.getKeyStorePassword());
                    SslHandler sslHandler = sslContext.newHandler(ch.alloc());
                    sslHandler.engine().setNeedClientAuth(false);
                    pipeline.addFirst("ssl", sslHandler);
                    logger.info("SSL/TLS enabled for WebSocket server");
                }

                // HTTP编解码器
                pipeline.addLast(new HttpServerCodec());

                // 聚合HTTP消息
                pipeline.addLast(new HttpObjectAggregator(65536));

                // 空闲检测
                pipeline.addLast(new IdleStateHandler(
                    serverConfig.getMaxIdleTime(),
                    serverConfig.getHeartbeatInterval(),
                    serverConfig.getMaxIdleTime()));

                // WebSocket编解码器
                pipeline.addLast(new WebSocket13FrameDecoder(true, true, 65536));
                pipeline.addLast(new WebSocket13FrameEncoder(true));

                // 创建握手处理器工厂
                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketUrl(), null, true);

                // WebSocket处理器
                pipeline.addLast(new WebSocketServerHandler(
                    serverConfig, agentRegistry, wsFactory, httpProxyService));
            }

            private String getWebSocketUrl() {
                String protocol = serverConfig.isSslEnabled() ? "wss" : "ws";
                return protocol + "://0.0.0.0:" + serverConfig.getWsPort() + "/ws";
            }
        };
    }
}