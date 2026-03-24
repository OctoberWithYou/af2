package org.ljc.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Server配置类
 * 读取application.yml中的server配置
 */
@Configuration
@ConfigurationProperties(prefix = "server")
public class ServerConfig {
    /**
     * HTTP服务端口
     */
    private int port = 8080;

    /**
     * WebSocket端口
     */
    private int wsPort = 8888;

    /**
     * SSL/TLS启用
     */
    private boolean sslEnabled = false;

    /**
     * SSL密钥库路径
     */
    private String keyStorePath;

    /**
     * SSL密钥库密码
     */
    private String keyStorePassword;

    /**
     * 心跳检测间隔(秒)
     */
    private int heartbeatInterval = 30;

    /**
     * Agent最大空闲时间(秒)
     */
    private int maxIdleTime = 120;

    /**
     * 认证用户名
     */
    private String authUsername = "admin";

    /**
     * 认证密码
     */
    private String authPassword = "Sys_ljc_123";

    // Getters and Setters

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWsPort() {
        return wsPort;
    }

    public void setWsPort(int wsPort) {
        this.wsPort = wsPort;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public String getAuthUsername() {
        return authUsername;
    }

    public void setAuthUsername(String authUsername) {
        this.authUsername = authUsername;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }
}