package org.ljc.agent;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.ljc.agent.config.AgentConfig;
import org.ljc.agent.handler.WebSocketClientHandler;
import org.ljc.agent.handler.WebSocketClientInitializer;
import org.ljc.agent.service.RequestForwarder;
import org.ljc.common.model.AgentInfo;
import org.ljc.common.model.Message;
import org.ljc.common.model.MessageType;
import org.ljc.common.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Agent应用启动类
 * 负责启动WebSocket客户端，连接Server并转发AI请求
 */
public class AgentApplication {
    private static final Logger logger = LoggerFactory.getLogger(AgentApplication.class);

    private final AgentConfig config;
    private final RequestForwarder requestForwarder;
    private EventLoopGroup group;
    private Channel channel;
    private volatile boolean running = false;

    public AgentApplication(AgentConfig config, RequestForwarder requestForwarder) {
        this.config = config;
        this.requestForwarder = requestForwarder;
    }

    /**
     * 启动Agent并连接到Server
     */
    public void start() {
        if (running) {
            logger.warn("Agent is already running");
            return;
        }

        logger.info("Starting Agent: {} - {}", config.getName(), config.getAgentId());
        logger.info("Connecting to Server: {}", config.getServerUrl());

        // 创建RequestForwarder用于转发HTTP请求
        RequestForwarder forwarder = new RequestForwarder(config);

        group = new NioEventLoopGroup();

        try {
            // 解析WebSocket URL
            URI uri = new URI(config.getServerUrl());

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())
                .handler(new WebSocketClientInitializer(config, forwarder));

            // 连接服务器
            ChannelFuture future = bootstrap.connect(uri.getHost(), uri.getPort()).sync();
            channel = future.channel();

            // 等待连接关闭
            channel.closeFuture().sync();
            running = false;
            logger.info("Agent disconnected from server");

        } catch (Exception e) {
            logger.error("Failed to connect to server", e);
            running = false;
        } finally {
            group.shutdownGracefully();
            // 自动重连
            scheduleReconnect();
        }
    }

    /**
     * 调度重连
     */
    private void scheduleReconnect() {
        if (!running && config.getMaxRetries() > 0) {
            logger.info("Scheduling reconnect in {} seconds...", config.getReconnectInterval());
            try {
                TimeUnit.SECONDS.sleep(config.getReconnectInterval());
                start();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 停止Agent
     */
    public void stop() {
        running = false;
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
        logger.info("Agent stopped");
    }

    /**
     * 获取Agent状态
     */
    public boolean isRunning() {
        return running && channel != null && channel.isActive();
    }

    /**
     * 主入口
     */
    public static void main(String[] args) {
        // 加载配置
        AgentConfig config = new AgentConfig();
        RequestForwarder forwarder = new RequestForwarder(config);

        // 创建并启动Agent
        AgentApplication agent = new AgentApplication(config, forwarder);
        agent.start();

        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down agent...");
            agent.stop();
            forwarder.close();
        }));
    }
}