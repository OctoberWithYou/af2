package org.ljc.test.deploy;

import org.junit.jupiter.api.Test;
import org.ljc.deploy.entity.DeployConfig;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deploy 模块测试
 * 测试 DeployConfig 实体和配置解析
 */
class DeployModuleTest {

    @Test
    void testDeployConfigCreation() {
        // 测试 DeployConfig 创建和属性
        DeployConfig config = new DeployConfig();
        config.setId(1L);
        config.setName("Test Agent");
        config.setType("AGENT");
        config.setConfigJson("{\"serverHost\":\"localhost\",\"serverPort\":8888}");
        config.setStatus("STOPPED");
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        config.setCreatedBy("admin");

        assertEquals(1L, config.getId());
        assertEquals("Test Agent", config.getName());
        assertEquals("AGENT", config.getType());
        assertTrue(config.getConfigJson().contains("serverHost"));
        assertEquals("STOPPED", config.getStatus());
        assertNotNull(config.getCreatedAt());
        assertNotNull(config.getUpdatedAt());
        assertEquals("admin", config.getCreatedBy());
    }

    @Test
    void testDeployConfigDefaultValues() {
        // 测试默认状态
        DeployConfig config = new DeployConfig();

        assertNull(config.getId());
        assertNull(config.getName());
        assertNull(config.getType());
        assertNull(config.getConfigJson());
        assertNull(config.getStatus());
        assertNull(config.getCreatedAt());
        assertNull(config.getUpdatedAt());
        assertNull(config.getCreatedBy());
    }

    @Test
    void testDeployConfigAgentType() {
        // 测试 Agent 类型配置
        DeployConfig config = new DeployConfig();
        config.setId(1L);
        config.setName("My Agent");
        config.setType("AGENT");
        config.setStatus("STOPPED");

        String json = "{\"serverHost\":\"192.168.1.100\",\"serverPort\":8888,\"agentName\":\"TestAgent\",\"allowedTargets\":\"*\"}";
        config.setConfigJson(json);

        assertEquals("AGENT", config.getType());
        assertTrue(config.getConfigJson().contains("192.168.1.100"));
        assertTrue(config.getConfigJson().contains("TestAgent"));
    }

    @Test
    void testDeployConfigServerType() {
        // 测试 Server 类型配置
        DeployConfig config = new DeployConfig();
        config.setId(2L);
        config.setName("My Server");
        config.setType("SERVER");
        config.setStatus("RUNNING");

        String json = "{\"httpPort\":8080,\"wsPort\":8888}";
        config.setConfigJson(json);

        assertEquals("SERVER", config.getType());
        assertTrue(config.getConfigJson().contains("8080"));
        assertEquals("RUNNING", config.getStatus());
    }

    @Test
    void testDeployConfigEmptyJson() {
        // 测试空配置
        DeployConfig config = new DeployConfig();
        config.setName("Empty Config");
        config.setType("AGENT");
        config.setConfigJson("{}");

        assertNotNull(config.getConfigJson());
        assertEquals("{}", config.getConfigJson());
    }

    @Test
    void testDeployConfigStatus() {
        // 测试状态流转
        DeployConfig config = new DeployConfig();

        config.setStatus("STOPPED");
        assertEquals("STOPPED", config.getStatus());

        config.setStatus("DEPLOYING");
        assertEquals("DEPLOYING", config.getStatus());

        config.setStatus("RUNNING");
        assertEquals("RUNNING", config.getStatus());

        config.setStatus("STOPPED");
        assertEquals("STOPPED", config.getStatus());
    }
}