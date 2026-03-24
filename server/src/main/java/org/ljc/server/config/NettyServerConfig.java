package org.ljc.server.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.ljc.server.handler.WebSocketServerInitializer;
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
 * 启动WebSocket和HTTP服务器
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
                    .childHandler(new WebSocketServerInitializer(serverConfig, agentRegistry, httpProxyService))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

                Channel ch = b.bind(serverConfig.getWsPort()).sync().channel();
                logger.info("WebSocket server started on port {}", serverConfig.getWsPort());

                ch.closeFuture().sync();
            } catch (Exception e) {
                logger.error("WebSocket server failed to start", e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }
}