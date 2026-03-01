package com.xugang.ai.service;

import com.xugang.ai.entity.Permission;
import com.xugang.ai.entity.Role;
import com.xugang.ai.entity.User;
import com.xugang.ai.mapper.PermissionMapper;
import com.xugang.ai.mapper.RoleMapper;
import com.xugang.ai.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RBAC 权限查询服务
 * 负责：用户 → 角色 → 权限 的数据加载链路
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RbacService {

    private final UserMapper       userMapper;
    private final RoleMapper       roleMapper;
    private final PermissionMapper permissionMapper;

    /**
     * 根据用户名加载用户实体
     */
    public User loadUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 根据用户ID加载角色列表（一次 JOIN，不产生 N+1）
     */
    public List<Role> loadRolesByUserId(Long userId) {
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        log.debug("userId={} 拥有角色: {}", userId,
                roles.stream().map(Role::getRoleCode).toList());
        return roles;
    }

    /**
     * 根据用户ID加载权限编码列表（两层 JOIN，一次查询）
     * 链路：users → user_roles → roles → role_permissions → permissions
     */
    public List<String> loadPermissionCodesByUserId(Long userId) {
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
        List<String> codes = permissions.stream()
                .map(Permission::getPermissionCode)
                .distinct()
                .toList();
        log.debug("userId={} 拥有权限: {}", userId, codes);
        return codes;
    }
}
