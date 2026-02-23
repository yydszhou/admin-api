package com.xugang.ai.common;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {
    
    private Integer code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String traceId;
    
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(Integer code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
        this.timestamp = LocalDateTime.now();
    }
    
    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>(200, "success", data, traceId);
    }
    
    public static <T> ApiResponse<T> success(String message, T data, String traceId) {
        return new ApiResponse<>(200, message, data, traceId);
    }
    
    public static <T> ApiResponse<T> error(Integer code, String message, String traceId) {
        return new ApiResponse<>(code, message, null, traceId);
    }
}
