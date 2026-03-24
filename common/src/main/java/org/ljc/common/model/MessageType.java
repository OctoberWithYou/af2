package org.ljc.common.model;

/**
 * 消息类型枚举
 * 定义Agent与Server之间通信的消息类型
 */
public enum MessageType {
    /**
     * Agent注册请求
     */
    REGISTER,

    /**
     * Agent注册响应
     */
    REGISTER_RESPONSE,

    /**
     * 心跳检测
     */
    HEARTBEAT,

    /**
     * 心跳响应
     */
    HEARTBEAT_RESPONSE,

    /**
     * HTTP请求转发
     */
    FORWARD_REQUEST,

    /**
     * HTTP响应转发
     */
    FORWARD_RESPONSE,

    /**
     * 断开连接
     */
    DISCONNECT,

    /**
     * 认证请求
     */
    AUTH_REQUEST,

    /**
     * 认证响应
     */
    AUTH_RESPONSE,

    /**
     * 错误消息
     */
    ERROR
}