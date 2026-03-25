package org.ljc.deploy.service;

import org.ljc.deploy.entity.User;
import org.ljc.deploy.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务
 * 负责用户认证和会话管理
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;
    
    /**
     * 会话存储：token -> userId
     */
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();
    
    /**
     * 是否已初始化默认用户
     */
    private boolean initialized = false;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    /**
     * 懒加载初始化默认用户
     */
    private void initDefaultUserIfNeeded() {
        if (initialized) {
            return;
        }
        try {
            User existingUser = userMapper.findByUsername("admin");
            if (existingUser == null) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword("admin");
                adminUser.setMustChangePassword(true);
                adminUser.setCreatedAt(LocalDateTime.now());
                adminUser.setUpdatedAt(LocalDateTime.now());
                userMapper.insert(adminUser);
                logger.info("Created default user: admin/admin (must change password on first login)");
            }
            initialized = true;
        } catch (Exception e) {
            logger.debug("Init default user failed, will retry: {}", e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        initDefaultUserIfNeeded();
        Map<String, Object> result = new HashMap<>();
        
        User user = userMapper.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        if (!user.getPassword().equals(password)) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        String token = generateToken(username);
        sessions.put(token, user.getId());
        
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("username", username);
        result.put("mustChangePassword", user.isMustChangePassword());
        
        logger.info("User {} logged in", username);
        return result;
    }
    
    /**
     * 用户登出
     */
    public Map<String, Object> logout(String token) {
        Map<String, Object> result = new HashMap<>();
        
        if (token != null && sessions.containsKey(token)) {
            sessions.remove(token);
            result.put("success", true);
            result.put("message", "登出成功");
        } else {
            result.put("success", false);
            result.put("message", "无效的 token");
        }
        
        return result;
    }
    
    /**
     * 修改密码
     */
    public Map<String, Object> changePassword(Long userId, String oldPassword, String newPassword) {
        initDefaultUserIfNeeded();
        Map<String, Object> result = new HashMap<>();
        
        User user = userMapper.findById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        if (!user.getPassword().equals(oldPassword)) {
            result.put("success", false);
            result.put("message", "原密码错误");
            return result;
        }
        
        if (newPassword.length() < 6) {
            result.put("success", false);
            result.put("message", "密码长度至少 6 位");
            return result;
        }
        
        user.setPassword(newPassword);
        user.setMustChangePassword(false);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        
        result.put("success", true);
        result.put("message", "密码修改成功");
        
        logger.info("Password changed for user {}", user.getUsername());
        return result;
    }
    
    /**
     * 验证 token
     */
    public boolean validateToken(String token) {
        return token != null && sessions.containsKey(token);
    }
    
    /**
     * 根据 token 获取用户
     */
    public User getUserByToken(String token) {
        if (token == null || !sessions.containsKey(token)) {
            return null;
        }
        Long userId = sessions.get(token);
        return userMapper.findById(userId);
    }
    
    /**
     * 生成 token
     */
    private String generateToken(String username) {
        return "token_" + username + "_" + System.currentTimeMillis();
    }
}
