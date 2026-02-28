---
trigger: always_on
---
# Spring Boot 后端开发规范

## 必须遵守的规则

### 1. 项目结构
- 包名 com.xugang.ai
- 严格按照分层架构：Controller → Service → Mapper → Entity
- 前端请求统一封装城Req, 返回给前端统一用Resp, 放到对应的req和resp目录下
- DTO 与 Entity 分离，禁止 Entity 直接返回给前端

### 2. 统一响应格式
- 所有接口必须返回 `ApiResponse<T>` 包装类
- 分页必须返回 count, List<T>
- 必须包含 code、message、data、timestamp、traceId 字段, traceId来自前端请求，必须包含的参数

### 3. 异常处理
- 业务异常使用 `BizException`, 系统异常使用`SysException`
- 必须使用 `@RestControllerAdvice` 全局异常处理
- 禁止在 Controller 中 try-catch 吞掉异常
- 检查参数业务异常之后，需要根据不同情况抛异常，异常的状态码需要定义在枚举里。

### 4. 参数验证
- 请求 Req 必须使用 `@Valid` 注解
- 所有字段必须有验证规则和非空提示
- 禁止在 Controller 中手动验证参数

### 5. 安全规范
- 密码必须使用 Sha256 加密
- 禁止 SQL 拼接，必须使用参数化查询
- 敏感信息日志必须脱敏
- 接口必须有认证授权

### 6. 日志规范
- 使用 SLF4J + Logback
- ERROR 记录异常堆栈，WARN 记录业务异常，INFO 记录业务流程
- 禁止记录明文密码、手机号等敏感信息

### 7. 数据库规范
- 所有表必须有 id、create_time、update_time、is_deleted 字段
- 必须使用 MyBatis-Plus 的 BaseMapper
- 事务必须加在 Service 层，使用 `@Transactional(rollbackFor = Exception.class)`

### 8. API 文档
- 所有接口必须写markdown文档，需要好看，直观，易于理解
- 接口文档需要给出完整的请求示例和响应示例

### 9. 测试规范
- 单元测试覆盖率不低于 80%
- 必须测试正常场景和异常场景
- 集成测试必须覆盖所有公开 API

### 10. 性能规范
- 简单接口响应时间 < 100ms
- 复杂查询必须分页，禁止全表查询
- 热点数据必须使用缓存

### 11. Sql语句
- 自动生成sql语句，不要执行，需要审计完之后再执行