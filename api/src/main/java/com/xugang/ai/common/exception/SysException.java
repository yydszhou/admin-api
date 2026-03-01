package com.xugang.ai.common.exception;

import lombok.Getter;

@Getter
public class SysException extends RuntimeException {
    
    private final Integer code;
    
    public SysException(String message) {
        super(message);
        this.code = 500;
    }
    
    public SysException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
