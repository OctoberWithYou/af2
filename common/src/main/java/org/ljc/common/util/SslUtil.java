package org.ljc.common.util;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * SSL/TLS工具类
 * 用于创建SSLContext支持WebSocket wss://连接
 */
public class SslUtil {
    private static final Logger logger = LoggerFactory.getLogger(SslUtil.class);

    /**
     * 创建Netty Server SSLContext
     *
     * @param keyStorePath 密钥库路径 (PKCS12格式)
     * @param keyStorePassword 密钥库密码
     * @return Netty SslContext
     */
    public static SslContext createServerSslContext(String keyStorePath, String keyStorePassword) {
        try {
            File keyFile = new File(keyStorePath);
            SslContext sslContext = SslContextBuilder.forServer(keyFile, keyFile, keyStorePassword)
                .build();

            logger.info("Server SSL context created successfully from: {}", keyStorePath);
            return sslContext;
        } catch (Exception e) {
            logger.error("Failed to create server SSL context", e);
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }

    /**
     * 创建Netty Client SSLContext (信任所有证书 - 仅用于测试)
     *
     * @return Netty SslContext
     */
    public static SslContext createTrustAllClientSslContext() {
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

            logger.info("Client SSL context (trust all) created");
            return sslContext;
        } catch (Exception e) {
            logger.error("Failed to create client SSL context", e);
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }

    /**
     * 创建Netty Client SSLContext (带证书验证)
     *
     * @param trustStorePath 信任库路径
     * @param trustStorePassword 信任库密码
     * @return Netty SslContext
     */
    public static SslContext createClientSslContext(String trustStorePath, String trustStorePassword) {
        try {
            File trustFile = new File(trustStorePath);
            SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(trustFile)
                .build();

            logger.info("Client SSL context created successfully");
            return sslContext;
        } catch (Exception e) {
            logger.error("Failed to create client SSL context", e);
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }

    /**
     * 检查密钥库文件是否存在
     */
    public static boolean keyStoreExists(String keyStorePath) {
        if (keyStorePath == null || keyStorePath.isEmpty()) {
            return false;
        }
        return new File(keyStorePath).exists();
    }

    /**
     * 打印SSL配置帮助信息
     */
    public static void printSslHelp(String keyStorePath, String keyStorePassword) {
        if (!keyStoreExists(keyStorePath)) {
            logger.warn("KeyStore not found at: {}. SSL requires a valid keystore.", keyStorePath);
            logger.info("To generate a self-signed certificate for testing, run:");
            logger.info("keytool -genkeypair -alias ai-forward -keyalg RSA -keysize 2048 " +
                "-validity 365 -keystore {} -storepass {} -keypass {} -dname \"CN=localhost\" -ext SAN=dns:localhost",
                keyStorePath, keyStorePassword, keyStorePassword);
        }
    }
}