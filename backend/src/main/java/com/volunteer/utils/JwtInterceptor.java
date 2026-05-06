package com.volunteer.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteer.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 拦截器 - 校验 Token
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty()) {
            sendError(response, 401, "请先登录");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            sendError(response, 401, "登录已过期，请重新登录");
            return false;
        }

        // 将用户信息放入请求属性
        request.setAttribute("userId", jwtUtil.getUserId(token));
        request.setAttribute("role", jwtUtil.getRole(token));
        request.setAttribute("account", jwtUtil.getAccount(token));

        return true;
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, message)));
    }
}
