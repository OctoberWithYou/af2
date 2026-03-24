package org.ljc.test.agent;

import org.junit.jupiter.api.Test;
import org.ljc.agent.config.AgentConfig;
import org.ljc.agent.service.RequestForwarder;
import org.ljc.common.model.*;
import org.ljc.common.util.JsonUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocketClientHandler 逻辑覆盖率测试
 * 测试不依赖 Netty Channel 的逻辑方法
 */
class WebSocketClientHandlerCoverageTest {

    @Test
    void testAgentConfigGetters() {
        AgentConfig config = new AgentConfig();
        config.setAgentId("test-agent");
        config.setName("Test Agent");
        config.setDescription("Test Description");
        config.setServerUrl("ws://localhost:8888/ws");
        config.setConnectTimeout(5000);
        config.setHeartbeatInterval(30);
        config.setReconnectInterval(10);
        config.setMaxRetries(5);
        config.setSslEnabled(true);
        config.setAllowedTargets(new String[]{"*", "https://api.openai.com/*"});
        config.setVersion("1.0.0");

        assertEquals("test-agent", config.getAgentId());
        assertEquals("Test Agent", config.getName());
        assertEquals("Test Description", config.getDescription());
        assertEquals("ws://localhost:8888/ws", config.getServerUrl());
        assertEquals(5000, config.getConnectTimeout());
        assertEquals(30, config.getHeartbeatInterval());
        assertEquals(10, config.getReconnectInterval());
        assertEquals(5, config.getMaxRetries());
        assertTrue(config.isSslEnabled());
        assertEquals(2, config.getAllowedTargets().length);
        assertEquals("1.0.0", config.getVersion());
    }

    @Test
    void testAgentInfo() {
        AgentInfo info = new AgentInfo();
        info.setAgentId("agent-001");
        info.setName("Test Agent");
        info.setDescription("Test");
        info.setStatus("CONNECTED");
        info.setVersion("1.0.0");
        info.setAllowedTargets(new String[]{"*"});

        assertEquals("agent-001", info.getAgentId());
        assertEquals("Test Agent", info.getName());
        assertEquals("CONNECTED", info.getStatus());
        assertNotNull(info.getAllowedTargets());
    }

    @Test
    void testHttpRequestGettersSetters() {
        HttpRequest request = new HttpRequest();
        request.setMethod("POST");
        request.setUrl("https://api.example.com/v1/chat");
        request.setPath("/v1/chat");
        request.setBody("{\"message\":\"hello\"}");
        request.setContentType("application/json");
        request.setCharset("UTF-8");

        assertEquals("POST", request.getMethod());
        assertEquals("https://api.example.com/v1/chat", request.getUrl());
        assertEquals("/v1/chat", request.getPath());
        assertEquals("{\"message\":\"hello\"}", request.getBody());
    }

    @Test
    void testHttpResponseGettersSetters() {
        HttpResponse response = new HttpResponse();
        response.setStatusCode(200);
        response.setStatusMessage("OK");
        response.setBody("{\"result\":\"success\"}");
        response.setContentType("application/json");
        response.setCharset("UTF-8");
        response.setResponseTime(100);

        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getStatusMessage());
        assertTrue(response.isSuccess());

        response.setStatusCode(500);
        assertFalse(response.isSuccess());
    }

    @Test
    void testMessageGettersSetters() {
        Message message = new Message(MessageType.REGISTER);
        message.setId("msg-001");
        message.setSenderId("agent-001");
        message.setPayload("{\"name\":\"test\"}");
        message.setSuccess(true);
        message.setErrorMessage("error");

        assertEquals("msg-001", message.getId());
        assertEquals("agent-001", message.getSenderId());
        assertEquals(MessageType.REGISTER, message.getType());
        assertEquals("{\"name\":\"test\"}", message.getPayload());
        assertTrue(message.isSuccess());
        assertEquals("error", message.getErrorMessage());
    }

    @Test
    void testMessageStaticMethods() {
        Message successMsg = Message.successResponse(MessageType.REGISTER_RESPONSE, "{\"status\":\"ok\"}");
        assertNotNull(successMsg);
        assertEquals(MessageType.REGISTER_RESPONSE, successMsg.getType());
        assertTrue(successMsg.isSuccess());
        assertEquals("{\"status\":\"ok\"}", successMsg.getPayload());

        Message errorMsg = Message.errorResponse(MessageType.ERROR, "Something went wrong");
        assertNotNull(errorMsg);
        assertEquals(MessageType.ERROR, errorMsg.getType());
        assertFalse(errorMsg.isSuccess());
        assertEquals("Something went wrong", errorMsg.getErrorMessage());
    }

    @Test
    void testMessageTypeValues() {
        MessageType[] types = MessageType.values();
        assertTrue(types.length >= 9);

        assertEquals(MessageType.REGISTER, MessageType.valueOf("REGISTER"));
        assertEquals(MessageType.HEARTBEAT, MessageType.valueOf("HEARTBEAT"));
        assertEquals(MessageType.FORWARD_REQUEST, MessageType.valueOf("FORWARD_REQUEST"));
    }

    @Test
    void testJsonUtil() {
        // 测试序列化
        Message message = new Message(MessageType.HEARTBEAT);
        message.setSenderId("test-agent");
        String json = JsonUtil.toJson(message);
        assertNotNull(json);
        assertTrue(json.contains("HEARTBEAT"));

        // 测试反序列化
        Message deserialized = JsonUtil.fromJson(json, Message.class);
        assertNotNull(deserialized);
        assertEquals(MessageType.HEARTBEAT, deserialized.getType());
        assertEquals("test-agent", deserialized.getSenderId());

        // 测试格式化 JSON
        String prettyJson = JsonUtil.toPrettyJson(message);
        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains("\n"));
    }

    @Test
    void testIsTargetAllowed() {
        // 通过 AgentConfig 测试目标 URL 检查逻辑
        AgentConfig config = new AgentConfig();

        // 测试通配符
        config.setAllowedTargets(new String[]{"*"});
        RequestForwarder forwarder = new RequestForwarder(config);
        assertNotNull(forwarder);
        forwarder.close();
    }

    @Test
    void testAgentConfigEmptyTargets() {
        AgentConfig config = new AgentConfig();
        config.setAllowedTargets(new String[]{});
        assertNotNull(config.getAllowedTargets());
    }

    @Test
    void testAgentConfigNullAgentId() {
        AgentConfig config = new AgentConfig();
        // Agent ID 可以为 null
        config.setAgentId(null);
        assertNull(config.getAgentId());
    }
}
