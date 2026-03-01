# AGENTS.md

This file provides guidance to Qoder (qoder.com) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.2.0 Java API project providing user authentication (register/login) functionality. It uses:
- Java 17
- Spring Boot 3.2.0 with Spring Web and Validation
- MyBatis-Plus 3.5.7 for ORM
- PostgreSQL database
- Lombok for boilerplate reduction

## Common Commands

All commands are run from the project root (`/Users/zhou/CodeBuddy/admin-api`):

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run

# Package into JAR
mvn clean package

# Run tests (if any exist)
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName
```

## Architecture

### Package Structure

Base package: `com.xugang.ai`

```
com.xugang.ai/
├── Application.java              # Spring Boot entry point
├── config/                       # Configuration classes
│   └── MybatisPlusConfig.java    # MyBatis-Plus pagination and auto-fill
├── controller/                   # REST API controllers
│   └── UserController.java       # /api/users endpoints
├── service/                      # Business logic layer
│   └── UserService.java          # User registration/login logic
├── entity/                       # Database entities
│   └── User.java                 # User entity with MyBatis-Plus annotations
├── mapper/                       # MyBatis-Plus data access layer
│   └── UserMapper.java           # User database operations
├── req/                          # Request DTOs with validation
│   ├── RegisterReq.java
│   └── LoginReq.java
├── resp/                         # Response DTOs
│   ├── UserResp.java
│   └── LoginResp.java
└── common/                       # Shared utilities
    ├── ApiResponse.java          # Standard API response wrapper
    ├── util/
    │   └── Sha256Util.java       # SHA256 encryption utility
    ├── enums/
    │   └── ResultCode.java       # Business error codes (200, 400, 1000-1099 for user)
    └── exception/
        ├── BizException.java     # Business exceptions (4xx)
        ├── SysException.java     # System exceptions (5xx)
        └── GlobalExceptionHandler.java  # Global exception handling
```

### Key Architectural Patterns

1. **Layered Architecture**: Controller -> Service -> Mapper -> Entity
2. **DTO Pattern**: Separate Request/Response DTOs in `req/` and `resp/` packages
3. **Global Exception Handling**: `GlobalExceptionHandler` catches all exceptions and returns `ApiResponse`
4. **Result Codes**: Business errors use codes 1000-1099 for user module (defined in `ResultCode.java`)
5. **Auto-fill Fields**: `create_time`, `update_time`, `is_deleted` are auto-managed by MyBatis-Plus
6. **Logical Delete**: Entities use `@TableLogic` on `isDeleted` field
7. **Password Security**: Passwords are SHA256 hashed (both client-side and server-side)
8. **Token Generation**: Simple SHA256-based tokens (not JWT) - see `UserService.generateToken()`

### API Endpoints

- `POST /api/users/register` - User registration
- `POST /api/users/login` - User login

Both endpoints accept/return JSON with `traceId` for request tracing.

### Database

- PostgreSQL on localhost:5432, database `ai`
- Schema defined in `src/main/resources/db/schema.sql`
- Credentials in `src/main/resources/application.yml`

### Validation

Request DTOs use Jakarta Bean Validation annotations (`@NotBlank`, `@Email`, `@Size`). Validation errors return 400 with field error messages.
