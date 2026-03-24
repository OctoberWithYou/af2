package org.ljc.server.service;

import org.ljc.server.config.ServerConfig;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务
 * 处理用户登录认证
 */
@Service
public class AuthenticationService {
    private final ServerConfig serverConfig;

    /**
     * 会话存储: token -> username
     */
    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    public AuthenticationService(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * 验证用户名和密码
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证成功返回token，否则返回null
     */
    public String authenticate(String username, String password) {
        if (serverConfig.getAuthUsername().equals(username) &&
            serverConfig.getAuthPassword().equals(password)) {
            // 生成token
            String token = Base64.getEncoder().encodeToString(
                (username + ":" + System.currentTimeMillis()).getBytes());
            sessionStore.put(token, username);
            return token;
        }
        return null;
    }

    /**
     * 验证token是否有效
     *
     * @param token token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        return sessionStore.containsKey(token);
    }

    /**
     * 登出 - 删除token
     *
     * @param token token
     */
    public void logout(String token) {
        sessionStore.remove(token);
    }

    /**
     * 从请求头中提取token
     * 支持Basic Auth和Bearer Token两种方式
     *
     * @param authHeader Authorization头
     * @return token或null
     */
    public String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }

        if (authHeader.startsWith("Basic ")) {
            // Basic Auth - 解析用户名密码并认证
            String base64Credentials = authHeader.substring(6);
            try {
                String credentials = new String(Base64.getDecoder().decode(base64Credentials));
                String[] parts = credentials.split(":", 2);
                if (parts.length == 2) {
                    return authenticate(parts[0], parts[1]);
                }
            } catch (Exception e) {
                return null;
            }
        } else if (authHeader.startsWith("Bearer ")) {
            // Bearer Token
            String token = authHeader.substring(7);
            if (validateToken(token)) {
                return token;
            }
        }

        return null;
    }
}