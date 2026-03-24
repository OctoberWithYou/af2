package org.ljc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Server应用启动类
 * 负责启动WebSocket服务器和HTTP代理服务
 *
 * @author Claude Opus 4.6
 * @created 2026-03-23
 */
@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}