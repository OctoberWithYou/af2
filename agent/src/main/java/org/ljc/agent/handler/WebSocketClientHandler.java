package org.ljc.agent.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.ljc.agent.config.AgentConfig;
import org.ljc.common.model.AgentInfo;
import org.ljc.common.model.Message;
import org.ljc.common.model.MessageType;
import org.ljc.common.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket客户端处理器
 * 负责连接Server、发送心跳、处理转发请求等
 */
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private final AgentConfig agentConfig;
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private ScheduledFuture<?> heartbeatFuture;

    private boolean connected = false;
    private String agentId;

    public WebSocketClientHandler(AgentConfig agentConfig, WebSocketClientHandshaker handshaker) {
        this.agentConfig = agentConfig;
        this.handshaker = handshaker;
        this.agentId = agentConfig.getAgentId();
        if (this.agentId == null || this.agentId.isEmpty()) {
            this.agentId = UUID.randomUUID().toString();
        }
    }

    public ChannelPromise handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        connected = false;
        stopHeartbeat();
        logger.info("Disconnected from server");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            logger.info("WebSocket handshake complete");
            handshakeFuture.setSuccess();

            // 开始心跳
            startHeartbeat(ch);

            // 注册到Server
            registerAgent(ch);
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new Exception("Unexpected FullHttpResponse (getStatus=" + response.status() +
                ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            handleMessage(textFrame.text(), ch);
        } else if (frame instanceof PongWebSocketFrame) {
            logger.debug("Received Pong from server");
        } else if (frame instanceof CloseWebSocketFrame) {
            logger.info("Received close frame");
            ch.close();
        }
    }

    /**
     * 处理接收到的消息
     */
    private void handleMessage(String text, Channel ch) {
        try {
            Message message = JsonUtil.fromJson(text, Message.class);

            switch (message.getType()) {
                case REGISTER_RESPONSE:
                    handleRegisterResponse(message, ch);
                    break;
                case HEARTBEAT_RESPONSE:
                    logger.debug("Heartbeat response received");
                    break;
                case FORWARD_REQUEST:
                    handleForwardRequest(message, ch);
                    break;
                case ERROR:
                    logger.error("Error from server: {}", message.getErrorMessage());
                    break;
                default:
                    logger.warn("Unknown message type: {}", message.getType());
            }
        } catch (Exception e) {
            logger.error("Failed to handle message: {}", text, e);
        }
    }

    /**
     * 处理注册响应
     */
    private void handleRegisterResponse(Message message, Channel ch) {
        if (message.isSuccess()) {
            connected = true;
            logger.info("Agent registered successfully: {}", agentId);
        } else {
            logger.error("Registration failed: {}", message.getErrorMessage());
        }
    }

    /**
     * 处理转发请求
     */
    private void handleForwardRequest(Message message, Channel ch) {
        logger.info("Received forward request: {}", message.getId());
        // TODO: 转发请求到内网AI模型，并返回响应
    }

    /**
     * 注册Agent到Server
     */
    private void registerAgent(Channel ch) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setAgentId(agentId);
        agentInfo.setName(agentConfig.getName());
        agentInfo.setDescription(agentConfig.getDescription());
        agentInfo.setAllowedTargets(agentConfig.getAllowedTargets());
        agentInfo.setVersion(agentConfig.getVersion());

        Message registerMsg = new Message(MessageType.REGISTER);
        registerMsg.setSenderId(agentId);
        registerMsg.setPayload(JsonUtil.toJson(agentInfo));

        ch.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(registerMsg)));
        logger.info("Sending registration request");
    }

    /**
     * 开始心跳
     */
    private void startHeartbeat(Channel ch) {
        heartbeatFuture = ch.eventLoop().scheduleAtFixedRate(() -> {
            if (ch.isActive()) {
                Message heartbeat = new Message(MessageType.HEARTBEAT);
                heartbeat.setSenderId(agentId);
                ch.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(heartbeat)));
                logger.debug("Heartbeat sent");
            }
        }, agentConfig.getHeartbeatInterval(), agentConfig.getHeartbeatInterval(), TimeUnit.SECONDS);
    }

    /**
     * 停止心跳
     */
    private void stopHeartbeat() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(true);
            heartbeatFuture = null;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception in WebSocket client", cause);
        ctx.close();
    }
}