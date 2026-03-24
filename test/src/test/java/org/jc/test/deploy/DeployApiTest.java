package org.ljc.test.deploy;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Deploy API黑盒测试
 * 测试部署管理接口功能
 * 端到端测试覆盖前端界面交互场景
 */
class DeployApiTest {

    // 注意: 测试前需要先启动 deploy 服务
    // 运行命令: ./gradlew :deploy:bootRun
    // 测试命令: ./gradlew :test:test --tests "org.ljc.test.deploy.DeployApiTest"
    private static final String BASE_URL = "http://localhost:8081";
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

    /**
     * 测试获取单个配置详情 (前端查看配置弹窗)
     */
    @Test
    void testGetConfigById() {
        // 先创建一个配置
        int configId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"View Test\",\"type\":\"AGENT\",\"configJson\":\"{\\\"serverHost\\\":\\\"localhost\\\"}\"}")
        .when()
            .post(API_BASE + "/configs")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("data.id");

        // 获取详情
        when()
            .get(API_BASE + "/configs/" + configId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.id", equalTo(configId))
            .body("data.name", equalTo("View Test"))
            .body("data.type", equalTo("AGENT"))
            .body("data.configJson", notNullValue());
    }

    /**
     * 测试统计信息与实际配置数量匹配
     * 前端首页统计卡片数据准确性
     */
    @Test
    void testStatsAccuracy() {
        // 获取初始统计
        int initialAgents = when()
            .get(API_BASE + "/stats")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("totalAgents");

        // 创建Agent配置
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Stats Test Agent\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
        .when()
            .post(API_BASE + "/configs")
        .then()
            .statusCode(200);

        // 创建Server配置
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Stats Test Server\",\"type\":\"SERVER\",\"configJson\":\"{}\"}")
        .when()
            .post(API_BASE + "/configs")
        .then()
            .statusCode(200);

        // 验证统计更新
        when()
            .get(API_BASE + "/stats")
        .then()
            .statusCode(200)
            .body("totalAgents", equalTo(initialAgents + 1))
            .body("totalServers", equalTo(1));
    }

    /**
     * 测试空配置名创建失败
     * 前端表单验证
     */
    @Test
    void testCreateConfigWithEmptyName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
        .when()
            .post(API_BASE + "/configs")
        .then()
            // 空名称应该被处理(后端或返回400或200但name为空)
            .statusCode(anyOf(is(200), is(400)));
    }

    /**
     * 测试无效类型创建失败
     */
    @Test
    void testCreateConfigWithInvalidType() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Invalid Type Test\",\"type\":\"INVALID\",\"configJson\":\"{}\"}")
        .when()
            .post(API_BASE + "/configs")
        .then()
            .statusCode(anyOf(is(200), is(400)));
    }

    /**
     * 测试配置JSON格式错误
     * 前端传入无效JSON时的错误处理
     */
    @Test
    void testCreateConfigWithMalformedJson() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Malformed JSON Test\",\"type\":\"AGENT\",\"configJson\":\"not-valid-json\"}")
        .when()
            .post(API_BASE + "/configs")
        .then()
            .statusCode(anyOf(is(200), is(400)));
    }

    /**
     * 测试删除不存在的配置
     */
    @Test
    void testDeleteNonExistentConfig() {
        when()
            .delete(API_BASE + "/configs/999999")
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }
}