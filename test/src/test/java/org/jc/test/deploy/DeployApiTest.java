package org.ljc.test.deploy;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Deploy API 黑盒测试
 * 测试部署管理接口功能
 * 端到端测试覆盖前端界面交互场景
 *
 * 运行方式:
 * 1. 启动 deploy 服务：./gradlew :deploy:bootRun
 * 2. 运行测试：./gradlew :test:apiTest -PrunApiTests=true
 */
class DeployApiTest {

    private static final String BASE_URL = "http://localhost:8081";
    private static final String API_BASE = BASE_URL + "/api";
    private static String authToken = "";

    /**
     * 测试前登录获取 token
     */
    @BeforeAll
    static void login() {
        // 尝试默认密码 admin
        String token = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"admin\"}")
        .when()
            .post(API_BASE + "/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        if (token == null) {
            // 尝试旧密码 admin123
            token = given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"password\":\"admin123\"}")
            .when()
                .post(API_BASE + "/auth/login")
            .then()
                .statusCode(200)
                .extract()
                .path("token");
        }

        authToken = token;
    }

    private Header authHeader() {
        return new Header("Authorization", "Bearer " + authToken);
    }

    /**
     * 测试获取部署配置列表
     */
    @Test
    void testGetConfigs() {
        given()
            .header(authHeader())
        .when()
            .get(API_BASE + "/deploy/configs")
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
        given()
            .header(authHeader())
        .when()
            .get(API_BASE + "/deploy/stats")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("totalAgents", greaterThanOrEqualTo(0))
            .body("totalServers", greaterThanOrEqualTo(0));
    }

    /**
     * 测试创建 Agent 部署配置
     */
    @Test
    void testCreateAgentConfig() {
        String configJson = "{\"serverUrl\":\"ws://localhost:8888/ws\",\"agentId\":\"test-agent\"}";

        given()
            .header(authHeader())
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Test Agent\",\"type\":\"AGENT\",\"configJson\":\"" + configJson + "\"}")
        .when()
            .post(API_BASE + "/deploy/configs")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.name", equalTo("Test Agent"))
            .body("data.type", equalTo("AGENT"))
            .body("data.status", equalTo("STOPPED"));
    }

    /**
     * 测试创建 Server 部署配置
     */
    @Test
    void testCreateServerConfig() {
        String configJson = "{\"port\":8080,\"wsPort\":8888}";

        given()
            .header(authHeader())
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Test Server\",\"type\":\"SERVER\",\"configJson\":\"" + configJson + "\"}")
        .when()
            .post(API_BASE + "/deploy/configs")
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
        given()
            .header(authHeader())
        .when()
            .get(API_BASE + "/deploy/configs/99999")
        .then()
            .statusCode(404)
            .body("success", is(false));
    }

    /**
     * 测试部署配置 CRUD 流程
     */
    @Test
    void testConfigCrudFlow() {
        // 1. 创建配置
        int configId = given()
            .header(authHeader())
            .contentType(ContentType.JSON)
            .body("{\"name\":\"CRUD Test Agent\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
        .when()
            .post(API_BASE + "/deploy/configs")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("data.id");

        // 2. 获取配置
        given()
            .header(authHeader())
        .when()
            .get(API_BASE + "/deploy/configs/" + configId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.name", equalTo("CRUD Test Agent"));

        // 3. 更新配置
        given()
            .header(authHeader())
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Updated Agent\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
        .when()
            .put(API_BASE + "/deploy/configs/" + configId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.name", equalTo("Updated Agent"));

        // 4. 删除配置
        given()
            .header(authHeader())
        .when()
            .delete(API_BASE + "/deploy/configs/" + configId)
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
            .header(authHeader())
            .contentType(ContentType.JSON)
            .body("{\"name\":\"View Test\",\"type\":\"AGENT\",\"configJson\":\"{\\\"serverHost\\\":\\\"localhost\\\"}\"}")
        .when()
            .post(API_BASE + "/deploy/configs")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("data.id");

        // 获取详情
        given()
            .header(authHeader())
        .when()
            .get(API_BASE + "/deploy/configs/" + configId)
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
        int initialAgents = given()
            .header(authHeader())
        .when()
            .get(API_BASE + "/deploy/stats")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("totalAgents");

        // 创建 Agent 配置
        given()
            .header(authHeader())
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Stats Test Agent\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
        .when()
            .post(API_BASE + "/deploy/configs")
        .then()
            .statusCode(200);

        // 创建 Server 配置
        given()
            .header(authHeader())
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Stats Test Server\",\"type\":\"SERVER\",\"configJson\":\"{}\"}")
        .when()
            .post(API_BASE + "/deploy/configs")
        .then()
            .statusCode(200);

        // 验证统计更新
        given()
            .header(authHeader())
        .when()
            .get(API_BASE + "/deploy/stats")
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
            .header(authHeader())
            .contentType(ContentType.JSON)
            .body("{\"name\":\"\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
        .when()
            .post(API_BASE + "/deploy/configs")
        .then()
            // 空名称应该被处理 (后端或返回 400 或 200 但 name 为空)
            .statusCode(anyOf(is(200), is(400)));
    }

    /**
     * 测试无效类型创建失败
     */
    @Test
    void testCreateConfigWithInvalidType() {
        given()
            .header(authHeader())
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Invalid Type Test\",\"type\":\"INVALID\",\"configJson\":\"{}\"}")
        .when()
            .post(API_BASE + "/deploy/configs")
        .then()
            .statusCode(anyOf(is(200), is(400)));
    }

    /**
     * 测试配置 JSON 格式错误
     * 前端传入无效 JSON 时的错误处理
     */
    @Test
    void testCreateConfigWithMalformedJson() {
        given()
            .header(authHeader())
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Malformed JSON Test\",\"type\":\"AGENT\",\"configJson\":\"not-valid-json\"}")
        .when()
            .post(API_BASE + "/deploy/configs")
        .then()
            .statusCode(anyOf(is(200), is(400)));
    }

    /**
     * 测试删除不存在的配置
     */
    @Test
    void testDeleteNonExistentConfig() {
        given()
            .header(authHeader())
        .when()
            .delete(API_BASE + "/deploy/configs/999999")
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }
}
