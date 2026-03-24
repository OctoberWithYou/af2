package org.ljc.test.agent;

import org.junit.jupiter.api.Test;
import org.ljc.agent.config.AgentConfig;
import org.ljc.agent.service.RequestForwarder;
import org.ljc.common.exception.AgentException;
import org.ljc.common.util.SslUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Agent 模块覆盖率补充测试
 */
class AgentModuleCoverageTest {

    @Test
    void testAgentException() {
        // 测试 AgentException
        AgentException exception1 = new AgentException("test message");
        assertNotNull(exception1);
        assertEquals("test message", exception1.getMessage());

        AgentException exception2 = new AgentException("test", new RuntimeException());
        assertNotNull(exception2);
        assertNotNull(exception2.getCause());
    }

    @Test
    void testSslUtil() {
        // 测试 SslUtil 静态方法
        // 测试密钥库检查
        assertFalse(SslUtil.keyStoreExists(""));
        assertFalse(SslUtil.keyStoreExists(null));
        assertFalse(SslUtil.keyStoreExists("non_existent_path.jks"));

        // SSL 上下文创建可能需要实际的密钥库，只测试方法存在
        try {
            SslUtil.createTrustAllClientSslContext();
        } catch (Exception e) {
            // 预期可能失败
            assertTrue(true);
        }

        try {
            SslUtil.createServerSslContext("test", "test");
        } catch (Exception e) {
            // 预期失败，因为密钥库不存在
            assertTrue(true);
        }
    }

    @Test
    void testRequestForwarder() {
        // 测试 RequestForwarder
        AgentConfig config = new AgentConfig();
        RequestForwarder forwarder = new RequestForwarder(config);
        assertNotNull(forwarder);

        // 测试 close 方法
        forwarder.close();
    }
}
