package com.xugang.ai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xugang.ai.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 统一安全异常处理器
 * - 401：未登录 / Token 无效
 * - 403：已登录但无权限
 */
@Slf4j
@Component
public class SecurityExceptionHandler
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // 支持 LocalDateTime 序列化

    /** 401 - 未认证 */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("未认证访问: uri={}, error={}", request.getRequestURI(), authException.getMessage());
        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                ApiResponse.error(401, "未登录或 Token 已失效，请重新登录", extractTraceId(request)));
    }

    /** 403 - 已认证但无权限 */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("权限不足: uri={}, error={}", request.getRequestURI(), accessDeniedException.getMessage());
        writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                ApiResponse.error(403, "权限不足，无法执行此操作", extractTraceId(request)));
    }

    private void writeJson(HttpServletResponse response, int status, ApiResponse<?> body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private String extractTraceId(HttpServletRequest request) {
        return request.getHeader("X-Trace-Id");
    }
}
