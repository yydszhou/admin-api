package com.xugang.ai.resp;

import lombok.Data;

@Data
public class RbacPermissionItemResp {
    private Long id;
    private String permissionCode;
    private String permissionName;
    private String module;
    private String description;
    private Boolean enabled;
}
