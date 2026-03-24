package org.ljc.deploy.service;

import org.ljc.deploy.entity.DeployConfig;
import org.ljc.deploy.mapper.DeployConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 部署服务
 * 负责Agent和Server的部署管理
 */
@Service
public class DeployService {
    private static final Logger logger = LoggerFactory.getLogger(DeployService.class);

    private final DeployConfigMapper configMapper;

    /**
     * 运行的进程存储: configId -> Process
     */
    private final Map<Long, Process> runningProcesses = new ConcurrentHashMap<>();

    public DeployService(DeployConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    /**
     * 获取所有部署配置
     */
    public List<DeployConfig> getAllConfigs() {
        return configMapper.findAll();
    }

    /**
     * 根据ID获取部署配置
     */
    public DeployConfig getConfigById(Long id) {
        return configMapper.findById(id);
    }

    /**
     * 根据类型获取部署配置
     */
    public List<DeployConfig> getConfigsByType(String type) {
        return configMapper.findByType(type);
    }

    /**
     * 创建部署配置
     */
    public DeployConfig createConfig(DeployConfig config) {
        config.setStatus("STOPPED");
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        configMapper.insert(config);
        return config;
    }

    /**
     * 更新部署配置
     */
    public DeployConfig updateConfig(DeployConfig config) {
        config.setUpdatedAt(LocalDateTime.now());
        configMapper.update(config);
        return config;
    }

    /**
     * 删除部署配置
     */
    public void deleteConfig(Long id) {
        // 如果进程正在运行，先停止
        stopProcess(id);
        configMapper.deleteById(id);
    }

    /**
     * 部署Agent或Server (一键部署)
     */
    public Map<String, Object> deploy(Long configId) {
        Map<String, Object> result = new HashMap<>();
        DeployConfig config = configMapper.findById(configId);

        if (config == null) {
            result.put("success", false);
            result.put("message", "Configuration not found");
            return result;
        }

        try {
            // 更新状态为部署中
            config.setStatus("DEPLOYING");
            configMapper.update(config);

            // 构建启动命令 (示例，实际需要根据实际情况调整)
            String jarPath = getJarPath(config.getType());
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", jarPath,
                "--spring.config.location=application.yml"
            );

            // 设置工作目录
            pb.directory(new java.io.File(getWorkDir(config.getType())));

            // 启动进程
            Process process = pb.start();
            runningProcesses.put(configId, process);

            // 更新状态为运行中
            config.setStatus("RUNNING");
            configMapper.update(config);

            // 记录日志
            logDeployment(configId, "Deployment started");

            result.put("success", true);
            result.put("message", "Deployment started successfully");
            result.put("pid", process.pid());

            logger.info("Deployed {} with config id: {}", config.getType(), configId);

        } catch (Exception e) {
            logger.error("Failed to deploy", e);
            config.setStatus("STOPPED");
            configMapper.update(config);

            result.put("success", false);
            result.put("message", "Deployment failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * 停止部署
     */
    public Map<String, Object> stop(Long configId) {
        Map<String, Object> result = new HashMap<>();
        DeployConfig config = configMapper.findById(configId);

        if (config == null) {
            result.put("success", false);
            result.put("message", "Configuration not found");
            return result;
        }

        boolean stopped = stopProcess(configId);

        if (stopped) {
            config.setStatus("STOPPED");
            configMapper.update(config);
            result.put("success", true);
            result.put("message", "Stopped successfully");
            logDeployment(configId, "Deployment stopped");
        } else {
            result.put("success", false);
            result.put("message", "Failed to stop process");
        }

        return result;
    }

    /**
     * 停止进程
     */
    private boolean stopProcess(Long configId) {
        Process process = runningProcesses.remove(configId);
        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                // 等待进程结束
                process.waitFor();
                return true;
            } catch (InterruptedException e) {
                process.destroyForcibly();
                return true;
            }
        }
        return true;
    }

    /**
     * 获取部署日志
     */
    public String getDeploymentLog(Long configId) {
        // 简化实现，实际需要日志文件读取
        return "Log for config " + configId;
    }

    /**
     * 记录部署日志
     */
    private void logDeployment(Long configId, String message) {
        logger.info("[Config {}] {}", configId, message);
    }

    /**
     * 获取JAR路径
     */
    private String getJarPath(String type) {
        // 实际部署时需要根据构建产物调整
        return "build/libs/" + type.toLowerCase() + "-1.0.0-SNAPSHOT.jar";
    }

    /**
     * 获取工作目录
     */
    private String getWorkDir(String type) {
        // 实际部署时需要根据实际情况调整
        return ".";
    }
}