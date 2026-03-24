package org.ljc.server.controller;

import org.ljc.server.service.AuthenticationService;
import org.ljc.server.service.HttpProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP代理控制器
 * 接受用户HTTP请求并转发到Agent
 */
@RestController
public class ProxyController {
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final HttpProxyService httpProxyService;
    private final AuthenticationService authService;

    public ProxyController(HttpProxyService httpProxyService, AuthenticationService authService) {
        this.httpProxyService = httpProxyService;
        this.authService = authService;
    }

    /**
     * 代理GET请求
     */
    @GetMapping("/proxy/**")
    public ResponseEntity<Map<String, Object>> proxyGet(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Map<String, String> allParams) {

        return handleProxyRequest(null, allParams, null, authHeader);
    }

    /**
     * 代理POST请求
     */
    @PostMapping("/proxy/**")
    public ResponseEntity<Map<String, Object>> proxyPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Map<String, String> allParams,
            @RequestBody(required = false) String body) {

        return handleProxyRequest(null, allParams, body, authHeader);
    }

    /**
     * 代理PUT请求
     */
    @PutMapping("/proxy/**")
    public ResponseEntity<Map<String, Object>> proxyPut(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Map<String, String> allParams,
            @RequestBody(required = false) String body) {

        return handleProxyRequest(null, allParams, body, authHeader);
    }

    /**
     * 代理DELETE请求
     */
    @DeleteMapping("/proxy/**")
    public ResponseEntity<Map<String, Object>> proxyDelete(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Map<String, String> allParams) {

        return handleProxyRequest(null, allParams, null, authHeader);
    }

    /**
     * 处理代理请求 (简化版本)
     *
     * 注意: 完整实现需要通过Netty直接获取FullHttpRequest
     * 这里使用简化版本，演示代理逻辑
     */
    private ResponseEntity<Map<String, Object>> handleProxyRequest(
            String agentId,
            Map<String, String> queryParams,
            String body,
            String authHeader) {

        // 简单认证检查 (可选)
        // String token = authService.extractToken(authHeader);
        // if (token == null) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        //         .body(Map.of("error", "Unauthorized"));
        // }

        try {
            // 检查是否有可用的Agent
            if (httpProxyService.getPendingCount() > 10) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Too many pending requests"));
            }

            // 由于Spring Boot的HTTP请求已经被解析，我们需要通过其他方式转发
            // 这里返回提示信息，完整的实现需要自定义Netty HTTP Server
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Proxy endpoint ready");
            result.put("note", "Use WebSocket connection to agent first, then proxy requests via /proxy/{agentId}/*");
            result.put("availableAgents", "Check via GET /api/agents");
            result.put("pendingRequests", httpProxyService.getPendingCount());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Proxy request failed", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 代理状态检查
     */
    @GetMapping("/proxy/status")
    public ResponseEntity<Map<String, Object>> getProxyStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("pendingRequests", httpProxyService.getPendingCount());
        status.put("status", "running");
        status.put("message", "Proxy service is running");
        return ResponseEntity.ok(status);
    }
}