package org.ljc.test.common;

import org.junit.jupiter.api.Test;
import org.ljc.common.model.*;
import org.ljc.common.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 公共模块黑盒测试
 * 测试Message、AgentInfo等实体的序列化/反序列化
 */
class CommonModelTest {

    /**
     * 测试Message序列化与反序列化
     */
    @Test
    void testMessageSerialization() {
        Message message = new Message(MessageType.REGISTER);
        message.setSenderId("test-agent-001");
        message.setPayload("{\"name\":\"TestAgent\"}");
        message.setSuccess(true);

        // 序列化
        String json = JsonUtil.toJson(message);
        assertNotNull(json);
        assertTrue(json.contains("REGISTER"));

        // 反序列化
        Message deserialized = JsonUtil.fromJson(json, Message.class);
        assertNotNull(deserialized);
        assertEquals(MessageType.REGISTER, deserialized.getType());
        assertEquals("test-agent-001", deserialized.getSenderId());
        assertTrue(deserialized.isSuccess());
    }

    /**
     * 测试Message创建成功响应
     */
    @Test
    void testMessageSuccessResponse() {
        Message response = Message.successResponse(MessageType.REGISTER_RESPONSE, "{\"status\":\"ok\"}");
        assertTrue(response.isSuccess());
        assertEquals(MessageType.REGISTER_RESPONSE, response.getType());
        assertNotNull(response.getId());
    }

    /**
     * 测试Message创建错误响应
     */
    @Test
    void testMessageErrorResponse() {
        Message error = Message.errorResponse(MessageType.ERROR, "Test error message");
        assertFalse(error.isSuccess());
        assertEquals(MessageType.ERROR, error.getType());
        assertEquals("Test error message", error.getErrorMessage());
    }

    /**
     * 测试AgentInfo序列化
     */
    @Test
    void testAgentInfoSerialization() {
        AgentInfo info = new AgentInfo();
        info.setAgentId("agent-001");
        info.setName("Test Agent");
        info.setDescription("Test description");
        info.setStatus("CONNECTED");
        info.setVersion("1.0.0");
        info.setAllowedTargets(new String[]{"https://api.openai.com/*"});

        String json = JsonUtil.toJson(info);
        assertNotNull(json);

        AgentInfo deserialized = JsonUtil.fromJson(json, AgentInfo.class);
        assertEquals("agent-001", deserialized.getAgentId());
        assertEquals("Test Agent", deserialized.getName());
        assertEquals("CONNECTED", deserialized.getStatus());
    }

    /**
     * 测试HttpRequest序列化
     */
    @Test
    void testHttpRequestSerialization() {
        HttpRequest request = new HttpRequest();
        request.setMethod("POST");
        request.setUrl("https://api.openai.com/v1/chat/completions");
        request.setPath("/v1/chat/completions");

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer test-key");
        request.setHeaders(headers);

        request.setBody("{\"model\":\"gpt-4\",\"messages\":[]}");

        String json = JsonUtil.toJson(request);
        assertNotNull(json);

        HttpRequest deserialized = JsonUtil.fromJson(json, HttpRequest.class);
        assertEquals("POST", deserialized.getMethod());
        assertEquals("https://api.openai.com/v1/chat/completions", deserialized.getUrl());
        assertEquals(2, deserialized.getHeaders().size());
        assertTrue(deserialized.getBody().contains("gpt-4"));
    }

    /**
     * 测试HttpResponse序列化
     */
    @Test
    void testHttpResponseSerialization() {
        HttpResponse response = new HttpResponse();
        response.setStatusCode(200);
        response.setStatusMessage("OK");
        response.setBody("{\"result\":\"success\"}");
        response.setContentType("application/json");
        response.setCharset("UTF-8");
        response.setResponseTime(150);

        assertTrue(response.isSuccess());

        String json = JsonUtil.toJson(response);
        HttpResponse deserialized = JsonUtil.fromJson(json, HttpResponse.class);
        assertEquals(200, deserialized.getStatusCode());
        assertTrue(deserialized.isSuccess());
    }

    /**
     * 测试格式化JSON输出
     */
    @Test
    void testPrettyJson() {
        Message message = new Message(MessageType.HEARTBEAT);
        message.setSenderId("test");

        String prettyJson = JsonUtil.toPrettyJson(message);
        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains("\n"));
    }

    /**
     * 测试MessageType枚举
     */
    @Test
    void testMessageType() {
        assertEquals("REGISTER", MessageType.REGISTER.name());
        assertEquals("HEARTBEAT", MessageType.HEARTBEAT.name());
        assertEquals("FORWARD_REQUEST", MessageType.FORWARD_REQUEST.name());
        assertEquals("FORWARD_RESPONSE", MessageType.FORWARD_RESPONSE.name());
        assertEquals(MessageType.values().length, 10);
    }

    /**
     * 测试HttpResponse错误状态判断
     */
    @Test
    void testHttpResponseErrorStatus() {
        HttpResponse response200 = new HttpResponse();
        response200.setStatusCode(200);
        assertTrue(response200.isSuccess());

        HttpResponse response201 = new HttpResponse();
        response201.setStatusCode(201);
        assertTrue(response201.isSuccess());

        HttpResponse response400 = new HttpResponse();
        response400.setStatusCode(400);
        assertFalse(response400.isSuccess());

        HttpResponse response500 = new HttpResponse();
        response500.setStatusCode(500);
        assertFalse(response500.isSuccess());
    }

    /**
     * 测试AgentInfo允许目标为空
     */
    @Test
    void testAgentInfoEmptyAllowedTargets() {
        AgentInfo info = new AgentInfo();
        info.setAgentId("agent-002");
        info.setAllowedTargets(new String[]{});

        String json = JsonUtil.toJson(info);
        AgentInfo deserialized = JsonUtil.fromJson(json, AgentInfo.class);
        assertNotNull(deserialized.getAllowedTargets());
        assertEquals(0, deserialized.getAllowedTargets().length);
    }
}