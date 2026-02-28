package com.xugang.ai.controller;

import com.xugang.ai.common.ApiResponse;
import com.xugang.ai.req.*;
import com.xugang.ai.resp.*;
import com.xugang.ai.service.RbacManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
public class RbacManageController {

    private final RbacManageService rbacManageService;

    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResp<RbacUserItemResp>> pageUsers(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long roleId,
            @RequestHeader("X-Trace-Id") String traceId) {

        return ApiResponse.success("获取用户列表成功",
                rbacManageService.pageUsers(page, pageSize, keyword, status, roleId), traceId);
    }

    @PostMapping("/users")
    @PreAuthorize("hasAnyAuthority('user:create', 'role:manage')")
    public ApiResponse<Void> createUser(@Valid @RequestBody UserCreateReq req) {
        rbacManageService.createUser(req);
        return ApiResponse.success("创建用户成功", null, req.getTraceId());
    }

    @PutMapping("/users/{userId}/roles")
    @PreAuthorize("hasAnyAuthority('user:update', 'role:manage')")
    public ApiResponse<Void> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateReq req) {
        rbacManageService.updateUserRoles(userId, req.getRoleIds());
        return ApiResponse.success("更新用户角色成功", null, req.getTraceId());
    }

    @PostMapping("/users/batch-delete")
    @PreAuthorize("hasAnyAuthority('user:delete', 'role:manage')")
    public ApiResponse<Void> batchDeleteUsers(@Valid @RequestBody UserBatchDeleteReq req) {
        rbacManageService.batchDeleteUsers(req.getUserIds());
        return ApiResponse.success("批量删除用户成功", null, req.getTraceId());
    }

    @GetMapping("/users/{userId}/roles")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Long>> userRoleIds(
            @PathVariable Long userId,
            @RequestHeader("X-Trace-Id") String traceId) {
        return ApiResponse.success("获取用户角色成功", rbacManageService.userRoleIds(userId), traceId);
    }

    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RbacRoleItemResp>> listRoles(
            @RequestHeader("X-Trace-Id") String traceId) {
        return ApiResponse.success("获取角色列表成功", rbacManageService.listRoles(), traceId);
    }

    @GetMapping("/roles/options")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RbacRoleSimpleResp>> roleOptions(
            @RequestHeader("X-Trace-Id") String traceId) {
        return ApiResponse.success("获取角色选项成功", rbacManageService.listRoleOptions(), traceId);
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAnyAuthority('role:manage', 'config:update')")
    public ApiResponse<Void> createRole(@Valid @RequestBody RoleCreateReq req) {
        rbacManageService.createRole(req);
        return ApiResponse.success("创建角色成功", null, req.getTraceId());
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasAnyAuthority('role:manage', 'config:update')")
    public ApiResponse<Void> deleteRole(
            @PathVariable Long roleId,
            @RequestHeader("X-Trace-Id") String traceId) {
        rbacManageService.deleteRole(roleId);
        return ApiResponse.success("删除角色成功", null, traceId);
    }

    @PutMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAnyAuthority('role:manage', 'config:update')")
    public ApiResponse<Void> updateRolePermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody RolePermissionUpdateReq req) {
        rbacManageService.updateRolePermissions(roleId, req.getPermissionIds());
        return ApiResponse.success("更新角色权限成功", null, req.getTraceId());
    }

    @GetMapping("/roles/{roleId}/permissions")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Long>> rolePermissionIds(
            @PathVariable Long roleId,
            @RequestHeader("X-Trace-Id") String traceId) {
        return ApiResponse.success("获取角色权限成功", rbacManageService.rolePermissionIds(roleId), traceId);
    }

    @GetMapping("/permissions/tree")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RbacPermissionTreeResp>> permissionTree(
            @RequestParam(required = false) String keyword,
            @RequestHeader("X-Trace-Id") String traceId) {
        return ApiResponse.success("获取权限树成功", rbacManageService.permissionTree(keyword), traceId);
    }

    @PutMapping("/permissions/{permissionId}/status")
    @PreAuthorize("hasAnyAuthority('role:manage', 'config:update')")
    public ApiResponse<Void> updatePermissionStatus(
            @PathVariable Long permissionId,
            @Valid @RequestBody PermissionStatusUpdateReq req) {
        rbacManageService.updatePermissionStatus(permissionId, req.getEnabled());
        return ApiResponse.success("更新权限状态成功", null, req.getTraceId());
    }
}
