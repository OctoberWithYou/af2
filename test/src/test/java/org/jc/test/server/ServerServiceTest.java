package org.ljc.test.server;

import org.junit.jupiter.api.Test;
import org.ljc.server.config.ServerConfig;
import org.ljc.server.service.AuthenticationService;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Server 模块服务测试
 */
class ServerServiceTest {

    @Test
    void testServerConfig() {
        // 测试 ServerConfig 默认值
        ServerConfig config = new ServerConfig();

        assertEquals(8080, config.getPort());
        assertEquals(8888, config.getWsPort());
        assertFalse(config.isSslEnabled());
        assertEquals(30, config.getHeartbeatInterval());
        assertEquals(120, config.getMaxIdleTime());
        assertEquals("admin", config.getAuthUsername());
        assertEquals("Sys_ljc_123", config.getAuthPassword());
    }

    @Test
    void testServerConfigSetters() {
        // 测试 ServerConfig setter
        ServerConfig config = new ServerConfig();

        config.setPort(9000);
        config.setWsPort(9999);
        config.setSslEnabled(true);
        config.setKeyStorePath("/path/to/keystore.jks");
        config.setKeyStorePassword("password");
        config.setHeartbeatInterval(60);
        config.setMaxIdleTime(300);
        config.setAuthUsername("testuser");
        config.setAuthPassword("testpass");

        assertEquals(9000, config.getPort());
        assertEquals(9999, config.getWsPort());
        assertTrue(config.isSslEnabled());
        assertEquals("/path/to/keystore.jks", config.getKeyStorePath());
        assertEquals("password", config.getKeyStorePassword());
        assertEquals(60, config.getHeartbeatInterval());
        assertEquals(300, config.getMaxIdleTime());
        assertEquals("testuser", config.getAuthUsername());
        assertEquals("testpass", config.getAuthPassword());
    }

    @Test
    void testAuthenticationServiceSuccess() {
        // 测试认证成功
        ServerConfig config = new ServerConfig();
        config.setAuthUsername("admin");
        config.setAuthPassword("Sys_ljc_123");

        AuthenticationService authService = new AuthenticationService(config);

        String token = authService.authenticate("admin", "Sys_ljc_123");
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // 验证 token 有效
        assertTrue(authService.validateToken(token));

        // 登出
        authService.logout(token);
        assertFalse(authService.validateToken(token));
    }

    @Test
    void testAuthenticationServiceWrongPassword() {
        // 测试密码错误
        ServerConfig config = new ServerConfig();
        config.setAuthUsername("admin");
        config.setAuthPassword("Sys_ljc_123");

        AuthenticationService authService = new AuthenticationService(config);

        String token = authService.authenticate("admin", "wrong_password");
        assertNull(token);
    }

    @Test
    void testAuthenticationServiceWrongUsername() {
        // 测试用户名错误
        ServerConfig config = new ServerConfig();
        config.setAuthUsername("admin");
        config.setAuthPassword("Sys_ljc_123");

        AuthenticationService authService = new AuthenticationService(config);

        String token = authService.authenticate("wrong_user", "Sys_ljc_123");
        assertNull(token);
    }

    @Test
    void testExtractTokenWithNullHeader() {
        // 测试空请求头
        ServerConfig config = new ServerConfig();
        config.setAuthUsername("admin");
        config.setAuthPassword("Sys_ljc_123");

        AuthenticationService authService = new AuthenticationService(config);

        assertNull(authService.extractToken(null));
        assertNull(authService.extractToken(""));
        assertNull(authService.extractToken("   "));
    }

    @Test
    void testExtractTokenWithBasicAuth() {
        // 测试 Basic Auth
        ServerConfig config = new ServerConfig();
        config.setAuthUsername("admin");
        config.setAuthPassword("Sys_ljc_123");

        AuthenticationService authService = new AuthenticationService(config);

        // admin:Sys_ljc_123 的 Base64 编码
        String basicAuth = "Basic YWRtaW46U3lzX2xqY18xMjM=";
        String token = authService.extractToken(basicAuth);
        assertNotNull(token);
        assertTrue(authService.validateToken(token));

        // 清理
        authService.logout(token);
    }

    @Test
    void testExtractTokenWithInvalidBasicAuth() {
        // 测试无效的 Basic Auth
        ServerConfig config = new ServerConfig();
        config.setAuthUsername("admin");
        config.setAuthPassword("Sys_ljc_123");

        AuthenticationService authService = new AuthenticationService(config);

        // 无效的 Base64
        assertNull(authService.extractToken("Basic !!!invalid!!!"));
    }

    @Test
    void testExtractTokenWithBearerToken() {
        // 测试 Bearer Token
        ServerConfig config = new ServerConfig();
        config.setAuthUsername("admin");
        config.setAuthPassword("Sys_ljc_123");

        AuthenticationService authService = new AuthenticationService(config);

        // 先获取有效 token
        String token = authService.authenticate("admin", "Sys_ljc_123");
        assertNotNull(token);

        // 使用 Bearer Token 方式提取
        String extractedToken = authService.extractToken("Bearer " + token);
        assertEquals(token, extractedToken);

        // 清理
        authService.logout(token);
    }

    @Test
    void testExtractTokenWithInvalidBearerToken() {
        // 测试无效的 Bearer Token
        ServerConfig config = new ServerConfig();
        config.setAuthUsername("admin");
        config.setAuthPassword("Sys_ljc_123");

        AuthenticationService authService = new AuthenticationService(config);

        // 使用一个不存在的 token
        String extractedToken = authService.extractToken("Bearer invalid_token_12345");
        assertNull(extractedToken);
    }

    @Test
    void testMultipleSessions() {
        // 测试多会话
        ServerConfig config = new ServerConfig();
        config.setAuthUsername("admin");
        config.setAuthPassword("Sys_ljc_123");

        AuthenticationService authService = new AuthenticationService(config);

        String token1 = authService.authenticate("admin", "Sys_ljc_123");
        assertNotNull(token1);

        // 等待一点时间以确保时间戳不同
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }

        String token2 = authService.authenticate("admin", "Sys_ljc_123");
        assertNotNull(token2);

        // 两个 token 应该都有效（可能在极短时间生成，但至少都可用）
        assertTrue(authService.validateToken(token1));
        assertTrue(authService.validateToken(token2));

        // 登出一个
        authService.logout(token1);
        assertFalse(authService.validateToken(token1));
        assertTrue(authService.validateToken(token2));

        // 清理
        authService.logout(token2);
    }

    @Test
    void testLogoutNonExistentToken() {
        // 测试登出不存在的 token
        ServerConfig config = new ServerConfig();
        config.setAuthUsername("admin");
        config.setAuthPassword("Sys_ljc_123");

        AuthenticationService authService = new AuthenticationService(config);

        // 不应该抛异常
        assertDoesNotThrow(() -> authService.logout("non_existent_token"));
    }
}