package org.ljc.test.deploy;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Deploy API 扩展性测试
 * 测试系统在高负载和高并发场景下的表现
 */
@Execution(ExecutionMode.CONCURRENT)
class DeployScalabilityTest {

    private static final String BASE_URL = "https://localhost:8081";
    private static final String API_BASE = BASE_URL + "/api";
    private static String authToken;

    @BeforeAll
    static void setup() {
        baseURI = BASE_URL;
        basePath = "/api";
        // Login to get token
        authToken = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"admin\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("token");
    }

    /**
     * 测试创建大量配置
     */
    @Test
    void testCreateManyConfigs() {
        int configCount = 20;

        for (int i = 0; i < configCount; i++) {
            String configName = "test-config-" + System.currentTimeMillis() + "-" + i;
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body("{\"name\":\"" + configName + "\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
            .when()
                .post("/deploy/configs")
            .then()
                .statusCode(200)
                .body("success", is(true));
        }

        System.out.println("Created " + configCount + " configurations successfully");
    }

    /**
     * 并发创建配置测试
     */
    @Test
    void testConcurrentConfigCreation() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String configName = "concurrent-config-" + index + "-" + System.currentTimeMillis();
                    given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + authToken)
                        .body("{\"name\":\"" + configName + "\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
                    .when()
                        .post("/deploy/configs")
                    .then()
                        .statusCode(200)
                        .body("success", is(true));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("Concurrent Config Creation Results:");
        System.out.println("  Success: " + successCount.get());
        System.out.println("  Failure: " + failureCount.get());

        assert successCount.get() > 0 : "At least some configs should be created";
    }

    /**
     * 并发读取配置测试
     */
    @Test
    void testConcurrentConfigRead() throws InterruptedException, ExecutionException {
        // First create a config to read
        String configName = "read-test-" + System.currentTimeMillis();
        int configId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body("{\"name\":\"" + configName + "\",\"type\":\"SERVER\",\"configJson\":\"{}\"}")
        .when()
            .post("/deploy/configs")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("data.id");

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    given()
                        .header("Authorization", "Bearer " + authToken)
                    .when()
                        .get("/deploy/configs/" + configId)
                    .then()
                        .statusCode(200)
                        .body("success", is(true));
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("Concurrent Config Read Results:");
        System.out.println("  Success: " + successCount.get() + "/" + threadCount);
    }

    /**
     * 压力测试 - 快速连续请求
     */
    @Test
    void testStressGetConfigs() {
        int requestCount = 50;

        for (int i = 0; i < requestCount; i++) {
            given()
                .header("Authorization", "Bearer " + authToken)
            .when()
                .get("/deploy/configs")
            .then()
                .statusCode(200)
                .body("success", is(true));
        }

        System.out.println("Stress Test: " + requestCount + " GET requests completed");
    }

    /**
     * 并发混合操作测试 - 同时进行读、写、统计
     */
    @Test
    void testMixedOperations() throws InterruptedException {
        int threadCount = 6;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Thread 1-2: Create configs
        for (int t = 0; t < 2; t++) {
            executor.submit(() -> {
                try {
                    String name = "mixed-test-" + System.currentTimeMillis();
                    given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + authToken)
                        .body("{\"name\":\"" + name + "\",\"type\":\"AGENT\",\"configJson\":\"{}\"}")
                    .when()
                        .post("/deploy/configs")
                    .then()
                        .statusCode(200);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Thread 3-4: Read configs
        for (int t = 0; t < 2; t++) {
            executor.submit(() -> {
                try {
                    given()
                        .header("Authorization", "Bearer " + authToken)
                    .when()
                        .get("/deploy/configs")
                    .then()
                        .statusCode(200);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Thread 5-6: Get stats
        for (int t = 0; t < 2; t++) {
            executor.submit(() -> {
                try {
                    given()
                        .header("Authorization", "Bearer " + authToken)
                    .when()
                        .get("/deploy/stats")
                    .then()
                        .statusCode(200);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("Mixed Operations Test: All concurrent operations completed");
    }

    /**
     * 测试认证过期场景
     */
    @Test
    void testInvalidToken() {
        String[] invalidTokens = {"", "invalid", "Bearer token_123"};

        for (String token : invalidTokens) {
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/deploy/configs")
            .then()
                .statusCode(anyOf(is(401), is(403)))
                .body("success", is(false));
        }

        System.out.println("Invalid Token Test: All invalid tokens properly rejected");
    }
}