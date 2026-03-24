package org.ljc.deploy.controller;

import org.ljc.deploy.entity.DeployConfig;
import org.ljc.deploy.service.DeployService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部署管理REST控制器
 */
@RestController
@RequestMapping("/api/deploy")
public class DeployController {

    private final DeployService deployService;

    public DeployController(DeployService deployService) {
        this.deployService = deployService;
    }

    /**
     * 获取所有部署配置
     */
    @GetMapping("/configs")
    public ResponseEntity<Map<String, Object>> getAllConfigs() {
        List<DeployConfig> configs = deployService.getAllConfigs();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", configs);
        result.put("count", configs.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取部署配置
     */
    @GetMapping("/configs/{id}")
    public ResponseEntity<Map<String, Object>> getConfig(@PathVariable Long id) {
        DeployConfig config = deployService.getConfigById(id);
        Map<String, Object> result = new HashMap<>();
        if (config != null) {
            result.put("success", true);
            result.put("data", config);
            return ResponseEntity.ok(result);
        } else {
            result.put("success", false);
            result.put("message", "Configuration not found");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建部署配置
     */
    @PostMapping("/configs")
    public ResponseEntity<Map<String, Object>> createConfig(@RequestBody DeployConfig config) {
        DeployConfig created = deployService.createConfig(config);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", created);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新部署配置
     */
    @PutMapping("/configs/{id}")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable Long id,
            @RequestBody DeployConfig config) {
        config.setId(id);
        DeployConfig updated = deployService.updateConfig(config);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", updated);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除部署配置
     */
    @DeleteMapping("/configs/{id}")
    public ResponseEntity<Map<String, Object>> deleteConfig(@PathVariable Long id) {
        deployService.deleteConfig(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Deleted successfully");
        return ResponseEntity.ok(result);
    }

    /**
     * 一键部署
     */
    @PostMapping("/configs/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deploy(@PathVariable Long id) {
        Map<String, Object> result = deployService.deploy(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 停止部署
     */
    @PostMapping("/configs/{id}/stop")
    public ResponseEntity<Map<String, Object>> stop(@PathVariable Long id) {
        Map<String, Object> result = deployService.stop(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取部署日志
     */
    @GetMapping("/configs/{id}/logs")
    public ResponseEntity<Map<String, Object>> getLogs(@PathVariable Long id) {
        String logs = deployService.getDeploymentLog(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("logs", logs);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取部署统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<DeployConfig> agents = deployService.getConfigsByType("AGENT");
        List<DeployConfig> servers = deployService.getConfigsByType("SERVER");

        long runningAgents = agents.stream().filter(c -> "RUNNING".equals(c.getStatus())).count();
        long runningServers = servers.stream().filter(c -> "RUNNING".equals(c.getStatus())).count();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalAgents", agents.size());
        result.put("runningAgents", runningAgents);
        result.put("totalServers", servers.size());
        result.put("runningServers", runningServers);

        return ResponseEntity.ok(result);
    }
}