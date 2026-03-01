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

    @Select({
            "<script>",
            "SELECT u.*",
            "FROM users u",
            "LEFT JOIN user_roles ur ON u.id = ur.user_id",
            "WHERE u.is_deleted = 0",
            "<if test='keyword != null and keyword != \"\"'>",
            "  AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR u.email LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='status != null'>",
            "  AND u.status = #{status}",
            "</if>",
            "<if test='roleId != null'>",
            "  AND ur.role_id = #{roleId}",
            "</if>",
            "GROUP BY u.id",
            "ORDER BY u.id DESC",
            "LIMIT #{pageSize} OFFSET #{offset}",
            "</script>"
    })
    List<User> selectUserPage(@Param("keyword") String keyword,
                              @Param("status") Integer status,
                              @Param("roleId") Long roleId,
                              @Param("offset") Long offset,
                              @Param("pageSize") Long pageSize);

    @Select("""
        SELECT COUNT(DISTINCT u.id)  -- 用DISTINCT避免roleId关联导致的计数重复
        FROM users u
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        WHERE u.is_deleted = 0
          AND (#{keyword}::text IS NULL OR #{keyword} = ''
               OR u.username LIKE CONCAT('%', #{keyword}, '%')
               OR u.email LIKE CONCAT('%', #{keyword}, '%'))
          AND (#{status}::integer IS NULL OR u.status = #{status})
          AND (#{roleId}::bigint IS NULL OR ur.role_id = #{roleId})
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
