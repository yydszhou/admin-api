package com.xugang.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xugang.ai.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("""
            SELECT r.*
            FROM roles r
            INNER JOIN user_roles ur ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.is_deleted = 0
            """)
    List<Role> selectRolesByUserId(Long userId);

    @Select("""
            SELECT id, role_code AS roleCode, role_name AS roleName
            FROM roles
            WHERE is_deleted = 0
            ORDER BY id DESC
            """)
    List<Map<String, Object>> selectRoleOptions();

    @Select("""
            SELECT r.id,
                   r.role_code AS roleCode,
                   r.role_name AS roleName,
                   r.description,
                   COUNT(DISTINCT ur.user_id) AS userCount,
                   COUNT(DISTINCT rp.permission_id) AS permissionCount
            FROM roles r
            LEFT JOIN user_roles ur ON ur.role_id = r.id
            LEFT JOIN role_permissions rp ON rp.role_id = r.id
            WHERE r.is_deleted = 0
            GROUP BY r.id, r.role_code, r.role_name, r.description
            ORDER BY r.id DESC
            """)
    List<Map<String, Object>> selectRoleSummaryList();

    @Select("SELECT * FROM roles WHERE role_name = #{roleName} AND is_deleted = 0")
    Role selectByRoleName(String roleName);

    @Insert("INSERT INTO user_roles(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    int deleteUserRolesByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM user_roles WHERE role_id = #{roleId}")
    int deleteUserRolesByRoleId(@Param("roleId") Long roleId);

    @Insert("INSERT INTO role_permissions(role_id, permission_id) VALUES(#{roleId}, #{permissionId})")
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId}")
    int deleteRolePermissionsByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT role_id FROM user_roles WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT permission_id FROM role_permissions WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
