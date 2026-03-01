package com.xugang.ai.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RbacUserItemResp {
    private Long id;
    private String avatar;
    private String username;
    private String email;
    private List<RbacRoleSimpleResp> roles;
    private Integer status;
    private LocalDateTime createTime;
}
