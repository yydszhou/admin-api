package com.xugang.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xugang.ai.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(String username);

    User selectByEmail(String email);

    int countByUsername(String username);

    int countByEmail(String email);

    List<User> selectUserPage(@Param("keyword") String keyword,
                              @Param("status") Integer status,
                              @Param("roleId") Long roleId,
                              @Param("offset") Long offset,
                              @Param("pageSize") Long pageSize);

    Long countUserPage(@Param("keyword") String keyword,
                       @Param("status") Integer status,
                       @Param("roleId") Long roleId);

    Long countUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
