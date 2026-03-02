package com.xugang.ai.service;

import com.xugang.ai.AbstractIntegrationTest;
import com.xugang.ai.common.enums.ResultCode;
import com.xugang.ai.common.exception.BizException;
import com.xugang.ai.req.LoginReq;
import com.xugang.ai.req.RegisterReq;
import com.xugang.ai.resp.LoginResp;
import com.xugang.ai.resp.UserResp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserService Integration Tests")
class UserServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Should register a new user and return user response")
    void shouldRegisterNewUser() {
        // Given
        RegisterReq request = new RegisterReq();
        request.setUsername("serviceuser");
        request.setEmail("serviceuser@example.com");
        request.setPassword("password123");
        request.setTraceId("svc-test-001");

        // When
        UserResp response = userService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getUsername()).isEqualTo("serviceuser");
        assertThat(response.getEmail()).isEqualTo("serviceuser@example.com");
    }

    @Test
    @DisplayName("Should throw exception when registering with existing username")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given - First register a user
        RegisterReq request = new RegisterReq();
        request.setUsername("existinguser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setTraceId("svc-test-002");
        userService.register(request);

        // When & Then - Try to register with same username
        RegisterReq duplicateRequest = new RegisterReq();
        duplicateRequest.setUsername("existinguser");
        duplicateRequest.setEmail("another@example.com");
        duplicateRequest.setPassword("password123");
        duplicateRequest.setTraceId("svc-test-003");

        assertThatThrownBy(() -> userService.register(duplicateRequest))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void shouldThrowExceptionWhenEmailExists() {
        // Given - First register a user
        RegisterReq request = new RegisterReq();
        request.setUsername("emailuser1");
        request.setEmail("duplicateemail@example.com");
        request.setPassword("password123");
        request.setTraceId("svc-test-004");
        userService.register(request);

        // When & Then - Try to register with same email
        RegisterReq duplicateRequest = new RegisterReq();
        duplicateRequest.setUsername("emailuser2");
        duplicateRequest.setEmail("duplicateemail@example.com");
        duplicateRequest.setPassword("password123");
        duplicateRequest.setTraceId("svc-test-005");

        assertThatThrownBy(() -> userService.register(duplicateRequest))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("邮箱已被注册");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        // Given - Register a user first
        String username = "loginserviceuser";
        String password = "password123";
        String email = "loginservice@example.com";

        RegisterReq registerReq = new RegisterReq();
        registerReq.setUsername(username);
        registerReq.setEmail(email);
        registerReq.setPassword(password);
        registerReq.setTraceId("svc-test-006");
        userService.register(registerReq);

        // When
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername(username);
        loginReq.setPassword(password);
        loginReq.setTraceId("svc-test-007");

        LoginResp response = userService.login(loginReq);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isNotNull();
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("Should throw exception when logging in with non-existent user")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername("nonexistentuser123");
        loginReq.setPassword("password123");
        loginReq.setTraceId("svc-test-008");

        // When & Then
        assertThatThrownBy(() -> userService.login(loginReq))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> {
                    BizException bizEx = (BizException) ex;
                    assertThat(bizEx.getCode()).isEqualTo(ResultCode.USER_NOT_FOUND.getCode());
                });
    }

    @Test
    @DisplayName("Should throw exception when logging in with wrong password")
    void shouldThrowExceptionWhenPasswordWrong() {
        // Given - Register a user first
        String username = "wrongpassworduser";
        String email = "wrongpwd@example.com";

        RegisterReq registerReq = new RegisterReq();
        registerReq.setUsername(username);
        registerReq.setEmail(email);
        registerReq.setPassword("correctpassword");
        registerReq.setTraceId("svc-test-009");
        userService.register(registerReq);

        // When & Then
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername(username);
        loginReq.setPassword("wrongpassword");
        loginReq.setTraceId("svc-test-010");

        assertThatThrownBy(() -> userService.login(loginReq))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> {
                    BizException bizEx = (BizException) ex;
                    assertThat(bizEx.getCode()).isEqualTo(ResultCode.PASSWORD_ERROR.getCode());
                });
    }

    @Test
    @DisplayName("Should get user info by user id")
    void shouldGetUserInfoById() {
        // Given - Register a user first
        RegisterReq registerReq = new RegisterReq();
        registerReq.setUsername("getinfouser");
        registerReq.setEmail("getinfo@example.com");
        registerReq.setPassword("password123");
        registerReq.setTraceId("svc-test-011");
        UserResp registeredUser = userService.register(registerReq);

        // When
        var userInfo = userService.getUserInfo(registeredUser.getId());

        // Then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getUserId()).isEqualTo(registeredUser.getId());
        assertThat(userInfo.getUsername()).isEqualTo("getinfouser");
        assertThat(userInfo.getEmail()).isEqualTo("getinfo@example.com");
    }

    @Test
    @DisplayName("Should get user info by username")
    void shouldGetUserInfoByUsername() {
        // Given - Register a user first
        RegisterReq registerReq = new RegisterReq();
        registerReq.setUsername("getinfobyname");
        registerReq.setEmail("getinfobyname@example.com");
        registerReq.setPassword("password123");
        registerReq.setTraceId("svc-test-012");
        userService.register(registerReq);

        // When
        var userInfo = userService.getUserInfoByUsername("getinfobyname");

        // Then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getUsername()).isEqualTo("getinfobyname");
        assertThat(userInfo.getEmail()).isEqualTo("getinfobyname@example.com");
    }

    @Test
    @DisplayName("Should delete user by id")
    void shouldDeleteUser() {
        // Given - Register a user first
        RegisterReq registerReq = new RegisterReq();
        registerReq.setUsername("deleteuser");
        registerReq.setEmail("delete@example.com");
        registerReq.setPassword("password123");
        registerReq.setTraceId("svc-test-013");
        UserResp registeredUser = userService.register(registerReq);

        // When
        userService.deleteUser(registeredUser.getId());

        // Then - User should not be found after deletion
        assertThatThrownBy(() -> userService.getUserInfo(registeredUser.getId()))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> {
                    BizException bizEx = (BizException) ex;
                    assertThat(bizEx.getCode()).isEqualTo(ResultCode.USER_NOT_FOUND.getCode());
                });
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999999L))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> {
                    BizException bizEx = (BizException) ex;
                    assertThat(bizEx.getCode()).isEqualTo(ResultCode.USER_NOT_FOUND.getCode());
                });
    }
}
