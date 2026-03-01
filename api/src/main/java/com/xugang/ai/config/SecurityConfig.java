package com.xugang.ai.config;

import com.xugang.ai.security.RbacUserDetailsService;
import com.xugang.ai.security.SecurityExceptionHandler;
import com.xugang.ai.security.TokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 核心配置
 *
 * 关键设计决策：
 * 1. 无状态 Session（STATELESS）：通过 Bearer Token（Redis 存储）完成认证
 * 2. 禁用 CSRF：REST API 场景下，Token 认证已防止 CSRF 攻击
 *    原因：CSRF 攻击依赖浏览器自动携带 Cookie，而 Bearer Token 需要 JS 主动读取并附加
 * 3. 权限字符串直接作为 GrantedAuthority（不加 ROLE_ 前缀），与 @PreAuthorize("hasAuthority(...)") 对应
 * 4. @EnableMethodSecurity：启用 @PreAuthorize / @PostAuthorize 等方法级注解
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   // 启用 @PreAuthorize / @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final RbacUserDetailsService  userDetailsService;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final SecurityExceptionHandler  securityExceptionHandler;

    // ----------------------------------------------------------------
    // PasswordEncoder：BCrypt（强度 10，Spring Security 推荐）
    // ----------------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // ----------------------------------------------------------------
    // DaoAuthenticationProvider：将自定义 UserDetailsService 和 PasswordEncoder 绑定
    // ----------------------------------------------------------------
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ----------------------------------------------------------------
    // AuthenticationManager：供需要编程式认证的场景使用（如登录接口）
    // ----------------------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ----------------------------------------------------------------
    // 核心过滤链配置
    // ----------------------------------------------------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. 禁用 CSRF（REST API + Bearer Token 场景下不需要）
            .csrf(AbstractHttpConfigurer::disable)

            // 2. 无状态 Session
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. 请求授权规则
            .authorizeHttpRequests(auth -> auth
                    // 公开接口：注册、登录
                    .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                    // 其他所有接口需要认证
                    .anyRequest().authenticated()
            )

            // 4. 认证提供者
            .authenticationProvider(authenticationProvider())

            // 5. 在 UsernamePasswordAuthenticationFilter 之前插入 Token 过滤器
            .addFilterBefore(tokenAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class)

            // 6. 统一异常响应（401 / 403）
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(securityExceptionHandler)   // 401
                    .accessDeniedHandler(securityExceptionHandler)         // 403
            );

        return http.build();
    }
}
