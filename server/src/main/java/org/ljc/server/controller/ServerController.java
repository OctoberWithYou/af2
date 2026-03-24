package org.ljc.server.controller;

import org.ljc.common.model.AgentInfo;
import org.ljc.server.config.ServerConfig;
import org.ljc.server.registry.AgentRegistry;
import org.ljc.server.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Server REST API控制器
 * 提供Agent管理、状态查询等REST接口
 */
@RestController
@RequestMapping("/api")
public class ServerController {

    private final ServerConfig serverConfig;
    private final AgentRegistry agentRegistry;
    private final AuthenticationService authService;

    public ServerController(ServerConfig serverConfig, AgentRegistry agentRegistry,
                           AuthenticationService authService) {
        this.serverConfig = serverConfig;
        this.agentRegistry = agentRegistry;
        this.authService = authService;
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        String token = authService.authenticate(username, password);

        if (token != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("token", token);
            result.put("message", "Login successful");
            return ResponseEntity.ok(result);
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
    }

    /**
     * 登出接口
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = authService.extractToken(authHeader);
        if (token != null) {
            authService.logout(token);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Logout successful");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有已连接的Agent列表
     */
    @GetMapping("/agents")
    public ResponseEntity<Map<String, Object>> getAgents(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!validateAuth(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<AgentInfo> agents = agentRegistry.getAllAgents().values().stream()
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", agents.size());
        result.put("agents", agents);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取指定Agent信息
     */
    @GetMapping("/agents/{agentId}")
    public ResponseEntity<Map<String, Object>> getAgent(
            @PathVariable String agentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!validateAuth(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AgentInfo agentInfo = agentRegistry.getAgentInfo(agentId);

        Map<String, Object> result = new HashMap<>();
        if (agentInfo != null) {
            result.put("success", true);
            result.put("agent", agentInfo);
            return ResponseEntity.ok(result);
        } else {
            result.put("success", false);
            result.put("message", "Agent not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    /**
     * 获取Server状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!validateAuth(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("port", serverConfig.getPort());
        result.put("wsPort", serverConfig.getWsPort());
        result.put("sslEnabled", serverConfig.isSslEnabled());
        result.put("agentCount", agentRegistry.getAgentCount());
        result.put("heartbeatInterval", serverConfig.getHeartbeatInterval());
        result.put("maxIdleTime", serverConfig.getMaxIdleTime());

        return ResponseEntity.ok(result);
    }

    /**
     * 验证认证
     */
    private boolean validateAuth(String authHeader) {
        String token = authService.extractToken(authHeader);
        return token != null;
    }
}