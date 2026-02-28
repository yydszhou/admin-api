package com.xugang.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xugang.ai.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
