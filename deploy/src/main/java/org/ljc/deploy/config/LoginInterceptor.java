package org.ljc.deploy.config;

import org.ljc.deploy.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    
    private final UserService userService;
    
    public LoginInterceptor(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // 放行认证接口和静态资源
        if (uri.startsWith("/api/auth/") || uri.startsWith("/static/") || uri.equals("/api/deploy/") || uri.equals("/api/deploy/index")) {
            return true;
        }
        
        // 获取 token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 验证 token
        if (userService.validateToken(token)) {
            return true;
        }
        
        // 返回 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"未登录或登录已过期\"}");
        return false;
    }
}
