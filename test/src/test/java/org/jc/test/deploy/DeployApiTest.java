package org.ljc.test.deploy;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Deploy API黑盒测试
 * 测试部署管理接口功能
 */
class DeployApiTest {

    private static final String BASE_URL = "http://localhost:8090";
    private static final String API_BASE = BASE_URL + "/api/deploy";

    /**
     * 测试获取部署配置列表
     */
    @Test
    void testGetConfigs() {
        when()
            .get(API_BASE + "/configs")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("count", greaterThanOrEqualTo(0));
    }

    /**
     * 测试获取部署统计信息
     */
    @Test
    void testGetStats() {
        when()
            .get(API_BASE + "/stats")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("totalAgents", greaterThanOrEqualTo(0))
            .body("totalServers", greaterThanOrEqualTo(0));
    }

    /**
     * 测试创建Agent部署配置
     */
    @Test
    void testCreateAgentConfig() {
        String configJson = "{\"serverUrl\":\"ws://localhost:8888/ws\",\"agentId\":\"test-agent\"}";

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Test Agent\",\"type\":\"AGENT\",\"configJson\":\"" + configJson + "\"}")
        .when()
            .post(API_BASE + "/configs")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.name", equalTo("Test Agent"))
            .body("data.type", equalTo("AGENT"))
            .body("data.status", equalTo("STOPPED"));
    }

    /**
     * 测试创建Server部署配置
     */
    @Test
    void testCreateServerConfig() {
        String configJson = "{\"port\":8080,\"wsPort\":8888}";

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Test Server\",\"type\":\"SERVER\",\"configJson\":\"" + configJson + "\"}")
        .when()
            .post(API_BASE + "/configs")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.name", equalTo("Test Server"))
            .body("data.type", equalTo("SERVER"));
    }

    /**
     * 测试获取不存在的配置
     */
    @Test
    void testGetConfigNotFound() {
        when()
            .get(API_BASE + "/configs/99999")
        .then()
            .statusCode(404)
            .body("success", is(false));
    }

    /**
     * 测试部署配置CRUD流程
     */
    @Test
    void testConfigCrudFlow() {
        // 1. 创建配置
        int configId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"CRUD Test Agent\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
        .when()
            .post(API_BASE + "/configs")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("data.id");

        // 2. 获取配置
        given()
        .when()
            .get(API_BASE + "/configs/" + configId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.name", equalTo("CRUD Test Agent"));

        // 3. 更新配置
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Updated Agent\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
        .when()
            .put(API_BASE + "/configs/" + configId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.name", equalTo("Updated Agent"));

        // 4. 删除配置
        given()
        .when()
            .delete(API_BASE + "/configs/" + configId)
        .then()
            .statusCode(200)
            .body("success", is(true));
    }
}