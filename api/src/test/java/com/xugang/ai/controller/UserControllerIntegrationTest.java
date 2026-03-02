package com.xugang.ai.controller;

import com.xugang.ai.AbstractIntegrationTest;
import com.xugang.ai.common.ApiResponse;
import com.xugang.ai.req.LoginReq;
import com.xugang.ai.req.RegisterReq;
import com.xugang.ai.resp.LoginResp;
import com.xugang.ai.resp.UserResp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String BASE_URL = "/api/users";

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        // Given
        RegisterReq request = new RegisterReq();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPassword("password123");
        request.setTraceId("test-trace-001");

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                request,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("注册成功");
    }

    @Test
    @DisplayName("Should fail to register with duplicate username")
    void shouldFailToRegisterWithDuplicateUsername() {
        // Given - First register a user
        RegisterReq request = new RegisterReq();
        request.setUsername("duplicateuser");
        request.setEmail("unique@example.com");
        request.setPassword("password123");
        request.setTraceId("test-trace-002");

        restTemplate.postForEntity(BASE_URL + "/register", request, ApiResponse.class);

        // When - Try to register with same username but different email
        RegisterReq duplicateRequest = new RegisterReq();
        duplicateRequest.setUsername("duplicateuser");
        duplicateRequest.setEmail("another@example.com");
        duplicateRequest.setPassword("password123");
        duplicateRequest.setTraceId("test-trace-003");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                duplicateRequest,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(1001); // USERNAME_EXISTS
    }

    @Test
    @DisplayName("Should fail to register with duplicate email")
    void shouldFailToRegisterWithDuplicateEmail() {
        // Given - First register a user
        RegisterReq request = new RegisterReq();
        request.setUsername("user1");
        request.setEmail("duplicate@example.com");
        request.setPassword("password123");
        request.setTraceId("test-trace-004");

        restTemplate.postForEntity(BASE_URL + "/register", request, ApiResponse.class);

        // When - Try to register with same email but different username
        RegisterReq duplicateRequest = new RegisterReq();
        duplicateRequest.setUsername("user2");
        duplicateRequest.setEmail("duplicate@example.com");
        duplicateRequest.setPassword("password123");
        duplicateRequest.setTraceId("test-trace-005");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                duplicateRequest,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(1002); // EMAIL_EXISTS
    }

    @Test
    @DisplayName("Should fail to register with invalid email format")
    void shouldFailToRegisterWithInvalidEmail() {
        // Given
        RegisterReq request = new RegisterReq();
        request.setUsername("testuser2");
        request.setEmail("invalid-email");
        request.setPassword("password123");
        request.setTraceId("test-trace-006");

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                request,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should fail to register with short username")
    void shouldFailToRegisterWithShortUsername() {
        // Given
        RegisterReq request = new RegisterReq();
        request.setUsername("ab");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setTraceId("test-trace-007");

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                request,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should fail to register with short password")
    void shouldFailToRegisterWithShortPassword() {
        // Given
        RegisterReq request = new RegisterReq();
        request.setUsername("testuser3");
        request.setEmail("test3@example.com");
        request.setPassword("123");
        request.setTraceId("test-trace-008");

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                request,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        // Given - First register a user
        String username = "logintest";
        String password = "password123";
        String email = "login@example.com";

        RegisterReq registerReq = new RegisterReq();
        registerReq.setUsername(username);
        registerReq.setEmail(email);
        registerReq.setPassword(password);
        registerReq.setTraceId("test-trace-009");
        restTemplate.postForEntity(BASE_URL + "/register", registerReq, ApiResponse.class);

        // When - Login with the registered user
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername(username);
        loginReq.setPassword(password);
        loginReq.setTraceId("test-trace-010");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginReq,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("登录成功");
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("Should fail to login with non-existent user")
    void shouldFailToLoginWithNonExistentUser() {
        // Given
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername("nonexistentuser");
        loginReq.setPassword("password123");
        loginReq.setTraceId("test-trace-011");

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginReq,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(1003); // USER_NOT_FOUND
    }

    @Test
    @DisplayName("Should fail to login with wrong password")
    void shouldFailToLoginWithWrongPassword() {
        // Given - First register a user
        String username = "wrongpasswordtest";
        String email = "wrongpwd@example.com";

        RegisterReq registerReq = new RegisterReq();
        registerReq.setUsername(username);
        registerReq.setEmail(email);
        registerReq.setPassword("correctpassword");
        registerReq.setTraceId("test-trace-012");
        restTemplate.postForEntity(BASE_URL + "/register", registerReq, ApiResponse.class);

        // When - Try to login with wrong password
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername(username);
        loginReq.setPassword("wrongpassword");
        loginReq.setTraceId("test-trace-013");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginReq,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(1005); // PASSWORD_ERROR
    }

    @Test
    @DisplayName("Should fail to login with empty username")
    void shouldFailToLoginWithEmptyUsername() {
        // Given
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername("");
        loginReq.setPassword("password123");
        loginReq.setTraceId("test-trace-014");

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginReq,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should fail to login with empty password")
    void shouldFailToLoginWithEmptyPassword() {
        // Given
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername("testuser");
        loginReq.setPassword("");
        loginReq.setTraceId("test-trace-015");

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginReq,
                ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
