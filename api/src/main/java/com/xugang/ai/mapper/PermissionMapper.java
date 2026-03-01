package com.xugang.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xugang.ai.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

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

    @Select("""
            SELECT p.*
            FROM permissions p
            WHERE (#{keyword} IS NULL OR #{keyword} = ''
                   OR p.permission_name ILIKE CONCAT('%', #{keyword}, '%')
                   OR p.permission_code ILIKE CONCAT('%', #{keyword}, '%')
                   OR p.module ILIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY p.module, p.id
            """)
    List<Permission> selectPermissionList(@Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM permissions WHERE id = #{permissionId}")
    Long countByPermissionId(@Param("permissionId") Long permissionId);

    @Update("""
            UPDATE permissions
            SET is_deleted = #{isDeleted},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{permissionId}
            """)
    int updatePermissionEnabled(@Param("permissionId") Long permissionId,
                                @Param("isDeleted") Integer isDeleted);
}
