package org.ljc.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import org.ljc.common.model.*;
import org.ljc.common.util.JsonUtil;
import org.ljc.server.config.ServerConfig;
import org.ljc.server.registry.AgentRegistry;
import org.ljc.server.service.HttpProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * WebSocket服务器处理器
 * 处理Agent的WebSocket连接、消息收发、心跳等
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private final ServerConfig serverConfig;
    private final AgentRegistry agentRegistry;
    private final WebSocketServerHandshakerFactory handshakerFactory;
    private final HttpProxyService httpProxyService;
    private WebSocketServerHandshaker handshaker;

    private String agentId;

    public WebSocketServerHandler(ServerConfig serverConfig, AgentRegistry agentRegistry,
                                   WebSocketServerHandshakerFactory handshakerFactory,
                                   HttpProxyService httpProxyService) {
        this.serverConfig = serverConfig;
        this.agentRegistry = agentRegistry;
        this.handshakerFactory = handshakerFactory;
        this.httpProxyService = httpProxyService;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            // 处理HTTP握手请求
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            // 处理WebSocket帧
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 处理HTTP握手请求
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 创建handshaker并完成握手
        handshaker = handshakerFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), request);
        }
    }

    /**
     * 处理WebSocket帧
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            // 文本帧 - 解析消息
            String text = ((TextWebSocketFrame) frame).text();
            try {
                Message message = JsonUtil.fromJson(text, Message.class);
                processMessage(ctx, message);
            } catch (Exception e) {
                logger.error("Failed to parse message: {}", text, e);
                sendError(ctx, "Invalid message format");
            }
        } else if (frame instanceof PongWebSocketFrame) {
            // 心跳Pong
            logger.debug("Received Pong from channel: {}", ctx.channel().id());
        } else if (frame instanceof CloseWebSocketFrame) {
            // 关闭连接
            if (handshaker != null) {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            }
        }
    }

    /**
     * 处理接收到的消息
     */
    private void processMessage(ChannelHandlerContext ctx, Message message) {
        switch (message.getType()) {
            case REGISTER:
                handleRegister(ctx, message);
                break;
            case HEARTBEAT:
                handleHeartbeat(ctx, message);
                break;
            case FORWARD_RESPONSE:
                handleForwardResponse(ctx, message);
                break;
            default:
                sendError(ctx, "Unknown message type: " + message.getType());
        }
    }

    /**
     * 处理Agent注册
     */
    private void handleRegister(ChannelHandlerContext ctx, Message message) {
        try {
            AgentInfo agentInfo = JsonUtil.fromJson(message.getPayload(), AgentInfo.class);
            agentId = agentInfo.getAgentId();

            if (agentId == null || agentId.isEmpty()) {
                agentId = java.util.UUID.randomUUID().toString();
                agentInfo.setAgentId(agentId);
            }

            agentInfo.setStatus("CONNECTED");
            agentInfo.setRegisteredAt(LocalDateTime.now());
            agentInfo.setLastHeartbeat(LocalDateTime.now());

            // 注册Agent
            agentRegistry.register(agentId, ctx.channel(), agentInfo);

            // 发送注册响应
            Message response = Message.successResponse(MessageType.REGISTER_RESPONSE,
                JsonUtil.toJson(agentInfo));
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(response)));

            logger.info("Agent registered: {}, channel: {}", agentId, ctx.channel().id());
        } catch (Exception e) {
            logger.error("Failed to register agent", e);
            sendError(ctx, "Registration failed: " + e.getMessage());
        }
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(ChannelHandlerContext ctx, Message message) {
        if (agentId != null) {
            agentRegistry.updateHeartbeat(agentId);

            // 发送心跳响应
            Message response = Message.successResponse(MessageType.HEARTBEAT_RESPONSE,
                "{\"status\":\"ok\"}");
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(response)));

            logger.debug("Heartbeat received from agent: {}", agentId);
        }
    }

    /**
     * 处理Agent转发的HTTP响应
     * 将响应返回给等待的HTTP客户端
     */
    private void handleForwardResponse(ChannelHandlerContext ctx, Message message) {
        try {
            // 从payload中解析HTTP响应
            HttpResponse httpResponse = JsonUtil.fromJson(message.getPayload(), HttpResponse.class);

            // 提取原始消息ID用于匹配请求
            String originalMessageId = message.getMetadata() != null ?
                message.getMetadata().get("originalMessageId") : null;

            if (originalMessageId != null) {
                httpProxyService.handleResponse(originalMessageId, httpResponse);
            }

            logger.debug("Forward response received from agent: {}, status: {}",
                agentId, httpResponse.getStatusCode());
        } catch (Exception e) {
            logger.error("Failed to handle forward response", e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(ChannelHandlerContext ctx, String errorMessage) {
        Message error = Message.errorResponse(MessageType.ERROR, errorMessage);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(error)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (agentId != null) {
            agentRegistry.unregister(agentId);
            logger.info("Agent disconnected: {}", agentId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception in WebSocket handler", cause);
        ctx.close();
    }
}