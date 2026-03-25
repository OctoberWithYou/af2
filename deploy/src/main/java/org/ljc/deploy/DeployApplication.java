package org.ljc.deploy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * Deploy 应用启动类
 * 提供部署管理 REST API 和 Web 界面
 *
 * 约定大于配置:
 * - 默认端口：8081
 * - 默认使用嵌入式 H2 数据库
 * - 无需配置文件即可启动
 *
 * @author Claude Opus 4.6
 * @created 2026-03-23
 */
@SpringBootApplication
@MapperScan("org.ljc.deploy.mapper")
@EnableConfigurationProperties(ServerProperties.class)
public class DeployApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeployApplication.class, args);
    }

    /**
     * 设置默认端口为 8081（约定配置）
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> portCustomizer() {
        return factory -> factory.setPort(8081);
    }

    /**
     * 嵌入式 H2 数据库（约定配置）
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .setName("deploydb")
            .build();
    }

    /**
     * 初始化数据库表结构（约定配置）
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        // 创建部署配置表（如果不存在）
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS deploy_config (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                type VARCHAR(50) NOT NULL,
                config_json TEXT,
                status VARCHAR(50) DEFAULT 'STOPPED',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_by VARCHAR(100)
            )
            """);
        // 创建用户表（如果不存在）
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                must_change_password BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """);
        return jdbc;
    }
}
