package org.ljc.common.model;

import java.time.LocalDateTime;

/**
 * Agent注册信息实体
 * 用于Server端管理已注册的Agent
 */
public class AgentInfo {
    /**
     * Agent唯一标识
     */
    private String agentId;

    /**
     * Agent名称
     */
    private String name;

    /**
     * Agent描述
     */
    private String description;

    /**
     * 连接状态: CONNECTED, DISCONNECTED
     */
    private String status;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * 注册时间
     */
    private LocalDateTime registeredAt;

    /**
     * 可转发的目标URL列表
     */
    private String[] allowedTargets;

    /**
     * Agent版本
     */
    private String version;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
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