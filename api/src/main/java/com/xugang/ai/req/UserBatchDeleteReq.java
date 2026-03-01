package com.xugang.ai.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserBatchDeleteReq {

    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    @NotBlank(message = "traceId不能为空")
    private String traceId;
}
