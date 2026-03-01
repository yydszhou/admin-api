package com.xugang.ai.resp;

import lombok.Data;

@Data
public class RbacRoleItemResp {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Long userCount;
    private Long permissionCount;
}
