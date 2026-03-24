package org.ljc.common.model;

import java.util.Map;
import java.util.UUID;

/**
 * 通信消息实体
 * 用于Agent与Server之间的JSON格式通信
 */
public class Message {
    /**
     * 消息唯一标识
     */
    private String id;

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 发送方标识 (Agent ID)
     */
    private String senderId;

    /**
     * 目标标识
     */
    private String targetId;

    /**
     * 负载数据 (JSON格式的请求/响应内容)
     */
    private String payload;

    /**
     * 附加数据 (如Headers等)
     */
    private Map<String, String> metadata;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    public Message() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public Message(MessageType type) {
        this();
        this.type = type;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 创建成功响应
     */
    public static Message successResponse(MessageType type, String payload) {
        Message msg = new Message(type);
        msg.setSuccess(true);
        msg.setPayload(payload);
        return msg;
    }

    /**
     * 创建错误响应
     */
    public static Message errorResponse(MessageType type, String errorMessage) {
        Message msg = new Message(type);
        msg.setSuccess(false);
        msg.setErrorMessage(errorMessage);
        return msg;
    }
}