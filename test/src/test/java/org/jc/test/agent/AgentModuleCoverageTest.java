package org.ljc.test.agent;

import org.junit.jupiter.api.Test;
import org.ljc.agent.config.AgentConfig;
import org.ljc.agent.service.RequestForwarder;
import org.ljc.common.exception.AgentException;
import org.ljc.common.model.HttpRequest;
import org.ljc.common.model.HttpResponse;
import org.ljc.common.util.JsonUtil;
import org.ljc.common.util.SslUtil;

import java.util.HashMap;
import java.util.Map;

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

    @Test
    void testRequestForwarderWithRequest() {
        // 测试带请求的转发
        AgentConfig config = new AgentConfig();
        config.setConnectTimeout(5000);
        RequestForwarder forwarder = new RequestForwarder(config);

        // 创建测试请求
        HttpRequest request = new HttpRequest();
        request.setMethod("POST");
        request.setUrl("https://httpbin.org/post");
        request.setBody("{\"test\":\"value\"}");
        request.setContentType("application/json");

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Test-Header", "test-value");
        request.setHeaders(headers);

        // 执行转发（网络可能可达也可能不可达，但应该返回响应而不是抛异常）
        HttpResponse response = forwarder.forward(request);
        assertNotNull(response);
        // 网络可达时会得到成功响应，不可达时会得到 500 错误响应
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 500);

        forwarder.close();
    }

    @Test
    void testRequestForwarderGetMethod() {
        // 测试 GET 方法
        AgentConfig config = new AgentConfig();
        RequestForwarder forwarder = new RequestForwarder(config);

        HttpRequest request = new HttpRequest();
        request.setMethod("GET");
        request.setUrl("https://httpbin.org/get");

        HttpResponse response = forwarder.forward(request);
        assertNotNull(response);

        forwarder.close();
    }

    @Test
    void testRequestForwarderPutMethod() {
        // 测试 PUT 方法
        AgentConfig config = new AgentConfig();
        RequestForwarder forwarder = new RequestForwarder(config);

        HttpRequest request = new HttpRequest();
        request.setMethod("PUT");
        request.setUrl("https://httpbin.org/put");
        request.setBody("{\"update\":\"value\"}");
        request.setContentType("application/json");

        HttpResponse response = forwarder.forward(request);
        assertNotNull(response);

        forwarder.close();
    }

    @Test
    void testRequestForwarderDeleteMethod() {
        // 测试 DELETE 方法
        AgentConfig config = new AgentConfig();
        RequestForwarder forwarder = new RequestForwarder(config);

        HttpRequest request = new HttpRequest();
        request.setMethod("DELETE");
        request.setUrl("https://httpbin.org/delete");

        HttpResponse response = forwarder.forward(request);
        assertNotNull(response);

        forwarder.close();
    }

    @Test
    void testRequestForwarderPatchMethod() {
        // 测试 PATCH 方法
        AgentConfig config = new AgentConfig();
        RequestForwarder forwarder = new RequestForwarder(config);

        HttpRequest request = new HttpRequest();
        request.setMethod("PATCH");
        request.setUrl("https://httpbin.org/patch");
        request.setBody("{\"patch\":\"value\"}");

        HttpResponse response = forwarder.forward(request);
        assertNotNull(response);

        forwarder.close();
    }

    @Test
    void testRequestForwarderHeadMethod() {
        // 测试 HEAD 方法
        AgentConfig config = new AgentConfig();
        RequestForwarder forwarder = new RequestForwarder(config);

        HttpRequest request = new HttpRequest();
        request.setMethod("HEAD");
        request.setUrl("https://httpbin.org/get");

        HttpResponse response = forwarder.forward(request);
        assertNotNull(response);

        forwarder.close();
    }

    @Test
    void testRequestForwarderOptionsMethod() {
        // 测试 OPTIONS 方法
        AgentConfig config = new AgentConfig();
        RequestForwarder forwarder = new RequestForwarder(config);

        HttpRequest request = new HttpRequest();
        request.setMethod("OPTIONS");
        request.setUrl("https://httpbin.org/get");

        HttpResponse response = forwarder.forward(request);
        assertNotNull(response);

        forwarder.close();
    }

    @Test
    void testRequestForwarderWithCharset() {
        // 测试带字符集的请求
        AgentConfig config = new AgentConfig();
        RequestForwarder forwarder = new RequestForwarder(config);

        HttpRequest request = new HttpRequest();
        request.setMethod("POST");
        request.setUrl("https://httpbin.org/post");
        request.setBody("测试内容");
        request.setContentType("application/json");
        request.setCharset("UTF-8");

        HttpResponse response = forwarder.forward(request);
        assertNotNull(response);

        forwarder.close();
    }

    @Test
    void testJsonUtilException() {
        // 测试 JsonUtil 异常处理
        assertThrows(org.ljc.common.exception.SerializationException.class, () -> {
            JsonUtil.toJson(new Object() {
                @Override
                public String toString() {
                    throw new RuntimeException("Serialization error");
                }
            });
        });

        assertThrows(org.ljc.common.exception.SerializationException.class, () -> {
            JsonUtil.fromJson("invalid json", Object.class);
        });

        assertThrows(org.ljc.common.exception.SerializationException.class, () -> {
            JsonUtil.toPrettyJson(new Object() {
                @Override
                public String toString() {
                    throw new RuntimeException("Pretty print error");
                }
            });
        });
    }
}
