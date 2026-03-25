package org.ljc.deploy.controller;

import org.ljc.deploy.entity.User;
import org.ljc.deploy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        if (username == null || password == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户名和密码不能为空");
            return ResponseEntity.badRequest().body(result);
        }
        
        Map<String, Object> result = userService.login(username, password);
        if (!(Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        // 处理 "Bearer " 前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Map<String, Object> result = userService.logout(token);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> passwords) {
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        User user = userService.getUserByToken(token);
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "未登录或登录已过期");
            return ResponseEntity.status(401).body(result);
        }
        
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "密码不能为空");
            return ResponseEntity.badRequest().body(result);
        }
        
        Map<String, Object> result = userService.changePassword(user.getId(), oldPassword, newPassword);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        User user = userService.getUserByToken(token);
        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }
        
        result.put("success", true);
        result.put("data", Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "mustChangePassword", user.isMustChangePassword()
        ));
        return ResponseEntity.ok(result);
    }
}
