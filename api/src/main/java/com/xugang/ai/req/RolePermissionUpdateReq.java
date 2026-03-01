package com.xugang.ai.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionUpdateReq {

    private List<Long> permissionIds;

    @NotBlank(message = "traceId不能为空")
    private String traceId;
}
