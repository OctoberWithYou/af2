package org.ljc.agent.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.ljc.agent.config.AgentConfig;
import org.ljc.agent.service.RequestForwarder;
import org.ljc.common.util.SslUtil;

import java.net.URI;

/**
 * WebSocket客户端通道初始化器
 * 配置Netty channel的处理器链，支持SSL
 */
public class WebSocketClientInitializer extends ChannelInitializer<SocketChannel> {
    private final AgentConfig agentConfig;
    private final RequestForwarder requestForwarder;

    public WebSocketClientInitializer(AgentConfig agentConfig, RequestForwarder requestForwarder) {
        this.agentConfig = agentConfig;
        this.requestForwarder = requestForwarder;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 如果启用SSL，添加SSL处理器
        if (agentConfig.isSslEnabled()) {
            // 对于生产环境，应该使用带证书验证的SSLContext
            // 这里使用trust-all用于演示，生产环境应该配置trustStore
            SslContext sslContext = SslUtil.createTrustAllClientSslContext();
            SslHandler sslHandler = sslContext.newHandler(ch.alloc());
            pipeline.addFirst("ssl", sslHandler);
            // 使用HTTPS端口
            // 注意: Agent需要使用wss:// URL
            org.slf4j.LoggerFactory.getLogger(getClass()).info("SSL/TLS enabled for client connection");
        }

        // HTTP编解码器
        pipeline.addLast(new HttpClientCodec());

        // 聚合HTTP消息
        pipeline.addLast(new HttpObjectAggregator(65536));

        // 空闲检测
        pipeline.addLast(new IdleStateHandler(
            agentConfig.getConnectTimeout() / 1000,
            agentConfig.getHeartbeatInterval(),
            agentConfig.getConnectTimeout() / 1000));

        // WebSocket编解码器
        pipeline.addLast(new WebSocket13FrameDecoder(true, true, 65536));
        pipeline.addLast(new WebSocket13FrameEncoder(true));

        // 创建WebSocket握手处理器
        URI uri = new URI(agentConfig.getServerUrl());
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
            uri, WebSocketVersion.V13, null, true,
            new DefaultHttpHeaders());

        if (handshaker == null) {
            throw new RuntimeException("Cannot create WebSocket handshaker for: " + uri);
        }

        // 创建处理器
        WebSocketClientHandler handler = new WebSocketClientHandler(agentConfig, handshaker, requestForwarder);
        pipeline.addLast(handler);
    }
}