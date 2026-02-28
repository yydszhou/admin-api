package com.xugang.ai.common.exception;

import com.xugang.ai.common.enums.ResultCode;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    
    private final Integer code;
    
    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
    
    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
