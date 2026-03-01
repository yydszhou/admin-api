package com.xugang.ai.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleCreateReq {

    @NotBlank(message = "角色名不能为空")
    @Size(max = 100, message = "角色名长度不能超过100")
    private String roleName;

    @Size(max = 255, message = "描述长度不能超过255")
    private String description;

    @NotEmpty(message = "初始权限不能为空")
    private List<Long> permissionIds;

    @NotBlank(message = "traceId不能为空")
    private String traceId;
}
