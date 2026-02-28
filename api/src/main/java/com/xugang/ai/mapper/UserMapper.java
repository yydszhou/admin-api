package com.xugang.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xugang.ai.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username = #{username} AND is_deleted = 0")
    User selectByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email} AND is_deleted = 0")
    User selectByEmail(String email);

    @Select("SELECT COUNT(*) FROM users WHERE username = #{username} AND is_deleted = 0")
    int countByUsername(String username);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email} AND is_deleted = 0")
    int countByEmail(String email);

    @Select("""
            SELECT u.*
            FROM users u
            WHERE u.is_deleted = 0
              AND (#{keyword} IS NULL OR #{keyword} = ''
                   OR u.username ILIKE CONCAT('%', #{keyword}, '%')
                   OR u.email ILIKE CONCAT('%', #{keyword}, '%'))
              AND (#{status} IS NULL OR u.status = #{status})
              AND (#{roleId} IS NULL OR EXISTS (
                   SELECT 1 FROM user_roles ur
                   WHERE ur.user_id = u.id AND ur.role_id = #{roleId}
              ))
            ORDER BY u.id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<User> selectUserPage(@Param("keyword") String keyword,
                              @Param("status") Integer status,
                              @Param("roleId") Long roleId,
                              @Param("offset") Long offset,
                              @Param("pageSize") Long pageSize);

    @Select("""
            SELECT COUNT(*)
            FROM users u
            WHERE u.is_deleted = 0
              AND (#{keyword} IS NULL OR #{keyword} = ''
                   OR u.username ILIKE CONCAT('%', #{keyword}, '%')
                   OR u.email ILIKE CONCAT('%', #{keyword}, '%'))
              AND (#{status} IS NULL OR u.status = #{status})
              AND (#{roleId} IS NULL OR EXISTS (
                   SELECT 1 FROM user_roles ur
                   WHERE ur.user_id = u.id AND ur.role_id = #{roleId}
              ))
            """)
    Long countUserPage(@Param("keyword") String keyword,
                       @Param("status") Integer status,
                       @Param("roleId") Long roleId);

    @Select("""
            SELECT COUNT(*)
            FROM user_roles
            WHERE user_id = #{userId} AND role_id = #{roleId}
            """)
    Long countUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
