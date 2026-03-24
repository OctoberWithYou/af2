package org.ljc.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent应用启动类
 * 负责启动WebSocket客户端，连接Server并转发AI请求
 */
public class AgentApplication {
    private static final Logger logger = LoggerFactory.getLogger(AgentApplication.class);

    public static void main(String[] args) {
        logger.info("Starting AI Forward Agent...");
        logger.info("Agent configuration should be set via system properties or config file");
        logger.info("Use --agent.server-url=ws://localhost:8888/ws to configure server URL");

        // 实际启动逻辑需要通过配置初始化
        // 这里仅作为入口点
        try {
            // 阻止主线程退出 (实际部署时需要实现守护进程)
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.info("Agent shutting down...");
        }
    }
}