package com.xugang.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xugang.ai.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID一次性加载该用户所有角色对应的权限集合（两层 JOIN，防止 N+1）
     * 链路：users → user_roles → roles → role_permissions → permissions
     */
    @Select("""
            SELECT DISTINCT p.*
            FROM permissions p
            INNER JOIN role_permissions rp ON p.id = rp.permission_id
            INNER JOIN roles r             ON r.id = rp.role_id
            INNER JOIN user_roles ur       ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.is_deleted = 0
              AND p.is_deleted = 0
            """)
    List<Permission> selectPermissionsByUserId(Long userId);
}
