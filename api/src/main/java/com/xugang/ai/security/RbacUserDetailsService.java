package com.xugang.ai.security;

import com.xugang.ai.entity.User;
import com.xugang.ai.service.RbacService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 自定义 UserDetailsService
 *
 * 每次认证时从数据库实时加载：
 *   用户 → 角色 → 权限（一次双层 JOIN，无 N+1）
 * 并将权限编码（如 user:delete）直接包装成 SimpleGrantedAuthority
 * 配合 @PreAuthorize("hasAuthority('user:delete')") 使用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RbacUserDetailsService implements UserDetailsService {

    private final RbacService rbacService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Security 加载用户: username={}", username);

        // 1. 查询用户
        User user = rbacService.loadUserByUsername(username);
        if (user == null) {
            log.warn("用户不存在: username={}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 2. 从数据库实时加载权限（一次 SQL，包含两层 JOIN）
        List<String> permCodes = rbacService.loadPermissionCodesByUserId(user.getId());

        // 3. 将权限编码转换为 GrantedAuthority（使用 hasAuthority() 匹配，不加 ROLE_ 前缀）
        List<GrantedAuthority> authorities = permCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .map(a -> (GrantedAuthority) a)
                .toList();

        log.info("用户 {} 已加载权限: {}", username, permCodes);

        // 4. 构建 Spring Security 的 UserDetails
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() != null && user.getStatus() == 0)
                .build();
    }
}
