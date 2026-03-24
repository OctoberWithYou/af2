package org.ljc.server.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.ljc.common.model.*;
import org.ljc.common.util.JsonUtil;
import org.ljc.server.config.ServerConfig;
import org.ljc.server.registry.AgentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * HTTP代理服务
 * 将用户请求转发到对应的Agent
 */
@Service
public class HttpProxyService {
    private static final Logger logger = LoggerFactory.getLogger(HttpProxyService.class);

    private final ServerConfig serverConfig;
    private final AgentRegistry agentRegistry;

    /**
     * 等待响应的Future: messageId -> CompletableFuture
     */
    private final ConcurrentHashMap<String, CompletableFuture<HttpResponse>> pendingResponses = new ConcurrentHashMap<>();

    /**
     * 默认超时时间(秒)
     */
    private static final int DEFAULT_TIMEOUT = 60;

    public HttpProxyService(ServerConfig serverConfig, AgentRegistry agentRegistry) {
        this.serverConfig = serverConfig;
        this.agentRegistry = agentRegistry;
    }

    /**
     * 处理HTTP请求并转发到Agent
     *
     * @param request HTTP请求
     * @param targetAgentId 目标Agent ID (如果为null则使用默认Agent)
     * @return HTTP响应
     */
    public HttpResponse forwardRequest(FullHttpRequest request, String targetAgentId) {
        long startTime = System.currentTimeMillis();

        // 确定目标Agent
        String agentId = targetAgentId;
        if (agentId == null || agentId.isEmpty()) {
            // 如果没有指定Agent，使用第一个已连接的Agent
            Map<String, AgentInfo> agents = agentRegistry.getAllAgents();
            if (agents.isEmpty()) {
                return createErrorResponse(404, "No available agents");
            }
            agentId = agents.keySet().iterator().next();
            logger.info("No agent specified, using default agent: {}", agentId);
        }

        // 获取目标Agent的Channel
        Channel agentChannel = agentRegistry.getChannelByAgentId(agentId);
        if (agentChannel == null || !agentChannel.isActive()) {
            return createErrorResponse(404, "Agent not found or not connected: " + agentId);
        }

        // 构建转发消息
        Message forwardMessage = buildForwardMessage(request, agentId);

        // 创建CompletableFuture等待响应
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        pendingResponses.put(forwardMessage.getId(), future);

        try {
            // 发送消息到Agent
            agentChannel.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(forwardMessage)));
            logger.info("Forwarded request to agent: {}, path: {}, messageId: {}",
                agentId, request.uri(), forwardMessage.getId());

            // 等待响应 (带超时)
            HttpResponse response = future.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            response.setResponseTime(System.currentTimeMillis() - startTime);
            return response;

        } catch (TimeoutException e) {
            logger.warn("Request timeout for message: {}", forwardMessage.getId());
            pendingResponses.remove(forwardMessage.getId());
            return createErrorResponse(504, "Gateway Timeout - Agent response timeout");
        } catch (Exception e) {
            logger.error("Failed to forward request", e);
            pendingResponses.remove(forwardMessage.getId());
            return createErrorResponse(500, "Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * 处理Agent返回的响应
     * 由WebSocket处理器调用
     *
     * @param messageId 消息ID
     * @param response HTTP响应
     */
    public void handleResponse(String messageId, HttpResponse response) {
        CompletableFuture<HttpResponse> future = pendingResponses.remove(messageId);
        if (future != null) {
            future.complete(response);
            logger.debug("Response received for message: {}", messageId);
        } else {
            logger.warn("No pending request found for message: {}", messageId);
        }
    }

    /**
     * 构建转发消息
     */
    private Message buildForwardMessage(FullHttpRequest request, String targetAgentId) {
        Message message = new Message(MessageType.FORWARD_REQUEST);
        message.setTargetId(targetAgentId);

        // 构建HTTP请求实体
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setMethod(request.method().name());
        httpRequest.setPath(request.uri());
        httpRequest.setUrl(request.uri());

        // 处理请求头
        Map<String, String> headers = new HashMap<>();
        request.headers().forEach(entry ->
            headers.put(entry.getKey(), entry.getValue()));
        httpRequest.setHeaders(headers);

        // 处理请求体
        if (request.content().readableBytes() > 0) {
            httpRequest.setBody(request.content().toString(io.netty.util.CharsetUtil.UTF_8));
        }

        // 设置Content-Type
        String contentType = request.headers().get("Content-Type");
        if (contentType != null) {
            httpRequest.setContentType(contentType);
        }

        httpRequest.setTimeout(DEFAULT_TIMEOUT * 1000);
        message.setPayload(JsonUtil.toJson(httpRequest));
        return message;
    }

    /**
     * 创建错误响应
     */
    private HttpResponse createErrorResponse(int statusCode, String message) {
        HttpResponse response = new HttpResponse();
        response.setStatusCode(statusCode);
        response.setStatusMessage(message);
        response.setBody("{\"error\":\"" + message + "\"}");
        response.setContentType("application/json");
        response.setCharset("UTF-8");
        return response;
    }

    /**
     * 获取等待中的请求数量
     */
    public int getPendingCount() {
        return pendingResponses.size();
    }
}