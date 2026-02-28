package com.xugang.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xugang.ai.common.enums.ResultCode;
import com.xugang.ai.common.exception.BizException;
import com.xugang.ai.entity.Permission;
import com.xugang.ai.entity.Role;
import com.xugang.ai.entity.User;
import com.xugang.ai.mapper.PermissionMapper;
import com.xugang.ai.mapper.RoleMapper;
import com.xugang.ai.mapper.UserMapper;
import com.xugang.ai.req.RoleCreateReq;
import com.xugang.ai.req.UserCreateReq;
import com.xugang.ai.resp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RbacManageService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResp<RbacUserItemResp> pageUsers(Long page, Long pageSize, String keyword, Integer status, Long roleId) {
        long currentPage = page == null || page < 1 ? 1 : page;
        long currentSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long offset = (currentPage - 1) * currentSize;

        List<User> users = userMapper.selectUserPage(keyword, status, roleId, offset, currentSize);
        Long count = userMapper.countUserPage(keyword, status, roleId);

        List<RbacUserItemResp> list = users.stream().map(this::toUserItemResp).toList();
        PageResp<RbacUserItemResp> pageResp = new PageResp<>();
        pageResp.setCount(count == null ? 0L : count);
        pageResp.setList(list);
        return pageResp;
    }

    public List<RbacRoleSimpleResp> listRoleOptions() {
        List<Map<String, Object>> rows = roleMapper.selectRoleOptions();
        return rows.stream().map(this::toRoleSimple).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateReq req) {
        if (userMapper.countByUsername(req.getUsername()) > 0) {
            throw new BizException(ResultCode.USERNAME_EXISTS);
        }
        if (userMapper.countByEmail(req.getEmail()) > 0) {
            throw new BizException(ResultCode.EMAIL_EXISTS);
        }

        List<Long> roleIds = req.getRoleIds() == null ? Collections.emptyList() : req.getRoleIds();
        ensureRoleIdsValid(roleIds);

        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setEmail(req.getEmail().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setStatus("0".equals(req.getStatus()) ? 0 : 1);
        userMapper.insert(user);

        if (!roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                roleMapper.insertUserRole(user.getId(), roleId);
            }
        }
        log.info("创建用户成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        User user = userMapper.selectById(userId);
        if (user == null || Objects.equals(user.getIsDeleted(), 1)) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        ensureRoleIdsValid(roleIds);

        roleMapper.deleteUserRolesByUserId(userId);
        for (Long roleId : roleIds) {
            roleMapper.insertUserRole(userId, roleId);
        }
        log.info("更新用户角色成功: userId={}, roleIds={}", userId, roleIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> userIds) {
        for (Long userId : userIds) {
            User user = userMapper.selectById(userId);
            if (user == null || Objects.equals(user.getIsDeleted(), 1)) {
                continue;
            }
            userMapper.deleteById(userId);
            roleMapper.deleteUserRolesByUserId(userId);
        }
        log.info("批量删除用户成功: userIds={}", userIds);
    }

    public List<RbacRoleItemResp> listRoles() {
        List<Map<String, Object>> rows = roleMapper.selectRoleSummaryList();
        return rows.stream().map(this::toRoleItem).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleCreateReq req) {
        Role exist = roleMapper.selectByRoleName(req.getRoleName().trim());
        if (exist != null) {
            throw new BizException(ResultCode.ROLE_NAME_EXISTS);
        }
        ensurePermissionIdsValid(req.getPermissionIds());

        Role role = new Role();
        role.setRoleName(req.getRoleName().trim());
        role.setDescription(req.getDescription());
        role.setRoleCode(generateUniqueRoleCode(req.getRoleName()));
        roleMapper.insert(role);

        for (Long permissionId : req.getPermissionIds()) {
            roleMapper.insertRolePermission(role.getId(), permissionId);
        }
        log.info("创建角色成功: roleId={}, roleCode={}", role.getId(), role.getRoleCode());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleMapper.selectById(roleId);
        if (role == null || Objects.equals(role.getIsDeleted(), 1)) {
            throw new BizException(ResultCode.ROLE_NOT_FOUND);
        }
        List<Long> ids = permissionIds == null ? Collections.emptyList() : permissionIds;
        ensurePermissionIdsValid(ids);

        roleMapper.deleteRolePermissionsByRoleId(roleId);
        for (Long permissionId : ids) {
            roleMapper.insertRolePermission(roleId, permissionId);
        }
        log.info("更新角色权限成功: roleId={}, permissionIds={}", roleId, ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null || Objects.equals(role.getIsDeleted(), 1)) {
            throw new BizException(ResultCode.ROLE_NOT_FOUND);
        }
        roleMapper.deleteRolePermissionsByRoleId(roleId);
        roleMapper.deleteUserRolesByRoleId(roleId);
        roleMapper.deleteById(roleId);
        log.info("删除角色成功: roleId={}", roleId);
    }

    public List<RbacPermissionTreeResp> permissionTree(String keyword) {
        List<Permission> permissions = permissionMapper.selectPermissionList(keyword);
        Map<String, RbacPermissionTreeResp> grouped = new LinkedHashMap<>();

        for (Permission permission : permissions) {
            String module = permission.getModule() == null ? "default" : permission.getModule();
            RbacPermissionTreeResp tree = grouped.computeIfAbsent(module, key -> {
                RbacPermissionTreeResp item = new RbacPermissionTreeResp();
                item.setModule(key);
                return item;
            });
            tree.getChildren().add(toPermissionItem(permission));
        }

        return new ArrayList<>(grouped.values());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePermissionStatus(Long permissionId, Boolean enabled) {
        Long count = permissionMapper.countByPermissionId(permissionId);
        if (count == null || count == 0) {
            throw new BizException(ResultCode.PERMISSION_NOT_FOUND);
        }
        permissionMapper.updatePermissionEnabled(permissionId, Boolean.TRUE.equals(enabled) ? 0 : 1);
        log.info("更新权限状态成功: permissionId={}, enabled={}", permissionId, enabled);
    }

    public List<Long> rolePermissionIds(Long roleId) {
        return roleMapper.selectPermissionIdsByRoleId(roleId);
    }

    public List<Long> userRoleIds(Long userId) {
        return roleMapper.selectRoleIdsByUserId(userId);
    }

    private RbacUserItemResp toUserItemResp(User user) {
        RbacUserItemResp resp = new RbacUserItemResp();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        resp.setAvatar("");

        List<RbacRoleSimpleResp> roles = roleMapper.selectRolesByUserId(user.getId()).stream()
                .map(role -> {
                    RbacRoleSimpleResp item = new RbacRoleSimpleResp();
                    item.setId(role.getId());
                    item.setRoleCode(role.getRoleCode());
                    item.setRoleName(role.getRoleName());
                    return item;
                })
                .toList();
        resp.setRoles(roles);
        return resp;
    }

    private RbacRoleSimpleResp toRoleSimple(Map<String, Object> row) {
        RbacRoleSimpleResp resp = new RbacRoleSimpleResp();
        resp.setId(toLong(row.get("id")));
        resp.setRoleCode(String.valueOf(row.get("roleCode")));
        resp.setRoleName(String.valueOf(row.get("roleName")));
        return resp;
    }

    private RbacRoleItemResp toRoleItem(Map<String, Object> row) {
        RbacRoleItemResp resp = new RbacRoleItemResp();
        resp.setId(toLong(row.get("id")));
        resp.setRoleCode(String.valueOf(row.get("roleCode")));
        resp.setRoleName(String.valueOf(row.get("roleName")));
        resp.setDescription((String) row.get("description"));
        resp.setUserCount(toLong(row.get("userCount")));
        resp.setPermissionCount(toLong(row.get("permissionCount")));
        return resp;
    }

    private RbacPermissionItemResp toPermissionItem(Permission permission) {
        RbacPermissionItemResp resp = new RbacPermissionItemResp();
        resp.setId(permission.getId());
        resp.setPermissionCode(permission.getPermissionCode());
        resp.setPermissionName(permission.getPermissionName());
        resp.setModule(permission.getModule());
        resp.setDescription(permission.getDescription());
        resp.setEnabled(!Objects.equals(permission.getIsDeleted(), 1));
        return resp;
    }

    private void ensureRoleIdsValid(List<Long> roleIds) {
        for (Long roleId : roleIds) {
            Role role = roleMapper.selectById(roleId);
            if (role == null || Objects.equals(role.getIsDeleted(), 1)) {
                throw new BizException(ResultCode.ROLE_NOT_FOUND);
            }
        }
    }

    private void ensurePermissionIdsValid(List<Long> permissionIds) {
        for (Long permissionId : permissionIds) {
            Long count = permissionMapper.countByPermissionId(permissionId);
            if (count == null || count == 0) {
                throw new BizException(ResultCode.PERMISSION_NOT_FOUND);
            }
        }
    }

    private String generateUniqueRoleCode(String roleName) {
        String base = roleName == null ? "ROLE" : roleName.trim().replaceAll("\\s+", "_")
                .replaceAll("[^A-Za-z0-9_\\u4e00-\\u9fa5]", "").toUpperCase();
        if (base.isBlank()) {
            base = "ROLE";
        }

        String roleCode = base;
        int seq = 1;
        while (roleMapper.selectOne(new QueryWrapper<Role>()
                .eq("role_code", roleCode)
                .eq("is_deleted", 0)) != null) {
            roleCode = base + "_" + seq++;
        }
        return roleCode;
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }
}
