package org.ljc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 版本信息类
 * 自动从git和构建配置中获取版本信息
 */
public class Version {
    private static final String VERSION_UNKNOWN = "unknown";
    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream is = Version.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (is != null) {
                PROPERTIES.load(is);
            }
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * 获取版本号
     */
    public static String getVersion() {
        return PROPERTIES.getProperty("version", VERSION_UNKNOWN);
    }

    /**
     * 获取Git提交哈希
     */
    public static String getGitCommit() {
        return PROPERTIES.getProperty("git.commit", VERSION_UNKNOWN);
    }

    /**
     * 获取Git描述
     */
    public static String getGitDescribe() {
        return PROPERTIES.getProperty("git.describe", VERSION_UNKNOWN);
    }

    /**
     * 获取构建时间
     */
    public static String getBuildTime() {
        return PROPERTIES.getProperty("build.time", VERSION_UNKNOWN);
    }

    /**
     * 获取完整版本信息
     */
    public static String getFullVersion() {
        String version = getVersion();
        String gitDescribe = getGitDescribe();

        if (!VERSION_UNKNOWN.equals(gitDescribe)) {
            return version + "-" + gitDescribe;
        }
        return version;
    }

    /**
     * 打印版本信息
     */
    public static void print() {
        System.out.println("========================================");
        System.out.println("  AI Forward Version Information");
        System.out.println("========================================");
        System.out.println("Version:     " + getVersion());
        System.out.println("Git Commit:  " + getGitCommit());
        System.out.println("Git Describe:" + getGitDescribe());
        System.out.println("Build Time:  " + getBuildTime());
        System.out.println("========================================");
    }

    public static void main(String[] args) {
        print();
    }
}