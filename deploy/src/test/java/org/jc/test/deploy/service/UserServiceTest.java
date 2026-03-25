package org.jc.test.deploy.service;

import org.ljc.deploy.entity.User;
import org.ljc.deploy.mapper.UserMapper;
import org.ljc.deploy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 * @author AI Forward Team
 * @created 2026-03-25
 */
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userMapper);
    }

    /**
     * 测试登录成功
     */
    @Test
    public void testLoginSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("admin");
        user.setMustChangePassword(true);

        when(userMapper.findByUsername("admin")).thenReturn(user);

        Map<String, Object> result = userService.login("admin", "admin");

        assertTrue((Boolean) result.get("success"));
        assertEquals("登录成功", result.get("message"));
        assertTrue(result.containsKey("token"));
        assertEquals("admin", result.get("username"));
        assertTrue((Boolean) result.get("mustChangePassword"));
    }

    /**
     * 测试登录失败 - 密码错误
     */
    @Test
    public void testLoginWrongPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("admin");

        when(userMapper.findByUsername("admin")).thenReturn(user);

        Map<String, Object> result = userService.login("admin", "wrongpassword");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
    }

    /**
     * 测试登录失败 - 用户不存在
     */
    @Test
    public void testLoginUserNotFound() {
        when(userMapper.findByUsername("notexist")).thenReturn(null);

        Map<String, Object> result = userService.login("notexist", "password");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
    }

    /**
     * 测试修改密码成功
     */
    @Test
    public void testChangePasswordSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("admin");

        when(userMapper.findById(1L)).thenReturn(user);

        Map<String, Object> result = userService.changePassword(1L, "admin", "newpassword123");

        assertTrue((Boolean) result.get("success"));
        assertEquals("密码修改成功", result.get("message"));
        verify(userMapper).update(user);
        assertFalse(user.isMustChangePassword());
    }

    /**
     * 测试修改密码失败 - 原密码错误
     */
    @Test
    public void testChangePasswordWrongOldPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("admin");

        when(userMapper.findById(1L)).thenReturn(user);

        Map<String, Object> result = userService.changePassword(1L, "wrongpassword", "newpassword");

        assertFalse((Boolean) result.get("success"));
        assertEquals("原密码错误", result.get("message"));
    }

    /**
     * 测试修改密码失败 - 新密码太短
     */
    @Test
    public void testChangePasswordTooShort() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("admin");

        when(userMapper.findById(1L)).thenReturn(user);

        Map<String, Object> result = userService.changePassword(1L, "admin", "123");

        assertFalse((Boolean) result.get("success"));
        assertEquals("密码长度至少 6 位", result.get("message"));
    }

    /**
     * 测试修改密码失败 - 用户不存在
     */
    @Test
    public void testChangePasswordUserNotFound() {
        when(userMapper.findById(999L)).thenReturn(null);

        Map<String, Object> result = userService.changePassword(999L, "admin", "newpassword");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户不存在", result.get("message"));
    }

    /**
     * 测试 token 验证
     */
    @Test
    public void testValidateToken() {
        // 先登录生成 token
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("admin");

        when(userMapper.findByUsername("admin")).thenReturn(user);
        Map<String, Object> loginResult = userService.login("admin", "admin");
        String token = (String) loginResult.get("token");

        assertTrue(userService.validateToken(token));
        assertFalse(userService.validateToken("invalid_token"));
        assertFalse(userService.validateToken(null));
    }

    /**
     * 测试登出
     */
    @Test
    public void testLogout() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("admin");

        when(userMapper.findByUsername("admin")).thenReturn(user);
        Map<String, Object> loginResult = userService.login("admin", "admin");
        String token = (String) loginResult.get("token");

        Map<String, Object> logoutResult = userService.logout(token);
        assertTrue((Boolean) logoutResult.get("success"));

        // 再次验证 token 应该无效
        assertFalse(userService.validateToken(token));
    }
}
