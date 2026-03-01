package com.xugang.ai.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionStatusUpdateReq {

    @NotNull(message = "enabled不能为空")
    private Boolean enabled;

    @NotBlank(message = "traceId不能为空")
    private String traceId;
}
