package org.ljc.agent.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.ljc.agent.config.AgentConfig;

import java.util.concurrent.TimeUnit;

/**
 * WebSocket客户端通道初始化器
 * 配置Netty channel的处理器链
 */
public class WebSocketClientInitializer extends ChannelInitializer<SocketChannel> {
    private final AgentConfig agentConfig;
    private final WebSocketClientHandshaker handshaker;
    private final WebSocketClientHandler clientHandler;

    public WebSocketClientInitializer(AgentConfig agentConfig, WebSocketClientHandshaker handshaker,
                                      WebSocketClientHandler clientHandler) {
        this.agentConfig = agentConfig;
        this.handshaker = handshaker;
        this.clientHandler = clientHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // HTTP编解码器
        pipeline.addLast(new HttpClientCodec());

        // 聚合HTTP消息
        pipeline.addLast(new HttpObjectAggregator(65536));

        // 空闲检测
        pipeline.addLast(new IdleStateHandler(
            agentConfig.getConnectTimeout() / 1000,
            agentConfig.getHeartbeatInterval(),
            agentConfig.getConnectTimeout() / 1000));

        // WebSocket编解码器 (使用正确的构造器)
        pipeline.addLast(new WebSocket13FrameDecoder(true, true, 65536));
        pipeline.addLast(new WebSocket13FrameEncoder(true));

        // 客户端处理器
        pipeline.addLast(clientHandler);
    }
}