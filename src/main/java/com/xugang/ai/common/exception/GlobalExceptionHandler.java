package com.xugang.ai.common.exception;

import com.xugang.ai.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException e) {
        e.printStackTrace();
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage(), null);
    }
    
    @ExceptionHandler(SysException.class)
    public ApiResponse<Void> handleSysException(SysException e) {
        e.printStackTrace();
        log.error("系统异常: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage(), null);
    }
    
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleValidationException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return ApiResponse.error(400, "参数校验失败: " + message, null);
    }
    
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.error(500, "服务器内部错误", null);
    }
}
