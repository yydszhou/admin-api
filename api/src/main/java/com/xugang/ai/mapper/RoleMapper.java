package com.xugang.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xugang.ai.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    List<Role> selectRolesByUserId(Long userId);

    List<Map<String, Object>> selectRoleOptions();

    List<Map<String, Object>> selectRoleSummaryList();

    Role selectByRoleName(String roleName);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int deleteUserRolesByUserId(@Param("userId") Long userId);

    int deleteUserRolesByRoleId(@Param("roleId") Long roleId);

    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    int deleteRolePermissionsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
