package com.xugang.ai.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户登录响应体（含权限列表）
 */
@Data
public class LoginResp {

    private Long userId;
    private String username;
    private String email;
    private String token;

    /** 登录时一次性返回角色编码，便于前端做菜单渲染 */
    private List<String> roles;

    /** 登录时一次性返回权限编码，便于前端做按钮级控制 */
    private List<String> permissions;

    private LocalDateTime createTime;
}
