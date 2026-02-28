package com.xugang.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xugang.ai.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询该用户的所有角色（一次 JOIN，防止 N+1）
     */
    @Select("""
            SELECT r.*
            FROM roles r
            INNER JOIN user_roles ur ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.is_deleted = 0
            """)
    List<Role> selectRolesByUserId(Long userId);
}
