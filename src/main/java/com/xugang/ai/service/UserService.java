package com.xugang.ai.service;

import com.xugang.ai.common.enums.ResultCode;
import com.xugang.ai.common.exception.BizException;
import com.xugang.ai.common.util.Sha256Util;
import com.xugang.ai.entity.User;
import com.xugang.ai.mapper.UserMapper;
import com.xugang.ai.req.RegisterReq;
import com.xugang.ai.resp.UserResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    
    @Transactional(rollbackFor = Exception.class)
    public UserResp register(RegisterReq req) {
        log.info("用户注册: username={}", req.getUsername());
        
        // 检查用户名
        if (userMapper.countByUsername(req.getUsername()) > 0) {
            log.warn("用户名已存在: username={}", req.getUsername());
            throw new BizException(ResultCode.USERNAME_EXISTS);
        }
        
        // 检查邮箱
        if (userMapper.countByEmail(req.getEmail()) > 0) {
            log.warn("邮箱已被注册: email={}", maskEmail(req.getEmail()));
            throw new BizException(ResultCode.EMAIL_EXISTS);
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(Sha256Util.encrypt(req.getPassword()));
        
        userMapper.insert(user);
        
        log.info("用户注册成功: userId={}", user.getId());
        
        return convertToResp(user);
    }
    
    private UserResp convertToResp(User user) {
        UserResp resp = new UserResp();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setCreate_time(user.getCreateTime());
        resp.setUpdate_time(user.getUpdateTime());
        return resp;
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String masked = local.length() > 2 
                ? local.substring(0, 2) + "***" 
                : "***";
        return masked + "@" + parts[1];
    }
}
