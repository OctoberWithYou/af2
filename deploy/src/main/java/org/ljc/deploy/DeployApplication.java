package org.ljc.deploy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Deploy应用启动类
 * 提供部署管理REST API和Web界面
 */
@SpringBootApplication
@MapperScan("org.ljc.deploy.mapper")
public class DeployApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeployApplication.class, args);
    }
}