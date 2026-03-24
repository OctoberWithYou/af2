package org.ljc.test.server;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Server API黑盒测试
 * 测试REST API接口功能
 */
class ServerApiTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String API_BASE = BASE_URL + "/api";

    @BeforeAll
    static void setup() {
        baseURI = BASE_URL;
        basePath = "/api";
    }

    /**
     * 测试登录接口 - 成功登录
     */
    @Test
    void testLoginSuccess() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"Sys_ljc_123\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("token", notNullValue())
            .body("message", equalTo("Login successful"));
    }

    /**
     * 测试登录接口 - 密码错误
     */
    @Test
    void testLoginWrongPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"wrong\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(401)
            .body("success", is(false));
    }

    /**
     * 测试登录接口 - 用户名错误
     */
    @Test
    void testLoginWrongUsername() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"wronguser\",\"password\":\"Sys_ljc_123\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(401)
            .body("success", is(false));
    }

    /**
     * 测试获取Server状态 - 未认证
     */
    @Test
    void testGetStatusUnauthenticated() {
        when()
            .get("/status")
        .then()
            .statusCode(401);
    }

    /**
     * 测试获取Agent列表 - 未认证
     */
    @Test
    void testGetAgentsUnauthenticated() {
        when()
            .get("/agents")
        .then()
            .statusCode(401);
    }

    /**
     * 测试获取Agent列表 - 已认证
     */
    @Test
    void testGetAgentsAuthenticated() {
        // 先登录获取token
        String token = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"Sys_ljc_123\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("token");

        // 使用token访问
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/agents")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("count", greaterThanOrEqualTo(0));
    }

    /**
     * 测试Basic Auth认证
     */
    @Test
    void testBasicAuth() {
        given()
            .auth().basic("admin", "Sys_ljc_123")
        .when()
            .get("/status")
        .then()
            .statusCode(200)
            .body("success", is(true));
    }

    /**
     * 测试登出接口
     */
    @Test
    void testLogout() {
        // 先登录
        String token = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"Sys_ljc_123\"}")
        .when()
            .post("/login")
        .then()
            .extract()
            .path("token");

        // 登出
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .post("/logout")
        .then()
            .statusCode(200)
            .body("success", is(true));
    }
}