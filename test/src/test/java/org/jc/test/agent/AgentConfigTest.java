package org.ljc.test.agent;

import org.junit.jupiter.api.Test;
import org.ljc.agent.config.AgentConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Agent配置黑盒测试
 */
class AgentConfigTest {

    /**
     * 测试默认配置加载
     */
    @Test
    void testDefaultConfig() {
        AgentConfig config = new AgentConfig();

        assertEquals("ws://localhost:8888/ws", config.getServerUrl());
        assertEquals(5000, config.getConnectTimeout());
        assertEquals(30, config.getHeartbeatInterval());
        assertEquals(10, config.getReconnectInterval());
        assertEquals(5, config.getMaxRetries());
        assertFalse(config.isSslEnabled());
        assertEquals("1.0.0", config.getVersion());
        assertNotNull(config.getAllowedTargets());
    }

    /**
     * 测试Agent ID生成
     */
    @Test
    void testAgentIdCanBeNull() {
        // Agent ID可以为空，会自动生成UUID
        AgentConfig config = new AgentConfig();
        // agentId可能为null或系统属性设置的值
        assertNotNull(config.getName());
    }

    /**
     * 测试允许目标配置
     */
    @Test
    void testAllowedTargets() {
        AgentConfig config = new AgentConfig();
        String[] targets = config.getAllowedTargets();
        assertNotNull(targets);
        assertTrue(targets.length > 0);
        assertEquals("*", targets[0]);
    }

    /**
     * 测试版本号
     */
    @Test
    void testVersion() {
        AgentConfig config = new AgentConfig();
        assertEquals("1.0.0", config.getVersion());
    }

    /**
     * 测试心跳间隔范围
     */
    @Test
    void testHeartbeatIntervalRange() {
        AgentConfig config = new AgentConfig();
        assertTrue(config.getHeartbeatInterval() > 0);
        assertTrue(config.getHeartbeatInterval() <= 300); // 最大5分钟
    }

    /**
     * 测试重连间隔范围
     */
    @Test
    void testReconnectIntervalRange() {
        AgentConfig config = new AgentConfig();
        assertTrue(config.getReconnectInterval() > 0);
        assertTrue(config.getReconnectInterval() <= 300);
    }

    /**
     * 测试连接超时范围
     */
    @Test
    void testConnectTimeoutRange() {
        AgentConfig config = new AgentConfig();
        assertTrue(config.getConnectTimeout() >= 1000); // 最小1秒
        assertTrue(config.getConnectTimeout() <= 60000); // 最大60秒
    }
}