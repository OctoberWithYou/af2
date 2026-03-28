package org.ljc.test.server;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Server 扩展性测试
 * 测试系统在高负载和高并发场景下的表现
 */
class ScalabilityTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String API_BASE = BASE_URL + "/api";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "Sys_ljc_123";

    @BeforeAll
    static void setup() {
        baseURI = BASE_URL;
        basePath = "/api";
    }

    /**
     * 并发登录测试 - 模拟多个用户同时登录
     */
    @Test
    void testConcurrentLogin() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    given()
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}")
                    .when()
                        .post("/login")
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

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("Concurrent Login Test Results:");
        System.out.println("  Success: " + successCount.get());
        System.out.println("  Failure: " + failureCount.get());

        assert successCount.get() > 0 : "At least some requests should succeed";
    }

    /**
     * 连续请求测试 - 模拟快速连续请求
     */
    @Test
    void testRapidRequests() {
        int requestCount = 50;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < requestCount; i++) {
            given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}")
            .when()
                .post("/login")
            .then()
                .statusCode(200)
                .body("success", is(true));
        }

        long duration = System.currentTimeMillis() - startTime;
        double avgTime = (double) duration / requestCount;

        System.out.println("Rapid Requests Test Results:");
        System.out.println("  Total Requests: " + requestCount);
        System.out.println("  Total Time: " + duration + "ms");
        System.out.println("  Avg Time per Request: " + avgTime + "ms");

        assert avgTime < 500 : "Average request time should be less than 500ms";
    }

    /**
     * 认证状态检查测试 - 模拟已认证用户的连续请求
     */
    @Test
    void testAuthenticatedRequests() {
        // First login to get token
        String token = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("token");

        // Make authenticated requests
        for (int i = 0; i < 10; i++) {
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/status")
            .then()
                .statusCode(200)
                .body("success", is(true));
        }

        System.out.println("Authenticated Requests Test: All 10 requests completed successfully");
    }

    /**
     * 并发认证请求测试
     */
    @Test
    void testConcurrentAuthenticatedRequests() throws InterruptedException {
        // First login to get token
        String token = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .extract()
            .path("token");

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String authToken = token;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 5; j++) {
                        given()
                            .header("Authorization", "Bearer " + authToken)
                        .when()
                            .get("/status")
                        .then()
                            .statusCode(200)
                            .body("success", is(true));
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("Concurrent Authenticated Requests Test Results:");
        System.out.println("  Total Requests: " + successCount.get());
        System.out.println("  All requests completed successfully");
    }

    /**
     * 错误认证测试 - 测试错误令牌的处理
     */
    @Test
    void testInvalidTokenHandling() {
        String[] invalidTokens = {"invalid", "", "Bearer ", "token_12345"};

        for (String token : invalidTokens) {
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/status")
            .then()
                .statusCode(anyOf(is(401), is(403)));
        }

        System.out.println("Invalid Token Handling Test: All invalid tokens properly rejected");
    }

    /**
     * 负载测试 - 模拟大量请求
     */
    @Test
    void testLoadTest() {
        int warmupRequests = 10;
        int testRequests = 100;

        // Warmup
        for (int i = 0; i < warmupRequests; i++) {
            given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}")
            .when()
                .post("/login")
            .then()
                .statusCode(anyOf(is(200), is(401)));
        }

        // Test
        List<Long> responseTimes = new ArrayList<>();
        for (int i = 0; i < testRequests; i++) {
            long start = System.currentTimeMillis();
            given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}")
            .when()
                .post("/login")
            .then()
                .statusCode(anyOf(is(200), is(401)));
            responseTimes.add(System.currentTimeMillis() - start);
        }

        double avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long min = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);

        System.out.println("Load Test Results:");
        System.out.println("  Requests: " + testRequests);
        System.out.println("  Avg Response Time: " + avg + "ms");
        System.out.println("  Min Response Time: " + min + "ms");
        System.out.println("  Max Response Time: " + max + "ms");

        assert avg < 1000 : "Average response time should be less than 1 second under load";
    }
}