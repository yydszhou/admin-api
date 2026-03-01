package com.xugang.ai.controller;

import com.xugang.ai.common.ApiResponse;
import com.xugang.ai.resp.UserInfoResp;
import com.xugang.ai.security.RbacUserDetailsService;
import com.xugang.ai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * RBAC 权限示例控制器
 *
 * GET  /api/me              - 已登录即可访问（返回当前用户信息 + 权限列表）
 * DELETE /api/users/{id}   - 需要 user:delete 权限
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RbacController {

    private final UserService            userService;
    private final RbacUserDetailsService userDetailsService;

    /**
     * 获取当前登录用户信息及权限列表
     * 认证：Bearer Token（任何已登录用户均可访问）
     */
    @GetMapping("/me")
    public ApiResponse<UserInfoResp> me(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        log.info("查询当前用户信息: username={}", userDetails.getUsername());

        // 通过 username 查询完整用户信息（含权限）
        // 此处利用已缓存在 Security 上下文中的 principal，再查数据库补充 DTO 字段
        UserInfoResp resp = userService.getUserInfoByUsername(userDetails.getUsername());

        return ApiResponse.success("获取用户信息成功", resp, traceId);
    }

    /**
     * 删除用户
     * 需要权限：user:delete
     * 使用 hasAuthority() 而非 hasRole()，权限字符串直接匹配
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ApiResponse<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        log.info("删除用户: targetUserId={}, operator={}", id, userDetails.getUsername());

        userService.deleteUser(id);

        return ApiResponse.success("用户已删除", null, traceId);
    }

    /**
     * 修改配置示例
     * 需要权限：config:update
     */
    @PutMapping("/config")
    @PreAuthorize("hasAuthority('config:update')")
    public ApiResponse<Void> updateConfig(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        log.info("修改配置操作: operator={}", userDetails.getUsername());
        return ApiResponse.success("配置已更新", null, traceId);
    }

    /**
     * 导出报表示例
     * 需要权限：report:export
     */
    @PostMapping("/report/export")
    @PreAuthorize("hasAuthority('report:export')")
    public ApiResponse<Void> exportReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        log.info("导出报表操作: operator={}", userDetails.getUsername());
        return ApiResponse.success("报表导出任务已提交", null, traceId);
    }
}
