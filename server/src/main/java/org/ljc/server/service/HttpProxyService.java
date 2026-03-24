package org.ljc.server.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import org.ljc.common.model.*;
import org.ljc.common.util.JsonUtil;
import org.ljc.server.config.ServerConfig;
import org.ljc.server.registry.AgentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP代理服务
 * 将用户请求转发到对应的Agent
 */
@Service
public class HttpProxyService {
    private static final Logger logger = LoggerFactory.getLogger(HttpProxyService.class);

    private final ServerConfig serverConfig;
    private final AgentRegistry agentRegistry;

    public HttpProxyService(ServerConfig serverConfig, AgentRegistry agentRegistry) {
        this.serverConfig = serverConfig;
        this.agentRegistry = agentRegistry;
    }

    /**
     * 处理HTTP请求并转发到Agent
     *
     * @param request HTTP请求
     * @param targetAgentId 目标Agent ID
     * @return HTTP响应
     */
    public HttpResponse forwardRequest(FullHttpRequest request, String targetAgentId) {
        long startTime = System.currentTimeMillis();

        // 获取目标Agent的Channel
        Channel agentChannel = agentRegistry.getChannelByAgentId(targetAgentId);
        if (agentChannel == null || !agentChannel.isActive()) {
            return createErrorResponse(404, "Agent not found or not connected");
        }

        // 构建转发消息
        Message forwardMessage = buildForwardMessage(request, targetAgentId);

        // 发送消息到Agent (这里需要等待响应，实际实现需要更复杂的异步处理)
        agentChannel.writeAndFlush(
            new io.netty.handler.codec.http.websocketx.TextWebSocketFrame(
                JsonUtil.toJson(forwardMessage)));

        // TODO: 等待Agent响应 (需要实现异步响应机制)
        logger.info("Forwarded request to agent: {}, path: {}", targetAgentId, request.uri());

        // 返回模拟响应
        HttpResponse response = new HttpResponse();
        response.setStatusCode(200);
        response.setStatusMessage("OK");
        response.setResponseTime(System.currentTimeMillis() - startTime);
        return response;
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
        response.setBody(message);
        return response;
    }
}