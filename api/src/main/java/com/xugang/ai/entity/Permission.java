package com.xugang.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("permissions")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限编码（即 GrantedAuthority 的字符串值）
     * 格式：模块:操作，如 user:delete / report:export / config:update
     */
    @TableField("permission_code")
    private String permissionCode;

    /** 权限名称，便于展示 */
    @TableField("permission_name")
    private String permissionName;

    /** 所属模块，如 user / report / config */
    @TableField("module")
    private String module;

    @TableField("description")
    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    @TableLogic
    private Integer isDeleted;
}
