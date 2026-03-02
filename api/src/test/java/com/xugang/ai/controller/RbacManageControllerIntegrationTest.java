package com.xugang.ai.controller;

import com.xugang.ai.AbstractIntegrationTest;
import com.xugang.ai.common.ApiResponse;
import com.xugang.ai.entity.Role;
import com.xugang.ai.entity.User;
import com.xugang.ai.mapper.PermissionMapper;
import com.xugang.ai.mapper.RoleMapper;
import com.xugang.ai.mapper.UserMapper;
import com.xugang.ai.req.LoginReq;
import com.xugang.ai.req.RegisterReq;
import com.xugang.ai.req.RoleCreateReq;
import com.xugang.ai.req.UserCreateReq;
import com.xugang.ai.resp.LoginResp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RBAC 管理接口集成测试
 * 测试覆盖：角色创建、用户创建、权限分配等核心功能
 * 每个测试都会验证数据库状态
 */
@DisplayName("RbacManageController Integration Tests")
class RbacManageControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    private static final String BASE_URL = "/api/rbac";
    private static final String USER_URL = "/api/users";

    private String adminToken;

    /**
     * 每个测试前初始化：创建管理员账号并登录获取token
     */
    @BeforeEach
    void setUp() {
        // 注册管理员用户
        RegisterReq registerReq = new RegisterReq();
        registerReq.setUsername("admin");
        registerReq.setEmail("admin@test.com");
        registerReq.setPassword("admin123");
        registerReq.setTraceId("setup-trace-001");

        ResponseEntity<ApiResponse> registerResp = restTemplate.postForEntity(
                USER_URL + "/register",
                registerReq,
                ApiResponse.class
        );

        // 登录获取token
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername("admin");
        loginReq.setPassword("admin123");
        loginReq.setTraceId("setup-trace-002");

        ResponseEntity<ApiResponse> loginResp = restTemplate.postForEntity(
                USER_URL + "/login",
                loginReq,
                ApiResponse.class
        );

        if (loginResp.getBody() != null && loginResp.getBody().getData() != null) {
            // 使用 Map 来获取 token，避免类型转换问题
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> loginData = (java.util.Map<String, Object>) loginResp.getBody().getData();
            adminToken = (String) loginData.get("token");
        }
    }

    /**
     * 创建带认证头的HTTP请求实体
     */
    private <T> HttpEntity<T> createAuthRequest(T body, String traceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + adminToken);
        headers.set("X-Trace-Id", traceId);
        return new HttpEntity<>(body, headers);
    }

    @Test
    @DisplayName("Should create role successfully and verify in database")
    void shouldCreateRoleAndVerifyInDatabase() {
        // Given - 首先获取可用的权限ID
        HttpEntity<Void> authRequest = createAuthRequest(null, "test-trace-001");
        ResponseEntity<ApiResponse> permResp = restTemplate.exchange(
                BASE_URL + "/permissions/tree",
                HttpMethod.GET,
                authRequest,
                ApiResponse.class
        );
        assertThat(permResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 获取第一个权限ID（假设数据库已初始化权限数据）
        List<Long> permissionIds = permissionMapper.selectList(null).stream()
                .map(p -> p.getId())
                .limit(3)
                .toList();

        assertThat(permissionIds).isNotEmpty();

        String roleName = "TEST_ROLE_" + System.currentTimeMillis();
        RoleCreateReq request = new RoleCreateReq();
        request.setRoleName(roleName);
        request.setDescription("Test role description");
        request.setPermissionIds(permissionIds);
        request.setTraceId("test-trace-001");

        // When - 调用创建角色接口
        HttpEntity<RoleCreateReq> httpRequest = createAuthRequest(request, "test-trace-001");
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                BASE_URL + "/roles",
                HttpMethod.POST,
                httpRequest,
                ApiResponse.class
        );

        // Then - 验证接口响应
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("创建角色成功");

        // Then - 验证数据库中确实存在该角色
        Role createdRole = roleMapper.selectByRoleName(roleName);
        assertThat(createdRole).isNotNull();
        assertThat(createdRole.getRoleName()).isEqualTo(roleName);
        assertThat(createdRole.getDescription()).isEqualTo("Test role description");

        // Then - 验证角色权限关联是否正确
        List<Long> rolePermissionIds = roleMapper.selectPermissionIdsByRoleId(createdRole.getId());
        assertThat(rolePermissionIds).containsExactlyInAnyOrderElementsOf(permissionIds);
    }

    @Test
    @DisplayName("Should create user with roles and verify in database")
    void shouldCreateUserWithRolesAndVerifyInDatabase() {
        // Given - 首先创建一个角色
        List<Long> permissionIds = permissionMapper.selectList(null).stream()
                .map(p -> p.getId())
                .limit(3)
                .toList();

        assertThat(permissionIds).isNotEmpty();

        String roleName = "USER_TEST_ROLE_" + System.currentTimeMillis();
        RoleCreateReq roleReq = new RoleCreateReq();
        roleReq.setRoleName(roleName);
        roleReq.setDescription("Role for user test");
        roleReq.setPermissionIds(permissionIds);
        roleReq.setTraceId("test-trace-002-role");

        HttpEntity<RoleCreateReq> roleHttpRequest = createAuthRequest(roleReq, "test-trace-002-role");
        ResponseEntity<ApiResponse> roleResponse = restTemplate.exchange(
                BASE_URL + "/roles",
                HttpMethod.POST,
                roleHttpRequest,
                ApiResponse.class
        );
        Role createdRole = roleMapper.selectByRoleName(roleName);
        assertThat(createdRole).isNotNull();
        Long roleId = createdRole.getId();

        // Given - 准备创建用户请求 (用户名长度限制3-20字符)
        String uniqueId = String.valueOf(System.currentTimeMillis() % 1000000); // 取后6位
        String username = "user" + uniqueId; // 4+6=10字符，符合3-20限制
        String email = "u" + uniqueId + "@test.com"; // 短邮箱格式

        UserCreateReq request = new UserCreateReq();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("password123");
        request.setStatus("1");
        request.setRoleIds(Collections.singletonList(roleId));
        request.setTraceId("test-trace-002");

        // When - 调用创建用户接口
        HttpEntity<UserCreateReq> httpRequest = createAuthRequest(request, "test-trace-002");
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                BASE_URL + "/users",
                HttpMethod.POST,
                httpRequest,
                ApiResponse.class
        );

        // Then - 验证接口响应
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("创建用户成功");

        // Then - 验证数据库中确实存在该用户
        User createdUser = userMapper.selectByUsername(username);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo(username);
        assertThat(createdUser.getEmail()).isEqualTo(email);
        assertThat(createdUser.getStatus()).isEqualTo(1);

        // Then - 验证用户角色关联是否正确
        List<Long> userRoleIds = roleMapper.selectRoleIdsByUserId(createdUser.getId());
        assertThat(userRoleIds).containsExactly(roleId);
    }

    @Test
    @DisplayName("Should list all roles and verify against database")
    void shouldListRolesAndVerifyAgainstDatabase() {
        // Given - 确保至少有一个角色存在
        List<Long> permissionIds = permissionMapper.selectList(null).stream()
                .map(p -> p.getId())
                .limit(1)
                .toList();

        if (!permissionIds.isEmpty()) {
            String roleName = "LIST_TEST_ROLE_" + System.currentTimeMillis();
            RoleCreateReq roleReq = new RoleCreateReq();
            roleReq.setRoleName(roleName);
            roleReq.setDescription("Role for list test");
            roleReq.setPermissionIds(permissionIds);
            roleReq.setTraceId("test-trace-003-role");

            HttpEntity<RoleCreateReq> roleHttpRequest = createAuthRequest(roleReq, "test-trace-003-role");
            restTemplate.exchange(
                    BASE_URL + "/roles",
                    HttpMethod.POST,
                    roleHttpRequest,
                    ApiResponse.class
            );
        }

        // When - 调用获取角色列表接口
        HttpEntity<Void> authRequest = createAuthRequest(null, "test-trace-003");
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                BASE_URL + "/roles",
                HttpMethod.GET,
                authRequest,
                ApiResponse.class
        );

        // Then - 验证接口响应
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);

        // Then - 验证数据库中的角色数量
        long dbRoleCount = roleMapper.selectCount(null);
        assertThat(dbRoleCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should update user roles and verify in database")
    void shouldUpdateUserRolesAndVerifyInDatabase() {
        // Given - 创建两个角色
        List<Long> permissionIds = permissionMapper.selectList(null).stream()
                .map(p -> p.getId())
                .limit(3)
                .toList();

        assertThat(permissionIds).isNotEmpty();

        String roleName1 = "UPDATE_ROLE_1_" + System.currentTimeMillis();
        String roleName2 = "UPDATE_ROLE_2_" + System.currentTimeMillis();

        createRole(roleName1, permissionIds, "test-trace-004-role1");
        createRole(roleName2, permissionIds, "test-trace-004-role2");

        Role role1 = roleMapper.selectByRoleName(roleName1);
        Role role2 = roleMapper.selectByRoleName(roleName2);

        assertThat(role1).isNotNull();
        assertThat(role2).isNotNull();

        // Given - 创建用户并分配第一个角色 (用户名长度限制3-20字符)
        String uniqueId = String.valueOf(System.currentTimeMillis() % 1000000); // 取后6位
        String username = "upd" + uniqueId; // 3+6=9字符，符合3-20限制
        UserCreateReq userReq = new UserCreateReq();
        userReq.setUsername(username);
        userReq.setEmail("u" + uniqueId + "@test.com"); // 短邮箱格式
        userReq.setPassword("password123");
        userReq.setStatus("1");
        userReq.setRoleIds(Collections.singletonList(role1.getId()));
        userReq.setTraceId("test-trace-004-user");

        HttpEntity<UserCreateReq> userHttpRequest = createAuthRequest(userReq, "test-trace-004-user");
        restTemplate.exchange(
                BASE_URL + "/users",
                HttpMethod.POST,
                userHttpRequest,
                ApiResponse.class
        );

        User createdUser = userMapper.selectByUsername(username);
        assertThat(createdUser).isNotNull();

        // 验证初始角色
        List<Long> initialRoles = roleMapper.selectRoleIdsByUserId(createdUser.getId());
        assertThat(initialRoles).containsExactly(role1.getId());

        // When - 更新用户角色为第二个角色
        String updateUrl = BASE_URL + "/users/" + createdUser.getId() + "/roles";
        String jsonBody = "{\"roleIds\":[" + role2.getId() + "],\"traceId\":\"test-trace-004-update\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + adminToken);
        headers.set("X-Trace-Id", "test-trace-004-update");
        HttpEntity<String> updateRequest = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                updateUrl,
                HttpMethod.PUT,
                updateRequest,
                ApiResponse.class
        );

        // Then - 验证接口响应
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("更新用户角色成功");

        // Then - 验证数据库中用户角色已更新
        List<Long> updatedRoles = roleMapper.selectRoleIdsByUserId(createdUser.getId());
        assertThat(updatedRoles).containsExactly(role2.getId());
        assertThat(updatedRoles).doesNotContain(role1.getId());
    }

    @Test
    @DisplayName("Should fail to create role with duplicate name")
    void shouldFailToCreateRoleWithDuplicateName() {
        // Given - 首先创建一个角色
        List<Long> permissionIds = permissionMapper.selectList(null).stream()
                .map(p -> p.getId())
                .limit(3)
                .toList();

        assertThat(permissionIds).isNotEmpty();

        String roleName = "DUPLICATE_ROLE_" + System.currentTimeMillis();
        RoleCreateReq roleReq = new RoleCreateReq();
        roleReq.setRoleName(roleName);
        roleReq.setDescription("Original role");
        roleReq.setPermissionIds(permissionIds);
        roleReq.setTraceId("test-trace-005-role");

        HttpEntity<RoleCreateReq> roleHttpRequest = createAuthRequest(roleReq, "test-trace-005-role");
        restTemplate.exchange(
                BASE_URL + "/roles",
                HttpMethod.POST,
                roleHttpRequest,
                ApiResponse.class
        );

        // 验证第一个角色创建成功
        Role firstRole = roleMapper.selectByRoleName(roleName);
        assertThat(firstRole).isNotNull();

        // When - 尝试创建同名角色
        RoleCreateReq duplicateReq = new RoleCreateReq();
        duplicateReq.setRoleName(roleName);
        duplicateReq.setDescription("Duplicate role");
        duplicateReq.setPermissionIds(permissionIds);
        duplicateReq.setTraceId("test-trace-005-duplicate");

        HttpEntity<RoleCreateReq> duplicateHttpRequest = createAuthRequest(duplicateReq, "test-trace-005-duplicate");
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                BASE_URL + "/roles",
                HttpMethod.POST,
                duplicateHttpRequest,
                ApiResponse.class
        );

        // Then - 验证接口返回错误
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(1103); // ROLE_NAME_EXISTS

        // Then - 验证数据库中只有一个该名称的角色
        List<Role> rolesWithName = roleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role>()
                        .eq("role_name", roleName)
                        .eq("is_deleted", 0)
        );
        assertThat(rolesWithName).hasSize(1);
    }

    @Test
    @DisplayName("Should get user roles and verify against database")
    void shouldGetUserRolesAndVerifyAgainstDatabase() {
        // Given - 创建角色和用户
        List<Long> permissionIds = permissionMapper.selectList(null).stream()
                .map(p -> p.getId())
                .limit(3)
                .toList();

        assertThat(permissionIds).isNotEmpty();

        String roleName = "GET_ROLE_" + System.currentTimeMillis();
        createRole(roleName, permissionIds, "test-trace-006-role");

        Role role = roleMapper.selectByRoleName(roleName);
        assertThat(role).isNotNull();

        // 用户名长度限制3-20字符
        String uniqueId = String.valueOf(System.currentTimeMillis() % 1000000); // 取后6位
        String username = "get" + uniqueId; // 3+6=9字符，符合3-20限制
        UserCreateReq userReq = new UserCreateReq();
        userReq.setUsername(username);
        userReq.setEmail("g" + uniqueId + "@test.com"); // 短邮箱格式
        userReq.setPassword("password123");
        userReq.setStatus("1");
        userReq.setRoleIds(Collections.singletonList(role.getId()));
        userReq.setTraceId("test-trace-006-user");

        HttpEntity<UserCreateReq> userHttpRequest = createAuthRequest(userReq, "test-trace-006-user");
        restTemplate.exchange(
                BASE_URL + "/users",
                HttpMethod.POST,
                userHttpRequest,
                ApiResponse.class
        );

        User createdUser = userMapper.selectByUsername(username);
        assertThat(createdUser).isNotNull();

        // When - 调用获取用户角色接口
        HttpEntity<Void> authRequest = createAuthRequest(null, "test-trace-006");
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                BASE_URL + "/users/" + createdUser.getId() + "/roles",
                HttpMethod.GET,
                authRequest,
                ApiResponse.class
        );

        // Then - 验证接口响应
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);

        // Then - 验证数据库中的角色
        List<Long> dbRoleIds = roleMapper.selectRoleIdsByUserId(createdUser.getId());
        assertThat(dbRoleIds).containsExactly(role.getId());
    }

    /**
     * 辅助方法：创建角色
     */
    private void createRole(String roleName, List<Long> permissionIds, String traceId) {
        RoleCreateReq roleReq = new RoleCreateReq();
        roleReq.setRoleName(roleName);
        roleReq.setDescription("Test role");
        roleReq.setPermissionIds(permissionIds);
        roleReq.setTraceId(traceId);

        HttpEntity<RoleCreateReq> roleHttpRequest = createAuthRequest(roleReq, traceId);
        restTemplate.exchange(
                BASE_URL + "/roles",
                HttpMethod.POST,
                roleHttpRequest,
                ApiResponse.class
        );
    }
}
