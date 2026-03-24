package org.ljc.agent.config;

import java.util.Properties;

/**
 * Agent配置类
 * 从系统属性或配置文件读取配置
 */
public class AgentConfig {
    private static final String DEFAULT_SERVER_URL = "ws://localhost:8888/ws";
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_HEARTBEAT_INTERVAL = 30;
    private static final int DEFAULT_RECONNECT_INTERVAL = 10;

    private String agentId;
    private String name = "Agent";
    private String description = "AI Model Forwarding Agent";
    private String serverUrl = DEFAULT_SERVER_URL;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
    private int reconnectInterval = DEFAULT_RECONNECT_INTERVAL;
    private int maxRetries = 5;
    private boolean sslEnabled = false;
    private String[] allowedTargets = {"*"};
    private String version = "1.0.0";

    public AgentConfig() {
        loadFromSystemProperties();
    }

    /**
     * 从系统属性加载配置
     */
    private void loadFromSystemProperties() {
        this.agentId = System.getProperty("agent.id", System.getenv("AGENT_ID"));
        this.name = System.getProperty("agent.name", "AI Forward Agent");
        this.description = System.getProperty("agent.description", "内网AI模型转发代理");
        this.serverUrl = System.getProperty("agent.server-url", DEFAULT_SERVER_URL);
        this.connectTimeout = Integer.parseInt(System.getProperty("agent.connect-timeout", "5000"));
        this.heartbeatInterval = Integer.parseInt(System.getProperty("agent.heartbeat-interval", "30"));
        this.reconnectInterval = Integer.parseInt(System.getProperty("agent.reconnect-interval", "10"));
        this.maxRetries = Integer.parseInt(System.getProperty("agent.max-retries", "5"));
        this.sslEnabled = Boolean.parseBoolean(System.getProperty("agent.ssl-enabled", "false"));
        this.version = System.getProperty("agent.version", "1.0.0");
    }

    // Getters and Setters

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public String[] getAllowedTargets() {
        return allowedTargets;
    }

    public void setAllowedTargets(String[] allowedTargets) {
        this.allowedTargets = allowedTargets;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}