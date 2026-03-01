package com.xugang.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xugang.ai.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    List<Permission> selectPermissionsByUserId(Long userId);

    List<Permission> selectPermissionList(@Param("keyword") String keyword);

    Long countByPermissionId(@Param("permissionId") Long permissionId);

    int updatePermissionEnabled(@Param("permissionId") Long permissionId,
                                @Param("isDeleted") Integer isDeleted);
}
