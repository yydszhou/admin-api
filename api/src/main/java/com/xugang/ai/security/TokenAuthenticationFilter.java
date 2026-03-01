package com.xugang.ai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Bearer Token 认证过滤器
 *
 * 处理流程：
 *   1. 从请求头 Authorization: Bearer {token} 中提取 token
 *   2. 根据 token 从 Redis 查找 userId，再加载 UserDetails（含权限）
 *   3. 将认证信息写入 SecurityContextHolder
 *
 * 注意：此 Filter 不负责登录，只负责验证已登录请求
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenAuthService          tokenAuthService;
    private final RbacUserDetailsService    userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 根据 token 从 Redis 解析出 username
            String username = tokenAuthService.getUsernameByToken(token);

            if (StringUtils.hasText(username)) {
                // 从数据库实时加载用户 + 权限
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Token 认证成功: username={}, authorities={}",
                            username, userDetails.getAuthorities());
                }
            } else {
                log.debug("Token 无效或已过期: token={}", token.substring(0, Math.min(token.length(), 8)) + "...");
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从 Authorization 请求头提取 Bearer Token
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }
}
