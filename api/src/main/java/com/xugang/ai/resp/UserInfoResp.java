package com.xugang.ai.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前用户信息响应体（/api/me）
 * 注意：不暴露 password 字段
 */
@Data
public class UserInfoResp {

    private Long userId;
    private String username;
    private String email;
    private Integer status;

    /** 用户拥有的角色编码列表，如 ["ADMIN"] */
    private List<String> roles;

    /** 用户拥有的权限编码列表，如 ["user:delete", "report:export"] */
    private List<String> permissions;

    private LocalDateTime createTime;
}
