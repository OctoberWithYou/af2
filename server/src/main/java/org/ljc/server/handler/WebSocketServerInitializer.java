package org.ljc.server.handler;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import org.ljc.server.config.ServerConfig;
import org.ljc.server.registry.AgentRegistry;
import org.ljc.server.service.HttpProxyService;

/**
 * WebSocket服务器通道初始化器
 * 配置Netty channel的处理器链
 */
public class WebSocketServerInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {
    private final ServerConfig serverConfig;
    private final AgentRegistry agentRegistry;
    private final HttpProxyService httpProxyService;

    public WebSocketServerInitializer(ServerConfig serverConfig, AgentRegistry agentRegistry,
                                       HttpProxyService httpProxyService) {
        this.serverConfig = serverConfig;
        this.agentRegistry = agentRegistry;
        this.httpProxyService = httpProxyService;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

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

        // WebSocket处理器 (传入HttpProxyService)
        pipeline.addLast(new WebSocketServerHandler(serverConfig, agentRegistry, wsFactory, httpProxyService));
    }

    /**
     * 获取WebSocket URL
     */
    private String getWebSocketUrl() {
        String protocol = serverConfig.isSslEnabled() ? "wss" : "ws";
        return protocol + "://0.0.0.0:" + serverConfig.getWsPort() + "/ws";
    }
}